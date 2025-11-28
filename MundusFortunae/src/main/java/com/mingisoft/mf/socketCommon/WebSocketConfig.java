package com.mingisoft.mf.socketCommon;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * 핸들러 등록 : 웹소켓을 활성화
 */
@Configuration
@EnableWebSocket
//@WebSocketMessageBrokerConfigurer -> STOMP쓰는 고레벨 어노테이션 
public class WebSocketConfig implements WebSocketConfigurer {
  
  private final SocketRoomHandler socketRoomHandler; //소켓 룸 핸들러
  private final SocketChatHandler socketChatHandler; //소켓 채팅 핸들러 
  private final SocketGameHandler socketGameHandler; //소켓 게임 핸들러 
  
  public WebSocketConfig(SocketRoomHandler socketRoomHandler, SocketChatHandler socketChatHandler, SocketGameHandler socketGameHandler) {
    this.socketRoomHandler = socketRoomHandler; //스프링 : 아 컨테이너에 있는거 찾아 넣어줘야지 (의존성 주입 : DI)
    this.socketChatHandler = socketChatHandler; 
    this.socketGameHandler = socketGameHandler;
  }

  /**
   * 도메인별로 나누는게 정석 
   */
  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    //웹소켓 경로와 그 경로를 처리할 핸들러(=서버 쪽 처리기)를 등록하는 코드, 그리고 허용할 클라이언트url (루트+/chat) 
    registry.addHandler(socketRoomHandler , "/room")
      .setAllowedOrigins("*"); 
    registry.addHandler(socketChatHandler, "/chat")
      .setAllowedOrigins("*");
    registry.addHandler(socketGameHandler, "/game")
      .setAllowedOrigins("*"); //Origin = 프로토콜 + 도메인 + 포트 -> 프론트의 포트가 백엔드와 다를때 의미있는 코드
  }
  
  

}
