package com.mingisoft.mf.game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mingisoft.mf.common.ObjectMapperSingleton;

/**
 * RoomSocketBroadcaster
 * "데이터를 전달받아, 세션들에게 뿌린다"의 기술적 역할만 담당
 */
@Component
public class SocketRoomBroadcaster {

  private final static Logger logger = LoggerFactory.getLogger(SocketRoomBroadcaster.class);
  private final ObjectMapperSingleton objectMapper;
  
  public SocketRoomBroadcaster(ObjectMapperSingleton objectMapper) {
    this.objectMapper = objectMapper;
  }
  
  //연결된 모든 소켓세션을 저장 (ArrayList는 불안정, 나중에 바꿔야함)
  private List<WebSocketSession> webSocketSessionList = new ArrayList<WebSocketSession>();
  
  /**
   * Handler가 연결/해제 시 세션 등록/삭제용으로 호출
   */
  public void addSession(WebSocketSession session) {
    webSocketSessionList.add(session);
    logger.info("클라이언트 접속 : {}" , session.getId()); //세션아이디 
    logger.info("클라이언트 Attribute : {}" , session.getAttributes()); //빈칸 
    logger.info("클라이언트 Principal : {}" , session.getPrincipal());
    logger.info("클라이언트 Uri : {}" , session.getUri()); // ws://localhost:8081/mf/chat
    logger.info("클라이언트 LocalAddress : {}" , session.getLocalAddress()); // /[0:0:0:0:0:0:0:1]:8081
    logger.info("클라이언트 RemoteAddress : {}" , session.getRemoteAddress()); // /[0:0:0:0:0:0:0:1]:65084
    logger.info("현재 웹소켓 세션 갯수 : {}", webSocketSessionList.size());
  }
  public void removeSession(WebSocketSession session) {
    webSocketSessionList.remove(session);
    logger.info("연결 종료 : {}", session.getId());
  }
  
  /**
   * 대기방 인원 몇명 
   */
  public void currentWaitingPeople() {
    Map<String, Object> roomWaitingPeople = Map.of(
      "type","roomWaitingPeople",
      "data", webSocketSessionList.size()
    );
    String jsonMsg;
    try {
      jsonMsg = objectMapper.getInstance().writeValueAsString(roomWaitingPeople); //java -> json
      
    } catch (JsonProcessingException e) {
      // 데이터가 존재하지 않아서, JSON 직렬화도 실패하면 보낼 게 없으니까 로그만 남기고 종료
      logger.info("대기중인 인원이 없음", e.getMessage(), e);
      return;
    }
    
    for (WebSocketSession s : webSocketSessionList) {
      if (!s.isOpen()) {
        continue; // 이미 끊긴 세션이면 패스
      }

      try {
        s.sendMessage(new TextMessage(jsonMsg)); //아무 문자열이면 됨, 다만 프론트에서 JSON.parse()할거니까 json으로 보내는 중 
      } catch (IOException e) {
        logger.warn("소켓에 sendMessage() 실패", e.getMessage(), e);
      }
    }
  }
  
  /**
   * 방 리스트 갱신 메서드 
   */
  public void sendRoomList(List<RoomDto> roomList) {
    //데이터를 JSON으로 변환해줘야 sendMessage의 매개변수로 대입 가능 (자바 <-> JSON : objectMapper.writeValueAsString(), JSON.parse() 
    // 1. 클라이언트와 약속한 프로토콜 형태로 포장
    Map<String, Object> roomListMap = Map.of(
        "type", "roomList",  // "room" 보단 좀 더 의미가 드러나는 이름 추천
        "data", roomList
    );

    // 2. JSON으로 변환
    String jsonMsg;
    try {
      jsonMsg = objectMapper.getInstance().writeValueAsString(roomListMap); //생성된 방 리스트를 JSON(문자열)으로 변환
      
    } catch (JsonProcessingException e) {
      // 데이터가 존재하지 않아서, JSON 직렬화도 실패하면 보낼 게 없으니까 로그만 남기고 종료
      logger.info("생성된 방이 없음", e.getMessage(), e);
      return;
    }

    // 3. 모든 세션에 브로드캐스트
    for (WebSocketSession s : webSocketSessionList) {
      if (!s.isOpen()) {
        continue; // 이미 끊긴 세션이면 패스
      }

      try {
        s.sendMessage(new TextMessage(jsonMsg));
      } catch (IOException e) {
        logger.warn("소켓에 sendMessage() 실패", e.getMessage(), e);
      }
    }
  }
  
  
  
  
}
