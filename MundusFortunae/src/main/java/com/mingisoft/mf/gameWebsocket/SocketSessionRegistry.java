package com.mingisoft.mf.gameWebsocket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class SocketSessionRegistry { //통신 세션만 관리 

  //대기실에 있는 모든 사람의 세션 선언
  //연결된 모든 소켓세션을 저장 (ArrayList는 불안정, CopyOnWriteArrayList<>() 로 바꿔야.. )
  //private final List<WebSocketSession> waitingRoomSessions = new ArrayList<WebSocketSession>();
  private final List<WebSocketSession> waitingRoomSessions = new CopyOnWriteArrayList<WebSocketSession>();
  
  //방에 있는 사람들 세션 (map도 불안정함 바꿔야해 (ConcurrentHashMap 로)) 선언
  //private final Map<Long, List<WebSocketSession>> gameRoomSessions = new HashMap<Long, List<WebSocketSession>>();
  private final Map<Long, CopyOnWriteArrayList<WebSocketSession>> gameRoomSessions = new ConcurrentHashMap<Long, CopyOnWriteArrayList<WebSocketSession>>();
  
  // 특정 방의 게임상황 객체 (map도 불안정함 바꿔야해 (ConcurrentHashMap 로)) 선언
  //private final Map<Long, RoomGameState> roomGameStates  = new HashMap<Long, RoomGameState>();
  private final Map<Long, RoomGameState> roomGameStates  = new ConcurrentHashMap<Long, RoomGameState>();
  
  //대기방 유저 세션 불러오기 함수 
  public List<WebSocketSession> getWaitingRoomSessions (){
    return waitingRoomSessions;
  }
  
  //특정방 유저 세션 불러오기 함수
  public Map<Long, CopyOnWriteArrayList<WebSocketSession>> getGameRoomSessions() {
    return gameRoomSessions;
  }
  
  //해당 방 세션에서 플레이어들 정보만 뽑기(SocketChatBroadcaster에서 세션은 json으로 변환 불가)
  public List<SocketPlayerDto> getSocketPlayerDtoList(Long roomSeq){
    // 1. null이 아니라 빈 리스트를 쓰자
    List<WebSocketSession> sessionList = gameRoomSessions.get(roomSeq);
    
    // 2. 아무세션 없으면 빈 리스트 리턴
    if(sessionList.isEmpty()) {
      return Collections.emptyList();
    }
    
    // 3. 플레이어 리스트 생성 
    List<SocketPlayerDto> players = new ArrayList<SocketPlayerDto>();
    for(WebSocketSession ws : sessionList) {
      SocketPlayerDto player = (SocketPlayerDto) ws.getAttributes().get("playerDto");
      if (player != null) { // 혹시 모를 null 방지
        players.add(player);
      }
    }
    return players;
  }
  
  //게임방 상황 map 불러오기
  public Map<Long, RoomGameState> getRoomGameStates(){
    return roomGameStates;
  }
  
  /**
   * 세션에서 SocketPlayerDto의 객체자체와 속성 5가지 호출 메서드   
   * @param session
   * @return
   */
  public SocketPlayerDto getSocketPlayerDtoBySession(WebSocketSession session) {
    SocketPlayerDto socketPlayerDto = (SocketPlayerDto) session.getAttributes().get("playerDto");
    return socketPlayerDto;
  }
  
  public Long getPlayerRoomSeqFromSession(WebSocketSession session) {
    SocketPlayerDto playerDto = (SocketPlayerDto) session.getAttributes().get("playerDto");
    return playerDto.getRoomSeq();
  }
  public String getPlayerNicknameFromSession(WebSocketSession session) {
    SocketPlayerDto playerDto = (SocketPlayerDto) session.getAttributes().get("playerDto");
    return playerDto.getNickname();
  }
  public int getPlayerSeqFromSession(WebSocketSession session) {
    SocketPlayerDto playerDto = (SocketPlayerDto) session.getAttributes().get("playerDto");
    return playerDto.getPlayerSeq();
  }
  public int getPlayerGameScore(WebSocketSession session) {
    SocketPlayerDto playerDto = (SocketPlayerDto) session.getAttributes().get("playerDto");
    return playerDto.getGameScore();
  }
  public String getPlayerRole(WebSocketSession session) {
    SocketPlayerDto playerDto = (SocketPlayerDto) session.getAttributes().get("playerDto");
    return playerDto.getRole();
  }
  
}
