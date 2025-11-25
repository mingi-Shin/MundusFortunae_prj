package com.mingisoft.mf.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;



@Controller
public class WebSocketController {

  private final static Logger logger = LoggerFactory.getLogger(WebSocketController.class);

  public WebSocketController() {
    
  }
  
  
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
  
  @GetMapping("/admin/socketChat")
  public String getWebsocketChatPage() {
    return "webSocket-game/webSocketChat";
  }
  
  @PostMapping("/webSocket/createRoom")
  public ResponseEntity<?> createNewRoom(@RequestBody String roomSetting ){
    
    logger.info("roomSetting 수신 : {}",roomSetting); //roomTitle, roomPassword, nickname
    -> 이제 여기서 roomSetting 값 가지고, 소켓 방을 만들어보자 
    
    return null;
  }
  
  
  
  
}
