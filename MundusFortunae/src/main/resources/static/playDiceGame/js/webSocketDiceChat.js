/**
 * 게임 채팅 + 참여자 목록 + 나가기 소켓 
 */
let chatSocket;
let roomSeq; 
let myPlayerSeq;
let gameStarted = false;

function connectChatSocket(){
	const host = window.location.host;
	const protocol = window.location.protocol === "https:" ? "wss://" : "ws://";
	chatSocket = new WebSocket(protocol + host + "/chat");
	
	roomSeq = document.getElementById("roomInfo").dataset.roomSeq;

	initChatSocketHandlers(); 
	
	//sendRoomSeqToHandler(); //메시지 전송은 .onopen() 후로 해야해 
	
	enterToMsg();

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
			//local닉네임 === addPlayer닉네임일 때, playerSeq결정
			if(localStorage.getItem("myNickname") === payload.nickname){
				myPlayerSeq = payload.playerSeq;
				//console.log("nickname : " + payload.nickname  + " / playerSeq : " + playerSeq);
			}
			//참여자 목록, 게임버튼 ui수정
			renewalPlayersListForIn(roomSeq, data, data.length);
			//채팅창 입장 알림
			alertPlayerInOutLog(payload.nickname, "in");
			
		}
		//플레이어가 방에서 퇴장했을 때
		if(type === "removePlayer"){
			const data = payload.data; //data:playerDtoList, nickname:nickname
			//참여자 목록, 게임버튼 ui수정 
			renewalPlayersListForOut(roomSeq, data, data.length);
			//채팅창 퇴장 알림
			alertPlayerInOutLog(payload.nickname, "out");
			//중간이탈자 발생, 게임 중지.. 단! 이미 게임을 시작한 후일때만 
			if(gameStarted === true){
				stopGame(payload.nickname);
			}
			
		}
		//채팅메시지 UI  
		if(type === "chat"){
			const msg = payload.data;
			const nickname = payload.nickname;
			//채팅창 UI수정 
			receiveMsg(nickname, msg);
			
		}
		//게임 준비완료 UI
		if(type === "gameReady"){
			gameStarted = true; //게임시작시 방 상태값 변환
			
			const gameState = payload.data;
			const nextTurnNickname = payload.nextTurnNickname;
			drawGameStartUI(gameState, nextTurnNickname);
			
		}
		//주사위 굴리고 수신한 메시지
		if(type === "rollResult"){
			const gameState = payload.data;
			const playerDtoList = payload.data.playerDtoList;
			const currentNickname = payload.currentNickname; //방금 던진 유저 닉넴
			const nextTurnNickname = payload.nextTurnNickname; //다음차례
			const diceA = payload.diceResult.diceA;
			const diceB = payload.diceResult.diceB;
			
			//
			if(gameState.currentTurn !== -1 && nextTurnNickname !== "noBody"){
				//주사위 결과를 5초동안 극적으로 보여주고, 다음사람 굴리기 UI그리기 
				drawGameResultUI(gameState, nextTurnNickname, diceA, diceB);
				
				setTimeout(() => {
					writeGameLog(currentNickname, diceA, diceB)
				  drawGameStartUI(gameState, nextTurnNickname); //다음사람 주사위굴리기 버튼 그리기 
					renewalPlayersScore(roomSeq, playerDtoList, playerDtoList.length); //점수갱신
					
				}, 5000);
				
			//-1 과 'noBody'는 서버에서, 유저가 모두 다 주사위를 굴렸을 때 내리는 값 
			} else if(gameState.currentTurn === -1 || nextTurnNickname === "noBody") {
				drawGameResultUI(gameState, nextTurnNickname, diceA, diceB);
				
				setTimeout(() => {
					writeGameLog(currentNickname, diceA, diceB)
					renewalPlayersScore(roomSeq, playerDtoList, playerDtoList.length);
				}, 5000);
				
				//결과 정리후 5초뒤에 끝!
				setTimeout(() => {
					//alert("게임 끝!");
					gameEnd();
				}, 5000);
				//게임끝 ------------------------------------ 여기도 수정해야함 정식적으로 끝내게 
				
				return;
			}
			
			
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
			//alert(result.message);
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
function renewalPlayersListForIn(roomSeq, data, playerCount){
	
	const myNickname = localStorage.getItem("myNickname");
	document.getElementById("roomInfo").textContent = `${roomSeq}번방 참여자 (${playerCount}/6)`;
	
	const currentBox = document.getElementById("currentPlayerBox");   // 내 정보 영역
	const listBox = document.getElementById("otherPlayersList");      // 다른 플레이어 목록
	const gameStartBtnSection = document.querySelector(".game-start-btn-section"); //게임 시작 버튼 (방장만)

	currentBox.innerHTML = "";
	listBox.innerHTML = "";
	gameStartBtnSection.innerHTML = "";
	
	// ✅ 1) 내가 HOST인지 먼저 계산
	const me = data.find(p => p.nickname === myNickname);
	const isMeHost = me && me.role === "HOST";
	

	// ✅ 2) 플레이어 UI 렌더
	data.forEach(player => {
	  const { nickname, playerSeq, gameScore, role } = player;

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
	    const div = document.createElement("div");
	    div.className = "p-2 border rounded";
	    div.innerHTML = `
	      <div class="d-flex align-items-center gap-2">
	        <div class="player-avatar bg-secondary">P${playerSeq}</div>
	        <div>
	          <div class="fw-bold small">${nickname} [${role}]</div>
	          <div class="text-muted" style="font-size: 0.75rem;">점수: ${gameScore}</div>
	        </div>
	      </div>
	    `;
	    listBox.appendChild(div);
	  }
	});

	// ✅ 3) 버튼은 루프 밖에서 “한 번만” 그리기, forEach에 들어가면 안됨
	if (isMeHost) {
	  gameStartBtnSection.innerHTML = `
	    <button id="game-start-btn"
	            class="btn btn-primary btn-lg px-4 px-md-5 shadow-sm"
	            onclick="startDicegame()">
	      <i class="bi bi-controller"></i> Game Start!
	    </button>
	  `;
	} else {
	  gameStartBtnSection.innerHTML = `
	    <button id="game-start-btn"
	            class="btn btn-outline-secondary btn-lg px-4 px-md-5 shadow-sm"
	            disabled>
	      <i class="bi bi-controller"></i> Game Waiting...
	    </button>
	  `;
	}
}
function renewalPlayersListForOut(roomSeq, data, playerCount){
	
	const myNickname = localStorage.getItem("myNickname");
	document.getElementById("roomInfo").textContent = `${roomSeq}번방 참여자 (${playerCount}/6)`;
	
	const currentBox = document.getElementById("currentPlayerBox");   // 내 정보 영역
	const listBox = document.getElementById("otherPlayersList");      // 다른 플레이어 목록

	currentBox.innerHTML = "";
	listBox.innerHTML = "";
	
	// ✅ 1) 내가 HOST인지 먼저 계산
	const me = data.find(p => p.nickname === myNickname);
	

	// ✅ 2) 플레이어 UI 렌더
	data.forEach(player => {
	  const { nickname, playerSeq, gameScore, role } = player;

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
	    const div = document.createElement("div");
	    div.className = "p-2 border rounded";
	    div.innerHTML = `
	      <div class="d-flex align-items-center gap-2">
	        <div class="player-avatar bg-secondary">P${playerSeq}</div>
	        <div>
	          <div class="fw-bold small">${nickname} [${role}]</div>
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
	document.getElementById("chatInput").addEventListener("keypress", (e) => { //keypress는 레거시이므로 나중에 keydown으로 바꿀것 
		if(e.key === "Enter"){
			e.preventDefault();
			sendMsg();
		}
	});
}

/*--------------------------------------------------------- 게임 관련 함수 ---------------------------------------------------------*/
let gameScore;
function startDicegame() {
	
	// 1. 게임시작 핸들러에 알림 
	const payload = {
		type : "game",
		roomSeq : Number(roomSeq),
		msg  : "ready"
	}
	const sendData = JSON.stringify(payload);
	chatSocket.send(sendData);
}

function drawGameStartUI(gameState, nextTurnNickname){

	if(!Array.isArray(gameState.playerDtoList)){
		console.log("gameState.playerDtoList가 Array가 아님");
		return;
	}
	
	/* 턴 안내 span */
	const roundSpan = document.querySelector(".round-span");
	const turnSpan = document.querySelector(".turn-span");
	const gameTip = document.querySelector(".game-tip");
	const current = Number(gameState.currentTurn ?? 0); 
	const total = gameState.playerDtoList?.length ?? 0;
	roundSpan.textContent = `진행중 : ${current + 1}/${total}`; //진행중 UI에는 +1 해줘야..
	turnSpan.textContent = `턴 : ${nextTurnNickname}`;
	
	/* 게임버튼 UI 차례에 따라 변화 */	
	const gameStartBtn = document.querySelector(".game-start-btn-section");
	
	if(!gameStartBtn){
		console.log("gameStartBtn가 없어요!!");
		return;
	}
	
	//내 차례일 때 
	if(myPlayerSeq === current){
		gameStartBtn.innerHTML = "";
		gameStartBtn.innerHTML = `
			<button id="dice-roll-btn" 
							class="btn btn-primary btn-lg px-4 px-md-5 shadow-sm" 
							onclick="rollDice(this)" 
							data-order-number="${gameState.currentTurn}" 
							data-nick-name="${nextTurnNickname}">
				<i class="bi bi-dice-5"></i> 주사위 굴리기
			</button>
			<button class="btn btn-outline-secondary btn-lg px-3 px-md-4 shadow-sm" onclick="alert('준비중')">
			  여신소환
			</button>
		`;
		
		gameTip.textContent = "";
		gameTip.textContent = "당신의 차례입니다.";
		
	}
	
	//내 차례가 아닐 때 
	if(myPlayerSeq !== current){
		gameStartBtn.innerHTML = "";
		gameStartBtn.innerHTML = `
			<button class="btn btn-outline-secondary btn-lg px-4 px-md-5 shadow-sm" disabled>
			  <i class="bi bi-controller"></i> [${nextTurnNickname}] 순서입니다.
			</button>
		`;
		
		gameTip.textContent = "";
		gameTip.textContent = "모두가 숨죽여 지켜봅니다!";
	}
	
}

/* 주사위 굴리기 버튼 클릭 : 서버전달  */
function rollDice(btn){
	//1.다시 못 누르게 막기
  btn.disabled = true;
	
	//2.텍스트 수정
	btn.innerHTML = `
	  <span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
	  주사위를 굴리는 중...
	`;
	
	const orderNumber = btn.dataset.orderNumber; //현재 차례의 유저정보가 담겨잇음 
	
	const payload = {
				type : "game",
				roomSeq : Number(roomSeq),
				orderNumber : orderNumber,
				msg  : "roll"
			}
	const jsonMsg = JSON.stringify(payload);
	chatSocket.send(jsonMsg);
}

/* 서버에서 주사위 던진 결과값 받아서 UI 그리기 */
function drawGameResultUI(gameState, nextTurnNickname, diceA, diceB){
	
	showRollingDice(diceA, diceB);
}
//클로저 (안쪽 함수가 바깥함수의 변수를 사용중) 활용한 주사위 굴리기 UI
function showRollingDice(diceA, diceB){
	//일단 합계 표시부터 
	document.getElementById("dice-sum-value").textContent = "??";
	
	const dice1 = document.getElementById('dice1');
	const dice2 = document.getElementById('dice2');
	const gameStartBtnSection = document.querySelector(".game-start-btn-section"); //게임 시작 버튼 (방장만)

	dice1.innerHTML = "";
	dice1.innerHTML = `<div class="dice-face"></div>`;
	dice2.innerHTML = "";
	dice2.innerHTML = `<div class="dice-face"></div>`;

	// 각 숫자(1~6)에 대한 점 위치 (0~8: 위에서부터 가로 3칸씩)
	const pipPatterns = {
	  1: [4],
	  2: [0, 8],
	  3: [0, 4, 8],
	  4: [0, 2, 6, 8],
	  5: [0, 2, 4, 6, 8],
	  6: [0, 2, 3, 5, 6, 8],
	};

	function renderDie(diceBox, value) {
	  const face = diceBox.querySelector('.dice-face');
	  face.innerHTML = '';

	  // 굴리는 애니메이션 재시작
	  diceBox.classList.remove('rolling');
	  void diceBox.offsetWidth;        // reflow 트릭
	  diceBox.classList.add('rolling');

	  // 6이면 황금색, 아니면 기본 흰색
	  diceBox.classList.toggle('dice-gold', value === 6);

	  // 3x3 셀을 돌면서 점 찍기
	  for (let i = 0; i < 9; i++) {
	    const cell = document.createElement('div');
	    cell.className = 'dice-cell';

	    if (pipPatterns[value].includes(i)) {
	      const dot = document.createElement('div');
	      dot.className = 'dice-dot';
	      cell.appendChild(dot);
	    }

	    face.appendChild(cell);
	  }
	}

	// 주사위 하나씩 값에따라 UI변경
	renderDie(dice1, diceA);
	if(diceA === 6 ){
		gameStartBtnSection.innerHTML = "";
		gameStartBtnSection.innerHTML = `
			<button id="dice-roll-btn" 
							class="btn btn-primary btn-lg px-4 px-md-5 shadow-sm"
							style="pointer-events: none; cursor: default;"
							aria-disabled="true"   
				<i class="bi bi-emoji-surprise"></i> 아♡ 행운의여신이여
			</button>
		`;
	}
	
	setTimeout(() => {
		renderDie(dice2, diceB);
		if(diceA === 6 && diceB === 6 ){
			gameStartBtnSection.innerHTML = "";
			gameStartBtnSection.innerHTML = `
				<button id="dice-roll-btn" 
								class="btn btn-outline-warning btn-lg px-4 px-md-5 shadow-sm"
								style="pointer-events: none; cursor: default;"
								aria-disabled="true"  
					<i class="bi bi-emoji-kiss"></i> 믿고 있었다고 젠장-!!
				</button>
			`;
		} else if(diceA !== 6 && diceB === 6 ){
			gameStartBtnSection.innerHTML = `
				<button id="dice-roll-btn" 
								class="btn btn-primary btn-lg px-4 px-md-5 shadow-sm"
								style="pointer-events: none; cursor: default;"
								aria-disabled="true"   
					<i class="bi bi-emoji-surprise"></i> 아♡ 행운의여신이여
				</button>
			`;
		}
	}, 2500);

	// 페이지 처음 들어왔을 때 초기 상태(1,1) 한 번 렌더링
	//updateDiceUI(1, 1);
}


/* 주사위 굴린 후에 결과값을 반영한 참여자 명단의 점수 갱신 + 게임 로그 */
function renewalPlayersScore(roomSeq, data, playerCount){
	
	const myNickname = localStorage.getItem("myNickname");
	document.getElementById("roomInfo").textContent = `${roomSeq}번방 참여자 (${playerCount}/6)`;
	
	const currentBox = document.getElementById("currentPlayerBox");   // 내 정보 영역
	const listBox = document.getElementById("otherPlayersList");      // 다른 플레이어 목록
	const gameStartBtnSection = document.querySelector(".game-start-btn-section"); //게임 시작 버튼 (방장만)

	currentBox.innerHTML = "";
	listBox.innerHTML = "";

	// ✅ 1) 플레이어 UI 렌더
	data.forEach(player => {
	  const { nickname, playerSeq, gameScore, role } = player;

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
	    const div = document.createElement("div");
	    div.className = "p-2 border rounded";
	    div.innerHTML = `
	      <div class="d-flex align-items-center gap-2">
	        <div class="player-avatar bg-secondary">P${playerSeq}</div>
	        <div>
	          <div class="fw-bold small">${nickname} [${role}]</div>
	          <div class="text-muted" style="font-size: 0.75rem;">점수: ${gameScore}</div>
	        </div>
	      </div>
	    `;
	    listBox.appendChild(div);
	  }
	});
}

/* 주사위 굴린 후 게임 로그에 결과를 기록 */
function writeGameLog(nextTurnNickname, diceA, diceB) {
  const today = new Date();

  // padStart -> 문자열 전용 메서드 : 예)최소길이 2자리, 길이가 부족하면 0을 넣어라.
  const hours = String(today.getHours()).padStart(2, "0");
  const minutes = String(today.getMinutes()).padStart(2, "0");
  const nowTime = `${hours}:${minutes}`;

  // ✅ gameLog 아이디로 가져와야 함 (chatLog 아님!)
  const gameLog = document.getElementById("gameLog");
  if (!gameLog) {
    console.error("#gameLog 요소를 찾을 수 없습니다.");
    return;
  }

  // 바깥 래퍼 div
  const wrapper = document.createElement("div");
  wrapper.className = "p-2 bg-light rounded log-item";

  // 시간 뱃지 span
  const badge = document.createElement("span");
  badge.className = "badge bg-info me-2";
  badge.textContent = nowTime;

  // 닉네임 strong
  const nameStrong = document.createElement("strong");
  nameStrong.textContent = nextTurnNickname;

  // 합계 strong (파란색)
  const sum = diceA + diceB;
  const sumStrong = document.createElement("strong");
  sumStrong.className = "text-primary";
  sumStrong.textContent = sum;

  // 조립: "<badge>[시간]</badge> <strong>닉네임</strong>이(가) 주사위를 굴려 <strong class='text-primary'>10</strong>이 나왔습니다."
  wrapper.appendChild(badge);
  wrapper.appendChild(nameStrong);
  wrapper.appendChild(document.createTextNode("이(가) 주사위를 굴려 "));
  wrapper.appendChild(sumStrong);
  wrapper.appendChild(document.createTextNode("이 나왔습니다."));

  // 로그에 추가 (맨 아래에 추가)
  gameLog.appendChild(wrapper);

  // 필요하면 스크롤 맨 아래로 내리기
  const scrollArea = gameLog.parentElement;
  if (scrollArea) {
    scrollArea.scrollTop = scrollArea.scrollHeight;
  }
	
	//합계 표시
	document.getElementById("dice-sum-value").textContent = sum;
}

/* 게임 끝, 게임 결과 모달창 지원 */
function gameEnd(){
	const gameStartBtnSection = document.querySelector(".game-start-btn-section"); //게임 시작 버튼 (방장만)
	const gameTip = document.querySelector(".game-tip");

	gameStartBtnSection.innerHTML = "";
	gameStartBtnSection.innerHTML = `
		<button id="dice-roll-btn" 
						class="btn btn-primary btn-lg px-4 px-md-5 shadow-sm" 
						onclick="alert('준비중입니다.')" 
			<i class="bi bi-clipboard-data"></i> 결과보기
		</button>
	`;
	
	gameTip.textContent = "";
	gameTip.innerHTML = `<span class="text-dnager">게임이 끝났습니다. 퇴장하셔도 됩니다.</span>`;
}

/* 이탈자 발생. 게임 중지 */
function stopGame(nickname){

	const gameStartBtn = document.querySelector(".game-start-btn-section");
	const gameTip = document.querySelector(".game-tip");
	gameStartBtn.innerHTML = "";
	gameStartBtn.innerHTML = `
	  <button id="dice-roll-btn" 
	          class="btn btn-danger btn-lg px-4 px-md-5 shadow-sm" 
						style="pointer-events: none; cursor: default;"
	    <i class="bi bi-stop-circle"></i> 게임 중지
	  </button>
	`;

	gameTip.textContent = "";
	gameTip.textContent = `${nickname}님이 탈주하여 게임을 중지합니다.`;
}
