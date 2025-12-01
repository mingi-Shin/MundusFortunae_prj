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
import com.mingisoft.mf.game.SocketGameController;
import com.mingisoft.mf.game.SocketRoomBroadcaster;

/**
 * 연결/메시지/종료 이벤트를 처리하는 핵심 부분입니다.
 */
@Component
public class SocketGameHandler extends TextWebSocketHandler {

  private static final Logger logger = LoggerFactory.getLogger(SocketGameHandler.class); 
  
  private final SocketGameController socketGameController;
  
  public SocketGameHandler(SocketGameController socketGameController) {
    this.socketGameController = socketGameController;
  }
  
  
  //연결된 모든 소켓세션을 저장
  private static List<WebSocketSession> webSocketSessionList = new ArrayList<WebSocketSession>();
  
  /** 
   * WebSocket 협상이 성공하고, WebSocket 연결이 열려서 사용할 준비가 되었을 때 호출된다.
   * 서버와 클라이언트 간의 WebSocket 핸드셰이크(협상 과정)가 정상적으로 완료된 후, 실제로 양방향 통신이 가능한 상태가 되면 실행되는 메서드나 이벤트
   */
  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    webSocketSessionList.add(session);
    logger.info("클라이언트 접속 : {}" , session.getId()); //세션아이디 
    logger.info("클라이언트 Attribute : {}" , session.getAttributes()); //빈칸 
    logger.info("클라이언트 Principal : {}" , session.getPrincipal());
    logger.info("클라이언트 Uri : {}" , session.getUri()); // ws://localhost:8081/mf/chat
    logger.info("클라이언트 LocalAddress : {}" , session.getLocalAddress()); // /[0:0:0:0:0:0:0:1]:8081
    logger.info("클라이언트 RemoteAddress : {}" , session.getRemoteAddress()); // /[0:0:0:0:0:0:0:1]:65084
    
    logger.info("현재 웹소켓 세션 갯수 : {}", webSocketSessionList.size());
    
  }
  
  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    // 클라이언트가 보낸 메시지 
    String payload = message.getPayload();
    logger.info("클라이언트로 부터 데이터 수신: {}", payload);
    
    // 연결되어있는 모든 소켓세션들에게 메시지를 브로드캐스트!!  
    for(WebSocketSession s : webSocketSessionList) {
      s.sendMessage(new TextMessage(payload));
    }
  }
  
  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    webSocketSessionList.remove(session);
    logger.info("연결 종료 : {}", session.getId());
  }
  
  
}
