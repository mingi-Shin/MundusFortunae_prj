document.addEventListener('DOMContentLoaded', () => {
	console.log('웹소켓 방리스트 페이지');
	
	createRoom();
	roomJoin();
	
});

//방 참여하기 
function roomJoin(){
	document.querySelectorAll(".joinBtn").forEach( (btns) => {
		btns.addEventListener("click", (e) => {
			const roomTitle = e.currentTarget.getAttribute("data-room-title"); //자주 활용하면 편리할 듯 
			document.getElementById('room-join-title').value = rtitle;	  			
			document.getElementById('room-join-title').readOnly = true; 
			
			const roomPassword = document.getElementById("room-join-password").value;
			const nickname = document.getElementById("room-join-nickname").value.trim();
			
			
			
		});
	});		
} 


//방 생성하기 
function createRoom(){
  
  document.getElementById("room-create-btn").addEventListener("click", async () => {
	  const roomTitle = document.getElementById("room-create-title").value.trim();
	  const roomPassword = document.getElementById("room-create-password").value.trim();
	  const nickname = document.getElementById("room-create-nickname").value.trim();
	  
	  if(!roomTitle || !roomPassword || !nickname){
		  alert("빈칸을 입력해주세요.");
		 	return;
	  }
		
		const roomSetting = [roomTitle, roomPassword, nickname]; //배열 : 반복문 돌리기, 순위/로그 기록 등
		const roomSetting2 = {roomTitle, roomPassword, nickname}; //객체 : 서버로 JSON 보낼 때, DB DTO 만들 때, 명확한 데이터 구조가 필요할 때
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
				body : JSON.stringify(roomSetting)
			});
			
			const result = await response.json();
			
			if(result.ok){
				
			} else {
				
			}
			
		} catch (err){
			console.error("통신 오류:", err);
			alert("서버와의 통신 중 오류가 발생했습니다.");
		}
	  
  });
  
  
}