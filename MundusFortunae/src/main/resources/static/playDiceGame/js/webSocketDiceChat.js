/**
 * 게임 채팅 + 참여자 목록 + 나가기 소켓 
 */
let chatSocket;
const roomSeq = document.getElementById("roomSeq").dataset.roomSeq;

function connectChatSocket(){
	const host = window.location.host;
	const protocol = window.location.protocol === "https://" ? "wss://" : "ws://";

	chatSocket = new WebSocket(protocol + host + "/mf/chat");

	sendRoomSeqToHandler();

	initChatSocketHandlers();
	
}

/**
 * 소켓 이벤트 핸들러 등록
 */
function initChatSocketHandlers(){
	if(!chatSocket){
		console.error("채팅소켓이 초기화 되지 않았습니다. connectSocket() 먼저 호출해주세요.");
		return;
	}
	
	chatSocket.onopen = () => {
	  console.log(roomSeq + "번 방 채팅소켓 연결 open!");
	};
	
	chatSocket.onmessage = (event) => {
		
	}
	
	
	chatSocket.onclose = () => {
	  console.log("채팅 소켓 연결이 종료되었습니다.");
	  // 필요하면 재연결 로직도 여기서 구현 가능
	};

	chatSocket.onerror = (err) => {
	  console.error("채팅 소켓 에러 발생:", err);
	};
	
}
/* roomSeq를 Handler에게 던지기*/
function sendRoomSeqToHandler(){
	const payload = {
		type : "playerUI",
		roomSeq : Number(roomSeq),
		nickname : localStorage.getItem("myNickname") //이건 그냥 필요할까봐 
	}
	
	chatSocket.send(JSON.stringify(payload));
}

