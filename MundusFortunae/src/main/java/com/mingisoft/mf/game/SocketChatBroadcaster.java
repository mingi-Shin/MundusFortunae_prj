package com.mingisoft.mf.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.mingisoft.mf.common.ObjectMapperSingleton;

/**
 * 여기는 방별로 세션 관리  사용자 방 추가는 controller가 아니라 Handler에서 처리해야함 
 */
@Component
public class SocketChatBroadcaster {
  
  private final ObjectMapperSingleton objectMapper;
  private final RoomService roomService;
  
  public SocketChatBroadcaster(ObjectMapperSingleton objectMapper, RoomService roomService) {
    this.objectMapper = objectMapper;
    this.roomService = roomService;
  }
  
  //map도 불안정함 바꿔야해 
  Map<String, List<WebSocketSession>> roomSessionMap = new HashMap<String, List<WebSocketSession>>();
  
  public void addSession(WebSocketSession session) {
    
  }
  
  public void removeSession(WebSocketSession session) {
    
  }
  
  //해당 방의 최신정보를 해당방 인원들에게 브로드캐스트
  public void renewalRoomPlayers(Long roomSeq) {
    RoomDto currentRoom = roomService.getRoom(roomSeq);
    for() {
      
    }
  }
}
