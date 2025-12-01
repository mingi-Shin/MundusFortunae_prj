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

import com.mingisoft.mf.game.SocketChatBroadcaster;
import com.mingisoft.mf.game.SocketGameBroadcaster;
import com.mingisoft.mf.game.SocketRoomBroadcaster;

/**
 * 연결/메시지/종료 이벤트를 처리하는 핵심 부분입니다.
 */
@Component
public class SocketChatHandler extends TextWebSocketHandler {

  private final SocketChatBroadcaster socketChatBroadcaster;
  
  public SocketChatHandler(SocketChatBroadcaster socketChatBroadcaster) {
    this.socketChatBroadcaster = socketChatBroadcaster; //스프링 : 아 컨테이너에 있는거 찾아 넣어줘야지 (의존성 주입 : DI)
  }
  
  /** 
   * WebSocket 협상이 성공하고, WebSocket 연결이 열려서 사용할 준비가 되었을 때 호출된다.
   * 서버와 클라이언트 간의 WebSocket 핸드셰이크(협상 과정)가 정상적으로 완료된 후, 실제로 양방향 통신이 가능한 상태가 되면 실행되는 메서드나 이벤트
   */
  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    //참여자를 해당하는 방 세션에 저장 
    socketChatBroadcaster.addSession(session);
    
  }
  
  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    // 클라이언트가 보낸 메시지 
    String payload = message.getPayload();
    //logger.info("클라이언트로 부터 데이터 수신: {}", payload);
    
    // 연결되어있는 모든 소켓세션들에게 메시지를 브로드캐스트!!  
    for(WebSocketSession s : webSocketSessionList) {
      s.sendMessage(new TextMessage(payload));
    }
  }
  
  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    webSocketSessionList.remove(session);
  }
  
  
}
