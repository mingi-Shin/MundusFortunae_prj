package com.mingisoft.mf.gameWebsocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mingisoft.mf.gameApplication.PlayerDto;
import com.mingisoft.mf.gameWebsocket.SocketPlayerDto;
import com.mingisoft.mf.gameWebsocket.SocketSessionRegistry;

/**
 * 여기는 방별로 세션 관리  사용자 방 추가는 controller가 아니라 Handler에서 처리해야함 
 */
@Component
public class SocketChatBroadcaster {

  private final static Logger logger = LoggerFactory.getLogger(SocketChatBroadcaster.class);
  
  //스프링에서 기본으로 ObjectMapper를 bean으로 들고있어서 바로 주입이 가능 
  private final ObjectMapper objectMapper;
  private final SocketSessionRegistry socketSessionRegistry;
  private final SocketChatService socketChatService;
  
  public SocketChatBroadcaster(ObjectMapper objectMapper, SocketSessionRegistry socketSessionRegistry, SocketChatService socketChatService) {
    this.objectMapper = objectMapper;
    this.socketSessionRegistry = socketSessionRegistry;
    this.socketChatService = socketChatService;
  }
  
  //플레이어 참가 메서드 
  public void addPlayerBroadcaster(WebSocketSession session, Long roomSeq, String nickname) {
    List<SocketPlayerDto> playerDtoList = socketSessionRegistry.getSocketPlayerDtoList(roomSeq);
    int playerSeq = playerDtoList.stream()
                      .filter(p -> nickname.equals(p.getNickname()))
                      .mapToInt(p -> p.getPlayerSeq())
                      .findFirst()
                      .orElseThrow(() -> new IllegalArgumentException("해당 nickname 없음 : " + nickname));
    //1. 전달할 정보를 MAP으로 포장 
    Map<String, Object> roomPlayersInfo = Map.of(
      "type", "addPlayer",
      "data", playerDtoList,
      "nickname", nickname,
      "playerSeq", playerSeq
    );
    
    //2.데이터를 JSON으로 변환
    String jsonMsg;
    try {
      jsonMsg = objectMapper.writeValueAsString(roomPlayersInfo);
    } catch (JsonProcessingException e) {
      // 데이터가 존재하지 않아서, JSON 직렬화도 실패하면 보낼 게 없으니까 로그만 남기고 종료
      logger.info("roomPlayersInfo를 json화 실패 : {}", e.getMessage(), e);
      return;
    }
    
    //3.방 유저들에게 브로드캐스트 
    List<WebSocketSession> roomPlayersSession = socketSessionRegistry.getGameRoomSessions().get(roomSeq);
    for(WebSocketSession ws : roomPlayersSession) {
      if(!ws.isOpen()) {
        continue; //이미 끊긴 세션은 패스 
      }
      
      try {
        ws.sendMessage(new TextMessage(jsonMsg));
      } catch (Exception e) {
        logger.warn("소켓에 sendMessage() 실패 : {}", e.getMessage(), e);
      }
    }
    
  }
  
  // 플레이어 퇴장 메서드 
  public void removePlayerBroadcaster(WebSocketSession session) {
    //1. 퇴장유저 세션에서 닉네임, 방번호 호출 
    String nickname = socketSessionRegistry.getPlayerNicknameFromSession(session);
    Long roomSeq = socketSessionRegistry.getPlayerRoomSeqFromSession(session);
    
    //2. 갱신된 방의 유저 정보 호출 및 map으로 패키징
    List <SocketPlayerDto>  playerDtoList = socketSessionRegistry.getSocketPlayerDtoList(roomSeq);
    Map<String, Object> roomPlayersInfo = Map.of(
        "type", "removePlayer",
        "data", playerDtoList,
        "nickname", nickname //퇴장유저 닉네임 
        );
    
    //3. 패키징된 정보를 json화 
    String jsonMsg;
    try {
      jsonMsg = objectMapper.writeValueAsString(roomPlayersInfo);
    } catch (JsonProcessingException e) {
      logger.info("playerDtoList를 json화 실패 : {}", e.getMessage(), e);
      return;
    }
    
    //4. 해당 방의 세션들에게 갱신된 플레이어 정보 브로드캐스팅 
    List <WebSocketSession> roomPlayersSession = socketSessionRegistry.getGameRoomSessions().get(roomSeq); //갱신된 세션 리스트 
    for(WebSocketSession s : roomPlayersSession) {
      if(!s.isOpen()) {
        continue;
      }
      
      try {
        s.sendMessage(new TextMessage(jsonMsg));
      } catch (Exception e) {
        logger.warn("소켓에 sendMessage() 실패 : {}", e.getMessage(), e);
      }
    }
    
  }
  
  //채팅창 브로드캐스트
  public void sendChatBroadcaster(WebSocketSession session, String msg) {
    //소켓세션에서 정보 
    String nickname = socketSessionRegistry.getPlayerNicknameFromSession(session);
    Long roomSeq = socketSessionRegistry.getPlayerRoomSeqFromSession(session);
    
    //데이터 Map으로 패키징
    Map<String, Object> object = Map.of(
        "type" , "chat",
        "data" , msg,
        "nickname", nickname
        );
    
    String jsonMsg = null;
    try {
      jsonMsg = objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      logger.warn("데이터 writeValueAsString() 실패 : {}", e.getMessage(), e);
    }
    
    //방 친구들에게 보내기
    List <WebSocketSession> roomPlayers = socketSessionRegistry.getGameRoomSessions().get(roomSeq);
    for(WebSocketSession s : roomPlayers) {
      if(!s.isOpen()) {
        continue;
      }
      try {
        s.sendMessage(new TextMessage(jsonMsg));
      } catch (IOException e) {
        logger.warn("소켓에 sendMessage() 실패 : {}", e.getMessage(), e);
      }
    }
  }
  
}
