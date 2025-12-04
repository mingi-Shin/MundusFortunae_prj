package com.mingisoft.mf.game;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.mingisoft.mf.api.ApiResponse;
import com.mingisoft.mf.api.ErrorResponse;
import com.mingisoft.mf.socketCommon.SocketRoomHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class RoomController {

  private final static Logger logger = LoggerFactory.getLogger(RoomController.class);

  private final RoomService roomService;
  private final SocketRoomBroadcaster socketRoomBroadcaster; //원래 여기서 하면 안되고, 소켓관련 로직은 따로 처리해줘야 하는데..
  
  public RoomController(RoomService roomService, SocketRoomBroadcaster socketRoomBroadcaster) {
    this.roomService = roomService;
    this.socketRoomBroadcaster = socketRoomBroadcaster;
  }
  /**
   * 웹게임 방 리스트 뷰페이지 
   * @param model
   * @return
   */
  @GetMapping({"/webSocket/websocketRoom", "/webSocket/websocketRoom/?hasError=true"})
  public String getWebsocketGameRoomList(@RequestParam(required = false) String hasError, Model model) {
    
    if(hasError != null) {
      model.addAttribute("hasError", "true");
    }
    
    Collection<RoomDto> roomList = roomService.getAllRooms();
    
    model.addAttribute("roomList", roomList);
    
    return "webSocket-game/webSocketRoomList";
  }
  
  /**
   * 소켓채팅방 연습용 
   * @return
   */
  @GetMapping("/webSocket/socketChat")
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
      RoomDto newRoom =  roomService.createGameRoom(roomTitle, roomPassword, nickname);
      
      //웹소켓 브로드캐스트 호출(데이터 전달)
      Collection<RoomDto> allRooms  = roomService.getAllRooms();
      socketRoomBroadcaster.sendRoomList(allRooms);

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
  public ResponseEntity<?> joinRoomRequest(@PathVariable String roomSeq, @RequestBody Map<String, String> bodyData, HttpServletRequest request){
    
    RoomDto room = roomService.getRoom(Long.valueOf(roomSeq));
    
    if(room == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(ErrorResponse.of(HttpStatus.NOT_FOUND, "존재하지 않는 방입니다.", "/api/rooms/" + roomSeq + "/join"));
    }
    
    logger.info("방 비밀번호 : {}", room.getPassword());
    String role = Optional.ofNullable(bodyData.get("role")).orElse("USER");
    logger.info("참여자 role : {} / 비밀번호 : {}", role, bodyData.get("roomPassword"));
    
    //검증
    //1. 맨먼저 방장의 방생성 후 요청
    if("HOST".equals(bodyData.get("role")) && room.getPassword().equals(bodyData.get("roomPassword"))) {
      //GetMapping용 Session 티켓 생성
      HttpSession newSession = request.getSession(true);
      newSession.setAttribute("joinableRoom" + roomSeq, "true");
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
    if(roomService.isFullRoom(Long.valueOf(roomSeq))) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(ErrorResponse.of(HttpStatus.FORBIDDEN, "방 인원이 가득 찼습니다.", "/api/rooms/" + roomSeq + "/join"));
    }
    
    //검증 모두 통과 
    roomService.addPlayerToRoom(Long.valueOf(roomSeq), inputNickname);
    
    //GetMapping용 Session 티켓 생성
    HttpSession newSession = request.getSession(true);
    newSession.setAttribute("joinableRoom" + roomSeq, "true");
    
    return ResponseEntity.status(HttpStatus.OK)
        .body(ApiResponse.of(HttpStatus.OK, "일반 유저 검증통과, 방 조인 성공", null));
    
  }
  
  
  /**
   * 방 페이지 이동  비상상황! URL하드코딩하면 접속되어버림!
   * 처리한 방법 = 서버에서 하는 게 좋음. js는 조작가능성 존재. -> 세션 활용 
   * @param roomNumber
   * @return
   */
  //RESTful API 디자인에 따르면 방번호를 주는게 맞다. 
  @GetMapping("/webSocket/room/{roomSeq}")
  public String enterRoom(@PathVariable(value = "roomSeq") String roomSeq, HttpServletRequest request, Model model) {
    
    //--- 접속자 세션 검증 --- 
    HttpSession session = request.getSession(false); //1. 접속자의 JESESSIONID 쿠키가 있는지 2. 그 쿠키의 ID가 내 서버 세션에 있는지
    if(session == null) {
      // 세션 자체가 없으니 → 이 사람은 ‘우리 시스템이 기억하는 사람’이 아님
      logger.info("세션 존재하지 않음_session : {}", session);
      return "redirect:/webSocket/websocketRoom?hasError=true";
    }
    
    boolean isJoinableTrue =  "true".equals(session.getAttribute("joinableRoom"+roomSeq));
    if(!Boolean.TRUE.equals(isJoinableTrue)) {
      logger.info("접속 거절_isJoinable : {}", isJoinableTrue);
      //세션은 용케 있는데, true값이 아님
      return "redirect:/webSocket/websocketRoom?hasError=true";
    }
    
    // --- 접속자 세션 검증 통과 ---
    //-- 후속처리 
    RoomDto roomInfo = roomService.getRoom(Long.valueOf(roomSeq));
    logger.info("접속하는 방 정보 : {}", roomInfo);
    
    //대기실 브로드캐스트 호출(방 참여인원 표기 업데이트 때문에 )
    Collection<RoomDto> allRooms  = roomService.getAllRooms();
    socketRoomBroadcaster.sendRoomList(allRooms);
    
    //최초렌더링 : html, 그 후 렌더링 : 소켓 
    model.addAttribute("roomSeq", roomSeq);
    
    return "webSocket-game/playDiceGame";
  }
  
  //유저 퇴장시 서버단 리스트에서 삭제 요청 
  @PostMapping("/webSocket/leave")
  public ResponseEntity<?> removePlayerFromServerRoom(@RequestBody PlayerDto player) {
    Long roomSeq = player.getRoomSeq();
    String nickname = player.getNickname();
    try {
      roomService.leavePlayerFromRoom(roomSeq, nickname);
      roomService.deleteEmptyRoom(roomSeq); //만약 빈방되면 리스트에서 방 삭제 
      logger.info("처리후 생성되어있는 방 갯수 : {}", roomService.getAllRooms().size());
      logger.info("방 리스트가 비어있냐? : {}", roomService.getAllRooms().isEmpty());
      return ResponseEntity
          .status(HttpStatus.OK)
          .body(ApiResponse.of(HttpStatus.OK, "퇴장 유저 리스트에서 추출 성공", null));
    } catch (Exception e) {
      logger.warn("유저 퇴장처리 실패 : {}", e.getMessage(), e);
      return ResponseEntity
              .status(HttpStatus.BAD_REQUEST)
              .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, "유저 퇴장처리 실패", "/webSocket/leave"));
    }
  }
  
}
