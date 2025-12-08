package com.mingisoft.mf.gameWebsocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SocketGameService {

  private final static Logger logger = LoggerFactory.getLogger(SocketGameService.class);
  private final SocketGameDiceRoll socketGameDiceRoll;
  
  public SocketGameService(SocketGameDiceRoll socketGameDiceRoll) {
    this.socketGameDiceRoll = socketGameDiceRoll;
  }
  
  
  
}
