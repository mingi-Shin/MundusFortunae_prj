package com.mingisoft.mf.game;

import org.springframework.stereotype.Controller;

@Controller
public class SocketRoomController {

  private final SocketRoomService socketRoomService;
  
  public SocketRoomController(SocketRoomService socketRoomService) {
    this.socketRoomService = socketRoomService;
  }
  
  
}
