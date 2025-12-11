package com.mingisoft.mf.gameWebsocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor //자동 생성자 매개변수로 
public class SocketGameService {
  
  private static final long NO_HOST = -1L; //사용자가 localStoragee 변조 가능성
  private static final long NO_NEXT_PLAYER = -1L; //끝 의미의 변수값 

  private final static Logger logger = LoggerFactory.getLogger(SocketGameService.class);
  private final SocketGameDiceRoll socketGameDiceRoll;
  private final SocketSessionRegistry socketSessionRegistry;
  
  //게임시작 준비 : 방 정보 가져와서, 플레이어를 게임순서세팅 클래스 객체에 대입
  public void setRoomGameState(Long roomSeq){
    
    //객체생성
    RoomGameState gameState = new RoomGameState();
    //게임방 번호
    gameState.setRoomSeq(roomSeq);
    //게임방 플레이어 리스트
    List <SocketPlayerDto>  playerDtoList = socketSessionRegistry.getSocketPlayerDtoList(roomSeq);
    gameState.setPlayerDtoList(playerDtoList);
    //첫턴은 방장
    int startNumber = playerDtoList.stream()
              .filter(p -> "HOST".equals(p.getRole()))
              .mapToInt(p -> p.getPlayerSeq())
              .findFirst()
              .orElseThrow(() -> new IllegalStateException("NO_HOST"));
    gameState.setCurrentTurn(startNumber);
    //턴 리미트는 방에 참여한 플레이어 수
    gameState.setMaxTurn(playerDtoList.size());
    
    socketSessionRegistry.getRoomGameStates().put(roomSeq, gameState);
    
    logger.info("게임시작버튼에 의해 게임 생성_ 방번호 : {}, gameState : {}", roomSeq, gameState);
  }
  
  
  //게임 진행 : 주사위 굴리기 
  public Map<String, Object> userRollDice(Long roomSeq, Long orderNumber, WebSocketSession session) {
    // 1. 주사위 굴리기 : 결과 a, b
    Map <String, Object> diceRollResult = socketGameDiceRoll.getResultByDefaultRoll();
    int diceA = (int)diceRollResult.get("diceA");
    int diceB = (int)diceRollResult.get("diceB");
    
    // 2. 주사위 결과 유저값에 대입
    SocketPlayerDto player = socketSessionRegistry.getSocketPlayerDtoBySession(session);
    player.setGameScore(diceA + diceB);
    
    // 3. 다음 순서 번호
    long nextOrderNumber;
    // 방 나갔을 경우를 대비해서, +1이 아니라 그 다음 존재하는 유저의 숫자를 찾기
    List <SocketPlayerDto> SocketPlayerDtoList = socketSessionRegistry.getSocketPlayerDtoList(roomSeq);
    nextOrderNumber = SocketPlayerDtoList.stream()
                        .filter( p -> p.getPlayerSeq() > orderNumber)
                        .mapToLong(p -> p.getPlayerSeq())
                        .findFirst()
                        .orElse(NO_NEXT_PLAYER);
    
    
    //중간 퇴장자를 대비해서 게임방 상황을 매번 갱신 
    socketSessionRegistry.getRoomGameStates().get(roomSeq).setPlayerDtoList(SocketPlayerDtoList);
    socketSessionRegistry.getRoomGameStates().get(roomSeq).setCurrentTurn(nextOrderNumber); //다음사람 seq를 currentTurn으로 지정
    socketSessionRegistry.getRoomGameStates().get(roomSeq).setMaxTurn(SocketPlayerDtoList.size());
    
    Map<String, Object> diceResult = new HashMap<String, Object>();
    diceResult.put("diceA", diceA);
    diceResult.put("diceB", diceB);
    
    // 주사위 점수가 갱신된 참여자리스트 UI용 객체
    List<SocketPlayerDto> playerDtoList = socketSessionRegistry.getSocketPlayerDtoList(roomSeq);
    diceResult.put("playerDtoList", playerDtoList);
    
    return diceResult;
  }
  
}
