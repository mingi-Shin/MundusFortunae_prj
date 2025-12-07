/**
 * 게임 채팅 + 참여자 목록 + 나가기 소켓 
 */
let chatSocket;
let roomSeq; 

function connectChatSocket(){
	const host = window.location.host;
	const protocol = window.location.protocol === "https://" ? "wss://" : "ws://";
	chatSocket = new WebSocket(protocol + host + "/chat");
	
	roomSeq = document.getElementById("roomInfo").dataset.roomSeq;

	initChatSocketHandlers(); 
	
	//sendRoomSeqToHandler(); //메시지 전송은 .onopen() 후로 해야해 

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
	  //console.log(roomSeq + "번 방 채팅소켓 연결 open!");
		
		addPlayerInfoToHandler(); //open한 뒤에 해 
	};
	
	chatSocket.onmessage = (event) => {
		const payload = JSON.parse(event.data); //이거 굉장히 중요 event는 data,type,target등으로 나뉘어져 있음
		const type = payload.type; // addPlayer or removePlayer
		
		//플레이어가 방에 참여했을 때
		if(type === "addPlayer"){
			const data = payload.data;
			const nickname = payload.nickname;
			//ui수정
			renewalPlayersList(roomSeq, data, data.length);
			//채팅창 입장 알림
			alertPlayerInOutLog(payload.nickname, "in");
			
		}
		//플레이어가 방에서 퇴장했을 때
		if(type === "removePlayer"){
			const data = payload.data;
			const nickname = payload.nickname;
			//ui수정 
			renewalPlayersList(roomSeq, data, data.length);
			//채팅창 퇴장 알림
			alertPlayerInOutLog(payload.nickname, "out");
			
		}
		//채팅메시지 UI  
		if(type === "chat"){
			const msg = payload.data;
			const nickname = payload.nickname;
			//채팅창 UI수정 
			receiveMsg(nickname, msg);
			
		}
		
	}
	
	chatSocket.onclose = () => {
		
	};

	chatSocket.onerror = (err) => {
	  console.error("채팅 소켓 에러 발생:", err);
	};
	
}

/* 방 접속시 유저 참여 메시지 날리기*/
function addPlayerInfoToHandler(){
	const payload = {
		type : "addPlayer",
		roomSeq : Number(roomSeq),
		nickname : localStorage.getItem("myNickname")
	}
	chatSocket.send(JSON.stringify(payload));
}

/* 유저 퇴장 메시지 날리기 1 */
function removePlayerInfoToHandler(){
	const out = confirm("게임방을 나가시겠습니까?");
	if(!out === true){
		return;
	}
	
	const payload = {
		type : "removePlayer",
		roomSeq : Number(roomSeq),
		nickname : localStorage.getItem("myNickname")
	}
	// 1) 서버 WebSocket Handler에게 알림
	chatSocket.send(JSON.stringify(payload));
	
	// 2) DB 갱신(fetch)
	removePlayerFromSererRoom(payload.roomSeq, payload.nickname);
	
	// 3) ---------------------------
	// 정상 퇴장이므로 socket 직접 닫기!
	// ---------------------------
	chatSocket.close();
	
}

/* 유저 퇴장 메시지 날리기 2 : fetch  */
async function removePlayerFromSererRoom(roomSeq, nickname){
	const url = contextPath + "webSocket/leave";
	try {
		const response = await fetch(url, {
			method : "POST",
			headers : {"Content-Type" : "application/json"},
			body : JSON.stringify({
				roomSeq : roomSeq,
				nickname : nickname
			})
		});
		
		const result = await response.json();
		
		if(response.ok){
			alert(result.message);
			window.location.href = contextPath + "webSocket/websocketRoom";
		} else {
			alert(result.message)
		}
		
	} catch (error){
		console.error("통신 오류:", error);
		alert("서버와의 통신 중 오류가 발생했습니다.");
	}
	
	
}

/* 참여자에 따른 UI수정 */
function renewalPlayersList(roomSeq, data, playerCount){
	
	const myNickname = localStorage.getItem("myNickname");
	document.getElementById("roomInfo").textContent = `${roomSeq}번방 참여자 (${playerCount}/6)`;
	
	const currentBox = document.getElementById("currentPlayerBox");   // 내 정보 영역
	const listBox = document.getElementById("otherPlayersList");      // 다른 플레이어 목록

	currentBox.innerHTML = "";
	listBox.innerHTML = "";

	data.forEach(player => {

		const { nickname, playerSeq, gameScore } = player;

		// 나 자신
		if (nickname === myNickname) {

			currentBox.innerHTML = `
				<div class="d-flex align-items-center justify-content-between">
				  <div class="d-flex align-items-center gap-2">
				    <div class="player-avatar bg-primary">P${playerSeq}</div>
				    <div>
				      <div class="fw-bold small">${nickname} (나)</div>
				      <div class="text-muted" style="font-size: 0.75rem;">점수: ${gameScore}</div>
				    </div>
				  </div>
				</div>
			`;

		} else {
			// 다른 플레이어들
			const div = document.createElement("div");
			div.className = "p-2 border rounded";

			div.innerHTML = `
				<div class="d-flex align-items-center gap-2">
				  <div class="player-avatar bg-secondary">P${playerSeq}</div>
				  <div>
				    <div class="fw-bold small">${nickname}</div>
				    <div class="text-muted" style="font-size: 0.75rem;">점수: ${gameScore}</div>
				  </div>
				</div>
			`;

			listBox.appendChild(div);
		}
	});
}

/** 채팅창 유저 입장알림 */
function alertPlayerInOutLog(nickname, status){
	let chatLog = document.getElementById("chatLog");
	
	// 1. <div class="chat-message"> 생성
	const wrapper = document.createElement("div");
	wrapper.className = "chat-message d-flex align-items-center";
	
	// 2. <span class="badge ...">닉네임</span>
	const badge = document.createElement("span");
	badge.className = "badge bg-secondary me-1";
	badge.textContent = nickname;
	
	// 3. <span>메시지 내용</span>
	const text = document.createElement("span");
	text.className = "fst-italic text-info"
	if(status === "in"){
		text.textContent = "님이 여정에 참여합니다.";
	} else if(status === "out"){
		text.textContent = "님이 퇴장하셨습니다.";
	}
	
	// 4. wrapper 안에 badge, text 넣기
	wrapper.appendChild(badge);
	wrapper.appendChild(text);
	
	// 5. 마지막으로 chatLog에 이 wrapper를 추가(append)하기
	chatLog.appendChild(wrapper);
	
	// 6. 스크롤 맨 아래로 자동 이동 ?? 이게 머지
	const scrollArea = chatLog.parentElement;
	scrollArea.scrollTop = scrollArea.scrollHeight;
}


/** 채팅 발신 */
function sendMsg(){
	const msg = document.getElementById("chatInput").value;
	if(msg.length > 60){
		alert("60자이상 입력하실 수 없습니다.");
		document.getElementById("chatInput").value = "";
		return;
	}
	
	const payload = {
		type : "chat",
		roomSeq : Number(roomSeq),
		nickname : localStorage.getItem("myNickname"),
		msg : msg
	}
	
	const sendData = JSON.stringify(payload);
	
	chatSocket.send(sendData);
	
	document.getElementById("chatInput").value = "";
}

/* 채팅 수신 UI */
function receiveMsg(nickname, msg){
	const myNickname = localStorage.getItem("myNickname");
	let chatLog = document.getElementById("chatLog");
	
	const wrapper = document.createElement("div");
	wrapper.className = "chat-message";
	
	const badge = document.createElement("span");
	badge.className = "badge bg-secondary me-1";
	if(myNickname && myNickname === nickname ){
		badge.textContent = "나";
	} else {
		badge.textContent = nickname;
	}
	
	const messageSpan = document.createElement("span");
	messageSpan.textContent = msg;
	
	wrapper.appendChild(badge);
	wrapper.appendChild(messageSpan);
	chatLog.appendChild(wrapper);
	
	// 6. 스크롤 맨 아래로 자동 이동
	const scrollArea = chatLog.parentElement;
	scrollArea.scrollTop = scrollArea.scrollHeight;
}

/* 엔터 누르면 채팅전송 편의기능 */
function enterToMsg(){
	document.getElementById("chatInput").addEventListener("keydown", (e) => {
		if(e.key === "Enter"){
			e.preventDefault();
			sendMsg();
		}
	});
}

