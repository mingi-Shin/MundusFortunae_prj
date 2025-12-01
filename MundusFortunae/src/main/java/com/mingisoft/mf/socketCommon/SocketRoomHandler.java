package com.mingisoft.mf.socketCommon;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.mingisoft.mf.game.RoomDto;
import com.mingisoft.mf.game.RoomService;
import com.mingisoft.mf.game.SocketRoomBroadcaster;

/**
 * 연결/메시지/종료 이벤트를 처리하는 핵심 부분입니다.
 */
@Component
public class SocketRoomHandler extends TextWebSocketHandler {

  private final SocketRoomBroadcaster socketRoomBroadcaster;
  private final RoomService roomService;
  
  public SocketRoomHandler(SocketRoomBroadcaster socketRoomBroadcaster, RoomService roomService) {
    this.socketRoomBroadcaster = socketRoomBroadcaster; //스프링 : 아 컨테이너에 있는거 찾아 넣어줘야지 (의존성 주입 : DI)
    this.roomService = roomService;
  }
  
  /** 
   * WebSocket 협상이 성공하고, WebSocket 연결이 열려서 사용할 준비가 되었을 때 호출된다.
   * 서버와 클라이언트 간의 WebSocket 핸드셰이크(협상 과정)가 정상적으로 완료된 후, 실제로 양방향 통신이 가능한 상태가 되면 실행되는 메서드나 이벤트
   */
  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    socketRoomBroadcaster.addSession(session);
    socketRoomBroadcaster.currentWaitingPeople();
    
    // 새 접속자에게도 최신 리스트(이미 http통신으로 주고 있지만, 렌더링 되는 그 짧은 간격을 메꾸고자 한다면 넣어도 된다) 
    List<RoomDto> roomList = roomService.getAllRoomList();
    socketRoomBroadcaster.sendRoomList(roomList);
    
  }
  
  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    
  }
  
  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    socketRoomBroadcaster.removeSession(session);
    socketRoomBroadcaster.currentWaitingPeople();
  }
  
  
}
