package com.mingisoft.mf.game;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mingisoft.mf.common.ObjectMapperSingleton;

/**
 * RoomSocketBroadcaster
 * "이 DTO를 저 세션들에게 뿌린다"의 기술적 역할만 담당
 */
@Component
public class SocketRoomBroadcaster {
  
  private final static Logger logger = LoggerFactory.getLogger(SocketRoomBroadcaster.class);
  private final ObjectMapperSingleton objectMapper;
  
  public SocketRoomBroadcaster(ObjectMapperSingleton objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * 방 리스트 갱신 소켓 메서드 
   */
  public void sendRoomList(List<WebSocketSession> webSocketSessionList, List<RoomDto> roomList) {
    //데이터를 JSON으로 변환해줘야 sendMessage의 매개변수로 대입 가능 (자바 <-> JSON : objectMapper.writeValueAsString(), JSON.parse() 
    // 1. 클라이언트와 약속한 프로토콜 형태로 포장
    Map<String, Object> roomListMap = Map.of(
        "type", "roomList",  // "room" 보단 좀 더 의미가 드러나는 이름 추천
        "data", roomList
    );

    // 2. JSON으로 변환
    String jsonMsg; //생성된 방 리스트를 JSON(문자열)으로 변환한 객체 
    try {
      jsonMsg = ObjectMapperSingleton.getInstance()
                                       .writeValueAsString(roomListMap);
    } catch (JsonProcessingException e) {
      // JSON 직렬화도 실패하면 보낼 게 없으니까 로그만 남기고 종료
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
        e.printStackTrace(); // 마찬가지로 logger.warn 정도로 처리 추천
        logger.info("소켓에 sendMessage() 실패", e.getMessage(), e);
      }
    }
  }
  
  
}
