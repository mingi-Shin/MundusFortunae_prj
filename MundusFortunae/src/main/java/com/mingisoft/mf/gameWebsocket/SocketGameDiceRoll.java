package com.mingisoft.mf.gameWebsocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Component;

@Component
public class SocketGameDiceRoll {
  
  //주사위 두개 나올수 있는 숫자 배열 / 재선언 못하게 final 선언 
  private final int[] DICEA;
  private final int[] DICEB;
  private final Random random;
  
  public SocketGameDiceRoll() {
    this.DICEA = new int[] {1, 2, 3, 4, 5, 6};
    this.DICEB = new int[] {1, 2, 3, 4, 5, 6};
    this.random = new Random();
  }

  //주사위 게임 메서드 (default)
  public Map<String, Object> getResultByDefaultRoll() {
    Map<String, Object> DiceMap = new HashMap<String, Object>();
    
    int idxA = random.nextInt(DICEA.length);
    int idxB = random.nextInt(DICEB.length);
    int resultA = DICEA[idxA];
    int resultB = DICEB[idxB];
    
    DiceMap.put("diceA", resultA);
    DiceMap.put("diceB", resultB);
    
    return DiceMap;
  }
  
  //주사위 게임 메서드 (축복 : 높은 숫자 확률업 -> 14, 15, 16, 17, 18, 20)  
  public Map<String, Object> getResultByBuffRoll() {
    Map<String, Object> DiceMap = new HashMap<String, Object>();
    
    int idxA;
    int idxB;
    int rangeA = random.nextInt(100) + 1; // 1 ~ 100
    if(rangeA >= 1 && rangeA < 15) {
      idxA = 1;
    } else if (rangeA >= 15 && rangeA < 30) {
      idxA = 2;
    } else if(rangeA >= 30 && rangeA < 46) {
      idxA = 3;
    } else if(rangeA >= 46 && rangeA < 63) {
      idxA = 4;
    } else if(rangeA >= 63 && rangeA < 81) {
      idxA = 5;
    } else {
      idxA = 6;
    }
    int rangeB = random.nextInt(100) + 1; // 1 ~ 100
    if(rangeB >= 1 && rangeB < 15) {
      idxB = 1;
    } else if (rangeB >= 15 && rangeB < 30) {
      idxB = 2;
    } else if(rangeB >= 30 && rangeB < 46) {
      idxB = 3;
    } else if(rangeB >= 46 && rangeB < 63) {
      idxB = 4;
    } else if(rangeB >= 63 && rangeB < 81) {
      idxB = 5;
    } else {
      idxB = 6;
    }
    
    int resultA = DICEA[idxA];
    int resultB = DICEB[idxB];
    
    DiceMap.put("diceA", resultA);
    DiceMap.put("diceB", resultB);
    
    return DiceMap;
  }
  
  
  
  //주사위 게임 메서드 (저주 : 낮은 숫자 확률업)
  
  
}
