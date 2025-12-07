//js에서 만원방 참여버튼 비활성화 (소켓에서도 동일기능 작성)
function changeJoinBtn(){
	document.querySelectorAll('.joinBtn').forEach( (btn) => {
		if(btn.dataset.roomPlayersize === "6"){ //dataset은 문자열
			btn.disabled = true;
		}
	});
}

//방 참여하기 모달창 오픈 자동완성 
function roomJoinModal(){
	document.querySelectorAll(".joinBtn").forEach( (btn) => {
		btn.addEventListener("click", async (e) => {
			const roomSeq = e.currentTarget.getAttribute("data-room-id"); 
			const roomTitle = e.currentTarget.getAttribute("data-room-title"); //자주 활용하면 편리할 듯 
			document.getElementById('room-join-id').value = roomSeq;	  			
			document.getElementById('room-join-title').value = roomTitle;	  			
		});
	});		
} 
//일반 유저 방 참여 함수 
function roomJoinFormSend(){
	document.getElementById("room-join-btn").addEventListener("click", async () => {

		try {
			const roomSeq = document.getElementById("room-join-id").value;
			const roomPassword = document.getElementById("room-join-password").value;
			const nickname = document.getElementById("room-join-nickname").value.trim();
			
			if(!roomPassword || !nickname){
				alert("공백은 허용되지 않습니다.");
				return;
			}
			
			const url = contextPath + "webSocket/room/" + roomSeq + "/join";
			const response = await fetch(url , {
				method : "POST",
				headers : {"Content-Type":"application/json"},
				body : JSON.stringify({
					roomSeq : roomSeq,
					roomPassword : roomPassword,
					nickname : nickname
				})
			})
			const result = await response.json();
			
			if(response.ok){
				localStorage.setItem("myNickname", nickname);
				const url = contextPath + "webSocket/room/" + roomSeq; 
				window.location.href=url;
				
			} else {
				alert(result.message);
				return;
			}
			
		} catch(err) {
			console.error("통신 오류:", err);
			alert("서버와의 통신 중 오류가 발생했습니다.");
		}
	});
}


//방 생성하기 
function createRoom(){
  
  document.getElementById("room-create-btn").addEventListener("click", async () => {
	  const roomTitle = document.getElementById("room-create-title").value.trim();
	  const roomPassword = document.getElementById("room-create-password").value.trim();
	  const nickname = document.getElementById("room-create-nickname").value.trim();
	  
	  if(!roomTitle || !roomPassword || !nickname){
		  alert("공백은 허용되지 않습니다.");
		 	return;
	  }
		
		const roomSettingArray = [roomTitle, roomPassword, nickname]; //배열 : 반복문 돌리기, 순위/로그 기록 등
		const roomSettingInstance = {roomTitle, roomPassword, nickname}; //객체 : 서버로 JSON 보낼 때, DB DTO 만들 때, 명확한 데이터 구조가 필요할 때
	  //	•	자바스크립트 코드 안에서 객체 만들 때
		//	→ { roomTitle: roomTitle } 또는 { roomTitle }
		//	•	다른 이름으로 보내고 싶으면
		//	→ { roomt: roomTitle } 또는 { "roomt": roomTitle }
		//	•	서버로 보낼 때는
		//	→ JSON.stringify({ roomt: roomTitle })
		const url = contextPath + "webSocket/createRoom";
		
		try{
			const response = await fetch(url, {
				method : "POST",
				headers : {"Content-Type": "application/json"}, 
				//body : JSON.stringify( {roomSetting : roomSettingInstance}) -> 잡바에서 Map으로 변환못
				body : JSON.stringify(roomSettingInstance)
			});
			
			const result = await response.json();
			//console.log("content-type:", response.headers.get("Content-Type"));
			
			if(response.ok){ 
				//닉네임을 localStorage에 저장하고 사용 (로그인이 아니면 이게 최선인거같음)
				localStorage.setItem("myNickname", result.data.playerList[0].nickname);
				//방생성 후 해당 방으로 조인 요청 
				joinRoomRequest(result.data, result.data.playerList[0].role, result.data.playerList[0].nickname); //newRoom
				
			} else {
				alert(result.message);
				localStorage.removeItem("myNickname");
				return;
			}
			
		} catch (err){
			console.error("통신 오류:", err);
			alert("서버와의 통신 중 오류가 발생했습니다.");
		}
  });
	//방장이 방 생성후 쓰일 방참여 함수 
	async function joinRoomRequest(roomDto, role, nickname){
		
		const url = contextPath + "webSocket/room/" + roomDto.roomSeq + "/join";
		try {
			const response = await fetch(url, {
				method : "POST",
				headers : {"Content-Type" : "application/json"},
				body : JSON.stringify({
					"role" : role,
					"roomPassword" : roomDto.password,
					"nickname" : nickname
				})
			});
			
			const result = await response.json();
			
			if(response.ok){
				window.location.href = contextPath + "webSocket/room/" + roomDto.roomSeq;
				return;
				
			} else {
				alert(result.message);
				return;
			}
			
		} catch(err){
			console.error("통신 오류:", err);
			alert("서버와의 통신 중 오류가 발생했습니다.");
		}
		
	}
  
}

//잘못된 접근 차단 모달 js
function wrongAccessOpenModal(){
	const wrongAccessModalBtn = document.getElementById('wrong-access-btn');
	if(!wrongAccessModalBtn){
		alert('버튼없음 ');
		return;
	}
	
	const errorValue = wrongAccessModalBtn.dataset.errorValue; //접속불가자:true
	if(errorValue === "true"){
		wrongAccessModalBtn.click();
		return;
	}
}





