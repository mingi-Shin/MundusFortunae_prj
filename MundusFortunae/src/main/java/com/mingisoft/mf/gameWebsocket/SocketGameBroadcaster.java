package com.mingisoft.mf.gameWebsocket;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SocketGameBroadcaster {
  
  private final static Logger logger = LoggerFactory.getLogger(SocketGameBroadcaster.class);

  private final ObjectMapper objectMapper;
  private final SocketSessionRegistry socketSessionRegistry;

  //게임 시작 브로드캐스트 
  public void setGamePlayerOrder(WebSocketSession session) {
    //1.전달할 정보 세팅
    long roomSeq = socketSessionRegistry.getPlayerRoomSeqFromSession(session);
    RoomGameState roomGameState = socketSessionRegistry.getRoomGameStates().get(roomSeq);
    String  nextTurnNickname = roomGameState.getNextTurnNickname();
    
    //2.정보를 MAP으로 포장
    Map<String, Object> gameStateInfo = Map.of(
        "type", "gameReady",
        "data", roomGameState,
        "nextTurnNickname", nextTurnNickname
        );
    
    //3.JSON으로 변환 : try_catch
    String jsonMsg;
    try {
      jsonMsg = objectMapper.writeValueAsString(gameStateInfo);
    } catch (JsonProcessingException e) {
      logger.info("gameStateInfo를 json화 실패 : {}", e.getMessage(), e);
      return;
    }
    
    //4.방 유저들에게 브로드캐스트
    List <WebSocketSession> players = socketSessionRegistry.getGameRoomSessions().get(roomSeq);
    for(WebSocketSession ws : players) {
      if(!ws.isOpen()) {
        continue;
      }
      
      try {
        ws.sendMessage(new TextMessage(jsonMsg));
      } catch (IOException e) {
        logger.warn("소켓에 sendMessage() 실패 : {}", e.getMessage(), e);
      }
    }
    
  }
  
  public void userRollDice(Map<String, Object> diceResult, WebSocketSession session) {
    //1.전달할 정보 세팅
    long roomSeq = socketSessionRegistry.getPlayerRoomSeqFromSession(session);
    RoomGameState roomGameState = socketSessionRegistry.getRoomGameStates().get(roomSeq);
    String currentNickname = socketSessionRegistry.getPlayerNicknameFromSession(session);
    String nextTurnNickname = roomGameState.getNextTurnNickname();
    
    List<SocketPlayerDto> playerDtoList = socketSessionRegistry.getSocketPlayerDtoList(roomSeq);
    
    //2.정보를 MAP으로 포장
    Map<String, Object> gameStateInfo = Map.of(
        "type", "rollResult",
        "data", roomGameState,
        "nextTurnNickname", nextTurnNickname,
        "currentNickname", currentNickname,
        "diceResult", diceResult
        );
    
    //3.JSON으로 변환 : try_catch
    String jsonMsg;
    try {
      jsonMsg = objectMapper.writeValueAsString(gameStateInfo);
    } catch (JsonProcessingException e) {
      logger.info("gameStateInfo를 json화 실패 : {}", e.getMessage(), e);
      return;
    }
    
    //4.방 유저들에게 브로드캐스트
    List <WebSocketSession> players = socketSessionRegistry.getGameRoomSessions().get(roomSeq);
    for(WebSocketSession ws : players) {
      if(!ws.isOpen()) {
        continue;
      }
      
      try {
        ws.sendMessage(new TextMessage(jsonMsg));
      } catch (IOException e) {
        logger.warn("소켓에 sendMessage() 실패 : {}", e.getMessage(), e);
      }
    }
    
  }
  
}
