package com.mingisoft.mf.socket;

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

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    //웹소켓 경로와 그 경로를 처리할 핸들러(=서버 쪽 처리기)를 등록하는 코드, 그리고 허용할 클라이언트url (루트+/chat) 
    registry.addHandler(new SocketRoomHandler() , "/chat")
      .setAllowedOrigins("*"); //Origin = 프로토콜 + 도메인 + 포트 -> 프론트의 포트가 백엔드와 다를때 의미있는 코드     
  }
  
  

}
