package com.mingisoft.mf.socketCommon;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.mingisoft.mf.common.ObjectMapperSingleton;
import com.mingisoft.mf.game.RoomService;
import com.mingisoft.mf.game.SocketChatBroadcaster;
import com.mingisoft.mf.game.SocketGameBroadcaster;
import com.mingisoft.mf.game.SocketRoomBroadcaster;

/**
 * 연결/메시지/종료 이벤트를 처리하는 핵심 부분입니다.
 */
@Component
public class SocketChatHandler extends TextWebSocketHandler {

  private final SocketChatBroadcaster socketChatBroadcaster;
  private final ObjectMapperSingleton objectMapperSingleton;
  private final RoomService roomService;
  
  public SocketChatHandler(SocketChatBroadcaster socketChatBroadcaster, ObjectMapperSingleton objectMapperSingleton, RoomService roomService) {
    this.socketChatBroadcaster = socketChatBroadcaster; //스프링 : 아 컨테이너에 있는거 찾아 넣어줘야지 (의존성 주입 : DI)
    this.objectMapperSingleton = objectMapperSingleton;
    this.roomService = roomService;
  }
  
  /** 
   * WebSocket 협상이 성공하고, WebSocket 연결이 열려서 사용할 준비가 되었을 때 호출된다.
   * 서버와 클라이언트 간의 WebSocket 핸드셰이크(협상 과정)가 정상적으로 완료된 후, 실제로 양방향 통신이 가능한 상태가 되면 실행되는 메서드나 이벤트
   */
  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    
  }
  
  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    // 클라이언트가 socket.send()한 메시지 
    /**
     * JSON 문자열을 파싱해서 node라는 JSON 트리 객체로 만들어라 라는 뜻 = 이제부터 node는 JSON 최상위 객체를 가리키고 있음
     */
    JsonNode node = objectMapperSingleton.getInstance().readTree(message.getPayload()); //json -> java
    String type = node.path("type").asText(null); //NPE피하기 용도 : 없으면 null반환 
    switch (type) {
    case "playerUI": {
      
    }
    case "chat" : {
      
    }
    default:
      throw new IllegalArgumentException("Unexpected value: " + type);
    }
    
    
    
    
  }
  
  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    socketChatBroadcaster.removeSession(session);
  }
  
  
}
