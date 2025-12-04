package com.mingisoft.mf.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.mingisoft.mf.socketCommon.SocketPlayerDto;
import com.mingisoft.mf.socketCommon.SocketSessionRegistry;

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
    int count = Optional.ofNullable(
                              socketSessionRegistry.getGameRoomSessions().get(roomSeq))
                              .map(List::size)
                              .orElse(0);
    int playerOrderNumber = count +1;
    
    //2.plyerDto로 만들기
    SocketPlayerDto socketPlayer = new SocketPlayerDto(roomSeq, playerOrderNumber, nickname, 0);
    
    //3.생성된 palyerDto를 session에 등록 
    session.getAttributes().put("playerDto", socketPlayer);
    
    //4.생성된 플레이어 세션을 해당 방 세션에 등록 
    List <WebSocketSession> roomPlayers = socketSessionRegistry.getGameRoomSessions().get(roomSeq); //방 가져오기 
    if(roomPlayers == null) {
      roomPlayers = new ArrayList<WebSocketSession>(); //방 숫자에 맞는 룸세션이 없을 때 (방장은 없을듯?)
      roomPlayers.add(session);
      socketSessionRegistry.getGameRoomSessions().put(roomSeq, roomPlayers); //방에 플레이어 리스트 등록 
      
    } else {
      roomPlayers.add(session); //참여자들은 만들어진 방에 세션만 등록 
    }
  }
  
  /**
   * 세션에서 SocketPlayerDto의 속성 뽑기 4가지 
   * @param session
   * @return
   */
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
  
  /**
   * 방번호 입력후 해당 방에서 퇴장 
   * @param session
   * @param roomSeq
   */
  public void removePlayer(WebSocketSession session) {
    Long roomSeq = getPlayerRoomSeqFromSession(session);
    socketSessionRegistry.getGameRoomSessions().get(roomSeq).remove(session); 
    logger.info("방 퇴장 유저: {}", session.getAttributes().get("playerDto"));
  }
  
  /**
   * 채팅로그를 서버에 기록할 거라면... 
   */
  
}
