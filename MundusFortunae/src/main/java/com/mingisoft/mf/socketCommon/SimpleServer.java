package com.mingisoft.mf.socketCommon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

public class SimpleServer {

  private final static Logger logger = LoggerFactory.getLogger(SimpleServer.class);
  
  public SimpleServer() {
    
    int port = 5000;
    
    try (ServerSocket serverSocket = new ServerSocket(port)) { //자동 close 인터페이스 상속, 활용문법try(())catch()
      logger.info("서버가 대기중인 포트 : {}", port);

      Socket clientSocket = serverSocket.accept(); //5000번 주소로 요청을 받아들이겠따. 라는 의미 
      logger.info("클라이언트가 연결되었습니다. : {}", clientSocket.getInetAddress());
      
      BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
      
      String message = in.readLine(); //엔터까지 한줄 
      logger.info("클라이언트로부터 받은 메시지 : {}", message);
      
      out.println("메시지를 잘 받았습니다 : " + message);
      
    } catch (IOException e) {
      logger.error("소켓 연결중 오류 발생 : {}", e);
    } 
    
  }
  
  
  
}
