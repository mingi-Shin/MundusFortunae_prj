package com.mingisoft.mf.socketCommon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.SocketHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mingisoft.mf.game.RoomDto;
import com.mingisoft.mf.game.RoomService;
import com.mingisoft.mf.game.SocketChatBroadcaster;
import com.mingisoft.mf.game.SocketChatService;
import com.mingisoft.mf.game.SocketGameBroadcaster;
import com.mingisoft.mf.game.SocketRoomBroadcaster;

/**
 * 연결/메시지/종료 이벤트를 처리하는 핵심 부분입니다.
 */
@Component
public class SocketChatHandler extends TextWebSocketHandler {

  private static final Logger logger = LoggerFactory.getLogger(SocketChatHandler.class);
  
  private final SocketChatBroadcaster socketChatBroadcaster;
  private final ObjectMapper objectMapper;
  private final SocketChatService socketChatService;
  private final SocketSessionRegistry socketSessionRegistry;
  
  private final RoomService roomService;
  private final SocketRoomBroadcaster socketRoomBroadcaster;
  
  
  
  public SocketChatHandler(SocketChatBroadcaster socketChatBroadcaster, ObjectMapper objectMapper, SocketChatService socketChatService, SocketSessionRegistry socketSessionRegistry
      , RoomService roomServic, SocketRoomBroadcaster socketRoomBroadcaster) {
    this.objectMapper = objectMapper;
    this.socketChatBroadcaster = socketChatBroadcaster; //스프링 : 아 컨테이너에 있는거 찾아 넣어줘야지 (의존성 주입 : DI)
    this.socketChatService = socketChatService;
    this.socketSessionRegistry = socketSessionRegistry;
    this.roomService = roomServic;
    this.socketRoomBroadcaster = socketRoomBroadcaster;
  }
  
  /** 
   * WebSocket 협상이 성공하고, WebSocket 연결이 열려서 사용할 준비가 되었을 때 호출된다.
   * 서버와 클라이언트 간의 WebSocket 핸드셰이크(협상 과정)가 정상적으로 완료된 후, 실제로 양방향 통신이 가능한 상태가 되면 실행되는 메서드나 이벤트
   */
  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    
  }
  
  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {// socket.send() 메시지 
    //입구에서 json메시지 파싱해서 필요정보만 전달 
    JsonNode node = objectMapper.readTree(message.getPayload()); //json -> java
    String type = node.path("type").asText(null); //NPE피하기 용도 : 없으면 null반환 
    switch (type) {
    //방참여 메시지 수신
    case "addPlayer": {
      Long roomSeq = node.path("roomSeq").asLong();
      String nickname = node.path("nickname").asText(null);
      socketChatService.addPlayer(session, roomSeq, nickname); //서비스를 통해 방번호, 닉네임 세션을 저장 
      socketChatBroadcaster.addPlayerBroadcaster(session, roomSeq, nickname);
      break;
    }
    //방탈퇴 메시지 수신 
    case "removePlayer" : {
      session.getAttributes().put("manualClose", true);
      
      socketChatService.removePlayer(session); //세션에서 삭제 
      socketChatBroadcaster.removePlayerBroadcaster(session); //퇴장 브로드캐스팅
      break;
    }
    
    //메시지 수신
    case "chat" : {
      String msg = node.path("msg").asText();
      socketChatBroadcaster.sendChatBroadcaster(session, msg);
      
      break;
    }
    
    default:
      throw new IllegalArgumentException("Unexpected value: " + type);
    }
    
    
    
    
  }
  
  //비정상 접속 종료일 때다. 
  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    
    // 정상 퇴장이면 중복처리 방지
    Boolean manual = (Boolean) session.getAttributes().get("manualClose");
    if (manual != null && manual) {
        logger.info("정상 퇴장 → afterConnectionClosed 로직은 스킵합니다.");
        return;
    }
    
    logger.info("비정상 퇴장 발생, 정상 처리된것처럼 방 유저들에게 브로드캐스팅");
    socketChatService.removePlayer(session); //세션에서 삭제 
    socketChatBroadcaster.removePlayerBroadcaster(session); //퇴장 브로드캐스팅 
    
    
    /**
     * removePlayerFromServerRoom와 SocketRoomHandler에서 가져왔다. 
     * 비정상 방퇴장시 fetch를 탈수 없기때문에 기존의 코드를 재사용해야한다. 그런데.. 
     * 로직 구성에 맞지 않는 코드이지만, websocketRoom 페이지 렌더링을 도메인로직에 의존해서 생성하고 세션에 뿌리는 걸로 코드를 짜놔서 
     * 지금와서 바꾸기가 쉽지않다. 
     * 나중에 리팩토링 해야겠다. 일단 되게 만들자 
     */
    SocketPlayerDto playerDto = (SocketPlayerDto) session.getAttributes().get("playerDto");
    roomService.leavePlayerFromRoom(playerDto.getRoomSeq(), playerDto.getNickname());
    roomService.deleteEmptyRoom(playerDto.getRoomSeq()); //만약 빈방되면 리스트에서 방 삭제 
    Collection<RoomDto> roomList = roomService.getAllRooms();
    socketRoomBroadcaster.sendRoomList(roomList);
    
  }
  
  
}
