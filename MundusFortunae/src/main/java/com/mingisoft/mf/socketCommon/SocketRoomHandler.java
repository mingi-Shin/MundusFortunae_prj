package com.mingisoft.mf.socketCommon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mingisoft.mf.common.ObjectMapperSingleton;
import com.mingisoft.mf.game.RoomDto;
import com.mingisoft.mf.game.RoomService;
import com.mingisoft.mf.game.SocketChatController;
import com.mingisoft.mf.game.SocketGameController;
import com.mingisoft.mf.game.SocketRoomController;

/**
 * 연결/메시지/종료 이벤트를 처리하는 핵심 부분입니다.
 */
@Component
public class SocketRoomHandler extends TextWebSocketHandler {

  private static final Logger logger = LoggerFactory.getLogger(SocketRoomHandler.class); 
  
  private final SocketRoomController socketRoomController;
  private final RoomService roomService;
  
  public SocketRoomHandler(SocketRoomController socketRoomController, RoomService roomService) {
    this.socketRoomController = socketRoomController; //스프링 : 아 컨테이너에 있는거 찾아 넣어줘야지 (의존성 주입 : DI)
    this.roomService = roomService;
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
  
  /**
   * 방 리스트 갱신 소켓 메서드 
   */
  public void renewalRoomList() {
    // 1. 방 목록 조회
    List<RoomDto> roomList = roomService.getAllRoomList();
    //데이터를 JSON으로 변환해줘야 sendMessage의 매개변수로 대입 가능 (자바 <-> JSON : objectMapper.writeValueAsString(), JSON.parse() 
    // 2. 클라이언트와 약속한 프로토콜 형태로 포장
    Map<String, Object> roomListMap = Map.of(
        "type", "roomList",  // "room" 보단 좀 더 의미가 드러나는 이름 추천
        "data", roomList
    );

    // 3. JSON으로 변환
    String jsonMsg;
    try {
        jsonMsg = ObjectMapperSingleton.getInstance()
                                       .writeValueAsString(roomListMap);
    } catch (JsonProcessingException e) {
        // JSON 직렬화도 실패하면 보낼 게 없으니까 로그만 남기고 종료
        e.printStackTrace(); // 나중엔 logger.error(...)로 교체 추천
        return;
    }

    // 4. 모든 세션에 브로드캐스트
    for (WebSocketSession s : webSocketSessionList) {
        if (!s.isOpen()) {
            continue; // 이미 끊긴 세션이면 패스
        }

        try {
            s.sendMessage(new TextMessage(jsonMsg));
        } catch (IOException e) {
            e.printStackTrace(); // 마찬가지로 logger.warn 정도로 처리 추천
        }
    }
    
  }
  
  
}
