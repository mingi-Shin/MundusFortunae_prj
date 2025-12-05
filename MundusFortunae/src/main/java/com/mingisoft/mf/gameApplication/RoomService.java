package com.mingisoft.mf.gameApplication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RoomService {
  
  private final static Logger logger = LoggerFactory.getLogger(RoomService.class);  
  
      
  /**
   * 스토리 만들기 -> 리팩토링 요소 
   */
  //방번호 생성기(@Service같은게 스프링 컨테이너에서 싱글톤으로 관리되므로, 여기다 생성하는게 역할상 맞음.)
  //private final AtomicLong roomNumberGenerator = new AtomicLong(0L);
  private Long roomNumberGenerator = 0L; //Service싱글톤에서 다루므로 전역변수처럼 동작 
  
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
  
  //다른곳에서 필요할수 있어 
  public Collection<RoomDto> getAllRooms(){
    return rooms.values(); // 리스트 형태로 반환됨
  }
  
  public RoomDto getRoom(Long roomSeq){
    return rooms.get(roomSeq);
  }
  
  public boolean isFullRoom(Long roomSeq) {
    RoomDto room = getRoom(roomSeq);
    return room.getMaxPlayerCount() == room.getPlayerList().size();
  }
  
  public boolean isEmptyRoom(Long roomSeq) {
    RoomDto room = this.getRoom(roomSeq);
    return room.getPlayerList().isEmpty();
  }
  
  //------------------------------------------------------------------------------------
  
  /**
   * 방 생성 메서드 
   * @param roomTitle 방제목 
   * @param roomPassword 방비번
   * @param nickname 호스트명 
   * @return
   */
  public RoomDto createGameRoom(String roomTitle, String roomPassword, String nickname){

    //1. 방장생성
    PlayerDto roomHost = new PlayerDto();
    roomHost.setRoomSeq(roomNumberGenerator);
    roomHost.setPlayerSeq(0);
    roomHost.setNickname(nickname);
    roomHost.setRole("HOST");
    
    //2. 플레이어 리스트 구성
    List<PlayerDto> playerList = new ArrayList<PlayerDto>();
    playerList.add(roomHost);
    
    //3. 방 번호 발급 (스레드 안전)
    //long roomSeq = roomNumberGenerator.getAndIncrement();
    
    //4. RoomDto 생성 (builder 사용)
    RoomDto newRoom = RoomDto.builder()
                          .roomSeq(roomNumberGenerator)
                          .title(roomTitle)
                          .password(roomPassword)
                          .maxPlayerCount(6) // 기본값 할당중이라 생략도 가능 
                          .playerList(playerList)
                          .build();
    
    //5. 방 메모리에 업로드
    rooms.put(roomNumberGenerator, newRoom);

    // 방번호 ++
    nextRoomNumber();
    
    logger.info("생성된 방 정보 : {} / 방장 정보 : {}",  newRoom, roomHost);
    logger.info("현재 생성된 방 갯수 : {}", rooms.size());
    
    return newRoom;
  }
  
  /**
   * 방 유저 추가 메서드 
   */
  public void addPlayerToRoom(Long roomSeq, String nickname) {
    RoomDto room = this.getRoom(roomSeq);
    
    PlayerDto newPlayer = new PlayerDto();
    newPlayer.setRoomSeq(roomSeq);
    newPlayer.setPlayerSeq(room.getPlayerList().size()); //유저번호는 1번부터 시작 
    newPlayer.setNickname(nickname);
    newPlayer.setRole("USER");
    
    room.getPlayerList().add(newPlayer);
    logger.info("새로운 유저 접속.. 방번호 : {}, 유저번호: {}, 닉네임 : {}", roomSeq, room.getPlayerList().size(), nickname);
  }
  
  /**
   * 방 유저 탈퇴 메서드
   */
  public void leavePlayerFromRoom(Long roomSeq, String nickname) {
    RoomDto room = this.getRoom(roomSeq);
    List <PlayerDto> players = room.getPlayerList();
    players.removeIf(p -> nickname.equals(p.getNickname()));
    logger.info("퇴장 처리된 인원 : {}", nickname);
    logger.info("남은 인원 : {}", room.getPlayerList());
  }
  
  /**
   * 방에 유저 없으면 방 삭제 메서드
   */
  public void deleteEmptyRoom(Long roomSeq) {
    if(isEmptyRoom(roomSeq)) {
      logger.info("빈방 확인, 삭제처리 : {}", roomSeq);
      rooms.remove(roomSeq);
    }
  }
  
  
  
}
