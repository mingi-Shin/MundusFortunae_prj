/**
 * 대기룸 웹소켓 전용 JS
 */
let socket;

/**
 * 소켓 연결
 */
function connectSocket() {
  const host = window.location.host;
  const protocol = window.location.protocol === "https:" ? "wss://" : "ws://";
  socket = new WebSocket(protocol + host + "/mf/room");

  initSocketHandlers();
}

/**
 * 소켓 이벤트 핸들러 등록
 */
function initSocketHandlers() {
  if (!socket) {
    console.error("소켓이 초기화되지 않았습니다. connectSocket() 먼저 호출하세요.");
    return;
  }

  socket.onopen = () => {
    console.log("서버와 소켓 연결 open!");
  };

  socket.onmessage = (event) => {
    // 서버에서 sendMessage()로 보낸 JSON 문자열
    const payload = JSON.parse(event.data);
    console.log("수신 payload:", payload);

    switch (payload.type) {
      case "roomWaitingPeople":
        updateWaitingPeople(payload.data);
        break;

      case "roomList":
        renderRoomList(payload.data);
        break;

      case "chat":
        appendChat(payload.data);
        break;

      default:
        console.warn("알 수 없는 타입:", payload.type, payload);
    }
  };

  socket.onclose = () => {
    console.log("연결이 종료되었습니다.");
    // 필요하면 재연결 로직도 여기서 구현 가능
  };

  socket.onerror = (err) => {
    console.error("웹소켓 에러 발생:", err);
  };
}

/**
 * 대기 인원 UI 업데이트
 * @param {number} count - 대기 인원 수
 */
function updateWaitingPeople(count) {
  const peopleEl = document.getElementById("peopleCount");
  if (!peopleEl) {
    console.warn("peopleCount 요소를 찾을 수 없습니다.");
    return;
  }

  peopleEl.textContent = count + "명";
}

/**
 * 방 리스트 UI 렌더링
 * @param {Array} roomList - 방 목록 배열
 */
function renderRoomList(roomList) {
  console.log("renderRoomList 호출, roomList:", roomList);

  const tbody = document.getElementById("roomListBody");

  if (!tbody) {
    console.error("roomListBody를 찾을 수 없습니다.");
    return;
  }

  // 1. 기존 행 초기화
  tbody.innerHTML = "";

  // 2. 방이 하나도 없을 때
  if (!roomList || roomList.length === 0 ) {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td colspan="4" class="text-center text-muted py-4">
        현재 생성된 방이 없습니다.
      </td>
    `;
    tbody.appendChild(tr);
    return;
  }

  // 3. 방 리스트로 tr 생성해서 추가
  roomList.forEach((room) => {
    console.log("room 객체:", room);

    const playerSize = room.playerList?.length ?? 0;
    const maxPlayer = room.maxPlayerCount ?? 6;

    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${room.roomSeq}</td>
      <td class="text-start">
        <div class="fw-semibold">${room.title}</div>
        <div class="small text-muted">방 번호 #${room.roomSeq}</div>
      </td>
      <td>
        <span class="badge bg-secondary">${playerSize}/${maxPlayer}</span>
      </td>
      <td>
        <button type="button"
                class="btn btn-sm btn-primary joinBtn"
                data-room-playersize="${playerSize}"
                data-room-id="${room.roomSeq}"
                data-room-title="${room.title}"
                data-bs-toggle="modal"
                data-bs-target="#joinRoomModal">
          참여
        </button>
      </td>
    `;

    tbody.appendChild(tr);
  });

  // 4. 참여 버튼 이벤트 바인딩 (필요하면)
	roomJoinModal();
	//roomJoinFormSend();
}

/**
 * 채팅 메시지 UI 처리 (향후 확장용)
 * @param {Object} chatData
 */
function appendChat(chatData) {
  // 예: { nickname: '열혈전사', message: '안녕' }
  console.log("chat 메시지 수신:", chatData);

  // 추후 채팅 영역이 생기면 여기서 DOM 추가
  // const chatBox = document.getElementById("chatBox");
  // ...
}

/**
 * 버튼 재 생성 후 이벤트 바인딩 
 */


const jsonWords = JSON.stringify({
	age : 36,
	name : "Raphael",
	isMerried : false
});
const javaWords = JSON.parse(jsonWords);
