package com.mingisoft.mf.socketCommon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

public class SimpleClient {
  
  private final static Logger logger = LoggerFactory.getLogger(SimpleClient.class);

  public SimpleClient() {
    
    String serverAddress = "localhost";
    int port = 5000;
    /**
     * 자동 close 인터페이스 상속, 활용문법try((socket = new socket))catch()
     * finally에서 close 할필요가 없음 
     */
    //소켓생성 (주소, 포트)
    try (Socket socket = new Socket(serverAddress, port)) { 
      
      PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
      BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      
      String message = "안녕하세요 서버!";
      out.print(message);
      logger.info("서버로 보낸 메시지 : {}", message);
      
      String response = in.readLine();
      logger.info("서버로부터 받은 응답 : {}", response);
      
    } catch (IOException e) {
      logger.error("소켓 연결 통신중 오류 발생 : {}", e);
    }
  }
}
