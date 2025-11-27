package com.mingisoft.mf.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.mingisoft.mf.api.ApiResponse;
import com.mingisoft.mf.api.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class GameController {

  private final static Logger logger = LoggerFactory.getLogger(GameController.class);

  private final GameService gameService;
  
  public GameController(GameService gameService) {
    this.gameService = gameService;
  }
  /**
   * 웹게임 방 리스트 뷰페이지 
   * @param model
   * @return
   */
  @GetMapping("/admin/websocketRoom")
  public String getWebsocketGameRoomList(Model model) {
    
    List<RoomDto> roomList = gameService.getAllRoomList();
    
    model.addAttribute("roomList", roomList);
    
    return "webSocket-game/webSocketRoomList";
  }
  
  /**
   * 소켓채팅방 연습용 
   * @return
   */
  @GetMapping("/admin/socketChat")
  public String getWebsocketChatPage() {
    return "webSocket-game/webSocketChat";
  }
  
  /**
   * 방 만들기 요청 
   * @param roomSetting
   * @return
   */
  @PostMapping("/webSocket/createRoom")
  public ResponseEntity<?> createNewRoom(@RequestBody Map<String, String> roomSetting, HttpServletRequest req ){
    
    // Map<String, String>일 때만 
    // {"roomSetting":{"roomTitle":"방제테스트","roomPassword":"4260","nickname":"신민기닉네임"}}
    boolean hasEmpty = 
        roomSetting == null || 
        roomSetting.values().stream().anyMatch(v -> v == null || v.isBlank());
    //null체크
    if (hasEmpty) {
      logger.warn("roomSetting 값 중 null 또는 빈 문자열이 있습니다. roomSetting={}", roomSetting);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, "roomSetting에 빈 문자열 존재", req.getRequestURI()));
    }
    
    String roomTitle = roomSetting.get("roomTitle");
    String roomPassword = roomSetting.get("roomPassword");
    String nickname = roomSetting.get("nickname");
    logger.info("컨트롤러 받은 값 : roomTitle : {} / roomPassword : {} / nickname : {}",
        roomTitle, roomPassword, nickname);
    
    try {
      RoomDto newRoom =  gameService.createGameRoom(roomTitle, roomPassword, nickname);

      return ResponseEntity
          .status(HttpStatus.OK)
          .body(ApiResponse.of(HttpStatus.OK, "방객체 생성 완료 ", newRoom)); //다 넘겨버려 
    } catch (Exception e) {
      logger.warn("방생성 실패 : {}", e.getMessage(), e);
      return ResponseEntity
          .status(HttpStatus.CONFLICT)
          .body(ErrorResponse.of(HttpStatus.CONFLICT, "방객체 생성 실패 \n관리자에게 문의해주세요.", req.getRequestURI()));
    }
    
    //방 만들고, 방 주소 return 
  }
  
  /**
   * 
   * @param roomSeq 요청 방 번호
   * @param bodyData 방장: nickname, role, roomPassword / 일반유저 :  nickname, roomPassword
   * @param model
   * @return
   */
  @PostMapping("/webSocket/room/{roomSeq}/join")
  public ResponseEntity<?> joinRoomRequest(@PathVariable String roomSeq, @RequestBody Map<String, String> bodyData){
    
    RoomDto room = gameService.getRoom(Long.valueOf(roomSeq));
    
    if(room == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(ErrorResponse.of(HttpStatus.NOT_FOUND, "존재하지 않는 방입니다.", "/api/rooms/" + roomSeq + "/join"));
    }
    
    logger.info("방 비밀번호 : {}", room.getPassword());
    logger.info("방장 role : {} / 비밀번호 : {}", bodyData.get("role"), bodyData.get("roomPassword"));
    
    //검증
    //1. 맨먼저 방장의 방생성 후 요청
    if("HOST".equals(bodyData.get("role")) && room.getPassword().equals(bodyData.get("roomPassword"))) {
      logger.info("방장의 방생성 후 조인 요청 통과");
      return ResponseEntity.status(HttpStatus.OK)
          .body(ApiResponse.of(HttpStatus.OK, "검증통과, 방 조인 성공", null));
    }
    
    //2. 일반유저의 비밀번호 체크 (null 허용이면 조건 분기)
    if (!room.getPassword().equals(bodyData.get("roomPassword"))) {
      return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .body(ErrorResponse.of(HttpStatus.UNAUTHORIZED, "비밀번호가 올바르지 않습니다.", "/api/rooms/" + roomSeq + "/join"));
    }
    
    //3. 일반유저의 닉네임 중복 체크 
    String inputNickname = bodyData.get("nickname");
    boolean isDuplicatedNickname = 
        room.getPlayerList().stream().anyMatch( p -> inputNickname.equals(p.getNickname()));
    
    if(isDuplicatedNickname) {
      logger.warn("닉네임 중복: roomSeq={}, nickname={}", room.getRoomSeq(), inputNickname);
      return ResponseEntity.status(HttpStatus.CONFLICT)
              .body(ErrorResponse.of(
                HttpStatus.CONFLICT,
                "이미 사용 중인 닉네임입니다.",
                "/api/rooms/" + room.getRoomSeq() + "/join"
              ));
    }
      
    //3. 일반유저의 방인원 체크 
    if(gameService.isFullRoom(Long.valueOf(roomSeq))) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(ErrorResponse.of(HttpStatus.FORBIDDEN, "방 인원이 가득 찼습니다.", "/api/rooms/" + roomSeq + "/join"));
    }
    
    //검증끝
    gameService.addPlayerToRoom(Long.valueOf(roomSeq), inputNickname);
    
    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.of(HttpStatus.OK, "일반 유저 검증통과, 방 조인 성공", null));
    
  }
  
  
  /**
   * 방 페이지 이동  비상상황! URL하드코딩하면 접속되어버림! 암호화코드를 추가해야 할지도.. 
   * @param roomNumber
   * @return
   */
  //RESTful API 디자인에 따르면 방번호를 주는게 맞다. 
  @GetMapping("/webSocket/room/{roomSeq}")
  public String enterRoom(@PathVariable(value = "roomSeq") String roomSeq, Model model) {
    
    RoomDto joinRoom = gameService.getRoom(Long.valueOf(roomSeq));
    model.addAttribute("roomObj", joinRoom);
    
    return "webSocket-game/webSocketDiceGame";
  }
}
