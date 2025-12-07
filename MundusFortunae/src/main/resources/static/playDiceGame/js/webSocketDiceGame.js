/**
 *  게임 다이스 + 게임 로그 소켓 
 */
let gameSocket;

function connectGameSocket(){
	const host = window.location.host;
	const protocol = window.location.protocol === "https:" ? "wss://" : "ws://";
	gameSocket = new WebSocket(protocol + host + "/game");
	
	initGameSocketHandlers();
}

/**
 * 소켓 이벤트 핸들러 등록
 */
function initGameSocketHandlers(){
	if(!gameSocket){
		console.error("게임 소켓이 초기화 되지 않았습니다. connectSocket() 먼저 호출해주세요.");
		return;
	}
	
	gameSocket.onopen = () => {
	  console.log("서버와 채팅소켓 연결 open!");
	};
	
	gameSocket.onmessage = (event) => { //chat 제외 게임만 
		const myNickname = localStorage.getItem("myNickname");
		
	}
	
	
	gameSocket.onclose = () => {
	  console.log("게임 소켓 연결이 종료되었습니다.");
	  // 필요하면 재연결 로직도 여기서 구현 가능
	};

	gameSocket.onerror = (err) => {
	  console.error("게임 소켓 에러 발생:", err);
	};
	
}