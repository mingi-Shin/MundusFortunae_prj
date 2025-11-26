package com.mingisoft.mf.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    
    List<Map<String, Object>> roomList = new ArrayList<Map<String,Object>>();
    Map<String, Object> room1 = new HashMap<String, Object>();
    Map<String, Object> room2 = new HashMap<String, Object>();
    room1.put("id", 1);
    room1.put("title", "탱커구합니다.");
    room1.put("playerCount", 3);
    room2.put("id", 2);
    room2.put("title", "힐러만 오심 돼요~");
    room2.put("playerCount", 5);
    
    roomList.add(room1);
    roomList.add(room2);
    
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
  public ResponseEntity<?> createNewRoom(@RequestBody Map<String, String> roomSetting ){
    
    // Map<String, String>일 때만 
    // {"roomSetting":{"roomTitle":"방제테스트","roomPassword":"4260","nickname":"신민기닉네임"}}
    boolean hasEmpty = 
        roomSetting == null || 
        roomSetting.values().stream().anyMatch(v -> v == null || v.isBlank());
    
    if (hasEmpty) {
      logger.warn("roomSetting 값 중 null 또는 빈 문자열이 있습니다. roomSetting={}", roomSetting);
      --이어서 return ResponseEntity.status(null). 
    }   
    
    //gameService.createGameRoom(room, roomSetting)
    
    //방 만들고, 방 주소 return 
    return ResponseEntity.status(HttpStatus.OK).body(Map.of("roomNumber",1004)); //가정 
  }
  
  /**
   * 방 참여 요청 
   * @param roomNumber
   * @return
   */
  //방주소 받아와서 이동 ?
  @GetMapping("/webSocket/room/{roomNumber}")
  public String joinRoom(@PathVariable(value = "roomNumber") String roomNumber) {
    
    logger.info("게임방 참여 : {}", roomNumber); 
    
    return "webSocket-game/webSocketDiceGame";
  }
}
