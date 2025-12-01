/**
 * 	대기룸 웹소켓 전용 js
 */
let socket;

/**
 * 소켓 연결 
 */
function connectSocket(){
	const host = window.location.host;
	const protocol = window.location.protocol === "https:" ? "wss://" : "ws://";
	socket = new WebSocket(protocol + host + "/mf/room");
}

function openSocket(){
	socket.onopen = () => {
		
	}
}

