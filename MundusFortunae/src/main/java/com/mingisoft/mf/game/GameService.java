package com.mingisoft.mf.game;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

@Service
public class GameService {
  
  /**
   * 스토리 만들기 -> 리팩토링 요소 
   */
  //방번호 생성기(@Service같은게 스프링 컨테이너에서 싱글톤으로 관리되므로, 여기다 생성하는게 역할상 맞음.)
  //private final AtomicLong roomNumberGenerator = new AtomicLong(0L);
  private Long roomNumberGenerator; //Service싱글톤에서 다루므로 전역변수ㅜ로 활용가능 
  
  //메모리에 올라갈 방 정보
  //private final Map<Long, RoomDto> rooms = new ConcurrentHashMap<Long, RoomDto>();
  private final Map<Long, RoomDto> rooms = new HashMap<Long, RoomDto>();
  
  /*
   * public Long nextRoomNumber() { return roomNumberGenerator.getAndIncrement();
   * // 스레드 안전 }
   */
  private Long nextRoomNumber() {
    return roomNumberGenerator++;
  }
  //------------------------------------------------------------------------------------
  
  public <T> RoomDto<T> createGameRoom(String roomTitle, String roomPassword, String nickname){
    RoomDto<T> newRoom = new RoomDto<T>();
    
    
    return newRoom;
  }
  
  
}
