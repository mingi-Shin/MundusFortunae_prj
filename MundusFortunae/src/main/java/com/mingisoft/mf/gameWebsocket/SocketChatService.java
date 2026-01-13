package com.mingisoft.mf.gameWebsocket;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.mingisoft.mf.gameWebsocket.SocketPlayerDto;
import com.mingisoft.mf.gameWebsocket.SocketSessionRegistry;

@Component
public class SocketChatService {

  private final static Logger logger = LoggerFactory.getLogger(SocketChatService.class);
  
  private final SocketSessionRegistry socketSessionRegistry;
  
  public SocketChatService(SocketSessionRegistry socketSessionRegistry) {
    this.socketSessionRegistry = socketSessionRegistry;
  }
  
  /**
   * 새로운 참가자의 정보를 소켓세션에 등록  
   * @param session 세션
   * @param roomSeq 방번호 
   * @param nickname 닉네임 
   */
  public void addPlayer(WebSocketSession session, Long roomSeq, String nickname) {
    //1.roomSeq로 기존의 방 인원수 체크하고, +1이 이번 참여자의 번호 (방장은 null뜸)
    /*
     * int count = Optional.ofNullable(
     * socketSessionRegistry.getGameRoomSessions().get(roomSeq)) .map(List::size)
     * .orElse(0);
     */
    
    List <SocketPlayerDto> players = socketSessionRegistry.getSocketPlayerDtoList(roomSeq);
    int playerSeq = getNextPlayerSeq(players);
    
    String role = playerSeq == 0 ? "HOST" : "GUEST";
    
    //2.plyerDto로 만들기
    SocketPlayerDto socketPlayer = new SocketPlayerDto(roomSeq, role, playerSeq, nickname, 0);
    
    logger.info("생성된 socketPlayer : {}", socketPlayer);
    
    //3.생성된 palyerDto를 session에 등록 
    session.getAttributes().put("playerDto", socketPlayer);
    
    //4.생성된 플레이어 세션을 해당 방 세션에 등록 
    CopyOnWriteArrayList<WebSocketSession> roomPlayers = socketSessionRegistry.getGameRoomSessions().get(roomSeq); //방 가져오기 
    if(roomPlayers == null) {
      roomPlayers = new CopyOnWriteArrayList<WebSocketSession>(); //방 숫자에 맞는 룸세션이 없을 때 (방장은 없을듯?)
      roomPlayers.add(session);
      socketSessionRegistry.getGameRoomSessions().put(roomSeq, roomPlayers); //방에 플레이어 리스트 등록 
      
    } else {
      roomPlayers.add(session); //참여자들은 만들어진 방에 세션만 등록 
    }
  }
  
  /**
   * 방번호 입력후 해당 방에서 퇴장 
   * @param session
   * @param roomSeq
   */
  public void removePlayer(WebSocketSession session) {
    //채팅창, 참여자 관련 퇴장
    Long roomSeq = socketSessionRegistry.getPlayerRoomSeqFromSession(session);
    socketSessionRegistry.getGameRoomSessions().get(roomSeq).remove(session); 
    
    //게임 퇴장
    List <SocketPlayerDto> SocketPlayerDtoList = socketSessionRegistry.getSocketPlayerDtoList(roomSeq);
    SocketPlayerDto leaveUser = socketSessionRegistry.getSocketPlayerDtoBySession(session);
    SocketPlayerDtoList.remove(leaveUser);
    
    logger.info("방 퇴장 유저: {}", session.getAttributes().get("playerDto"));
  }
  
  /**
   * PlayerSeq구하는 메서드
   */
  private int getNextPlayerSeq(List<SocketPlayerDto> players) {
    if( players.isEmpty() || players == null ) {
      return 0; //방에 아무도 없으면 0번 = HOST용
    }
    
    //Set으로 수집 (0(n))
    Set<Integer> usedSeq = players.stream()
                             .map(p -> p.getPlayerSeq())
                             .collect(Collectors.toSet());
    
    //0부터 시작해서 안쓰인 가장 작은 번호 찾기
    int seq = 0;
    while (usedSeq.contains(seq)) {
      seq++;
    }
    return seq;
  }
  
  /**
   * 채팅로그를 서버에 기록할 거라면... 
   */
  
}
