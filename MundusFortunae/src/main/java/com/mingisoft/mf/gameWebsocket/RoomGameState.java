package com.mingisoft.mf.gameWebsocket;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomGameState {
  
  private static final String NO_BODY = "noBody"; //끝 의미의 변수값 
 
  private long roomSeq;
  private long currentTurn;                                
  private int maxTurn;
  private List<SocketPlayerDto> playerDtoList;
  
  public String getNextTurnNickname() {
    String currentTurnNickname = playerDtoList.stream()
                    .filter( p -> p.getPlayerSeq() == currentTurn)
                    .map( p -> p.getNickname())
                    .findFirst()
                    .orElse(NO_BODY);
    return currentTurnNickname;
  }
  
}
