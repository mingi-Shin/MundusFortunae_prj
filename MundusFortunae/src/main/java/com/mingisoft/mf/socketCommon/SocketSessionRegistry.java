package com.mingisoft.mf.socketCommon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class SocketSessionRegistry { //통신 세션만 관리 

  //대기실에 있는 모든 사람의 세션
  //연결된 모든 소켓세션을 저장 (ArrayList는 불안정, CopyOnWriteArrayList<>() 로 바꿔야.. )
  private final List<WebSocketSession> waitingRoomSessions = new ArrayList<WebSocketSession>();
  
  //map도 불안정함 바꿔야해 (ConcurrentHashMap 로)
  private final Map<Long, List<WebSocketSession>> gameRoomSessions = new HashMap<Long, List<WebSocketSession>>();
  
  //대기방 유저 세션 불러오기 함수 
  public List<WebSocketSession> getWaitingRoomSessions (){
    return waitingRoomSessions;
  }
  
  //특정방 유저 세션 불러오기 함수
  public Map<Long, List<WebSocketSession>> getGameRoomSessions() {
    return gameRoomSessions;
  }
  
  //해당 방 세션에서 플레이어들 정보만 뽑기(SocketChatBroadcaster에서 세션은 json으로 변환 불가)
  public List<SocketPlayerDto> getSocketPlayerDtoList(Long roomSeq){
    List<SocketPlayerDto> players = new ArrayList<SocketPlayerDto>();
    List <WebSocketSession> sessionList = gameRoomSessions.get(roomSeq);
    for(WebSocketSession ws : sessionList) {
      SocketPlayerDto player = (SocketPlayerDto) ws.getAttributes().get("playerDto");
      players.add(player);
    }
    return players;
  }
}
