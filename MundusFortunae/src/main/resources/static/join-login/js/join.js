const validationState = {
		id : false,
		email : false,
		password : false,
		nickname : false
};

document.addEventListener("DOMContentLoaded", function(){
	
	console.log(contextPath);
	
	joinUser(); //회원가입
	
	duplicateUserBy(); //중복검사 
	
	agreeTerms(); //약관동의 
	
	chkInputExeption();
	
	
});//DOM
	
//회원가입 api 
function joinUser(){
	
	const joinBtn = document.getElementById("join-btn");
	
	joinBtn.addEventListener("click", async () => {

		//일단 먼저 아이디, 비밀번호, 이메일, 닉네임 유효값 검사
		const isAllValid = Object.values(validationState).every(v => v === true); //.every()
		if (isAllValid) {
		  console.log("모든 값이 true입니다!");
		} else {
		  alert("아직 유효하지 않은 항목이 있습니다.");
		  return;
		}

		const loginId = document.getElementById("user-id").value;
		const password = document.getElementById("user-password").value;
		const nickname = document.getElementById("nickname").value;
		const email = document.getElementById("user-email").value;
		const emailSubscribed = document.getElementById("switchCheckDefault").checked;
		
		
		try {
			const res = await fetch(contextPath + "api/join", {
				method : "POST",
				headers : {"Content-Type": "application/json"},
				body : JSON.stringify({loginId, password, nickname, email, emailSubscribed})
			});
			
			// 201이든 409든, 서버에서 body를 항상 보내니까 그냥 파싱
			const data = await res.json().catch(() => null);

			console.log("res:", res);
			console.log("data:", data);
			
			// 1) 성공 (201 + success=true)
			if (res.ok && data && data.success) {
			  // loginId로 로그인 페이지 이동 예시
			  location.href = contextPath + 'login/' + loginId;
			  return;
			}

			// 2) 중복 (409 CONFLICT)
			if (res.status === 409) {
			  const msg = data.message; //ApiResponse 객체 필드명 
			  alert(msg);
			  return;
			}
			
			// 3) 그 외 서버 에러
			console.error("회원가입 실패 상태코드:", res.status, data);
			alert("회원가입에 실패했습니다. 잠시 후 다시 시도해주세요.");

		} catch (err){
			console.error("통신 오류:", err);
			alert("서버와의 통신 중 오류가 발생했습니다.");
		}
	});
}

/**
 *	아이디, 이메일, 닉네임 중복체크
 */
function duplicateUserBy(){
	
	document.getElementById('id-dupl-btn').addEventListener('click', function(){
		const loginId = document.getElementById('user-id').value;
		duplicateUser('loginId', loginId);
	});
	document.getElementById('nickname-dupl-btn').addEventListener('click', function(){
		const nickName = document.getElementById('nickname').value;
		duplicateUser('nickname', nickName);
	});
	document.getElementById('email-dupl-btn').addEventListener('click', function(){
		const email = document.getElementById('user-email').value;
		duplicateUser('email', email);
	});
	
	//-------이메일 중복검사는 input이벤트로 처리 + debounce ------
/* 	let timer;
	document.getElementById('user-email').addEventListener('input', function(){
		clearTimeout(timer);
		timer = setTimeout(() => {
			let userEmail = document.getElementById('user-email').value;
			duplicateUser('email', userEmail);
		}, 1500);
	});
*/
	
}

/**
 * 중복확인 요청 
 */
async function duplicateUser(field, value){ //async는 함수선언 앞에 
	
	if(value == null || value.trim() === ""){
		alert("값을 입려해주세요 ");
		return;
	}
	
	//특정 단어 불가 필터 && .some(콜백함수) : 배열의 요소를 하나씩 꺼내서 콜백함수에 넣어보고 && keyword = forbiddenWords 배열의 각 요소를 순서대로 가리키는 매개변수
	function hasForbiddenWord(value) {
	  const forbiddenWords = ['관리자', 'admin', '운영자', 'system', '매니저', 'manager'];
	  return forbiddenWords.some(keyword =>  
	    value.toLowerCase().includes(keyword.toLowerCase())
	  );
	}
	if (hasForbiddenWord(value)) {
	  alert(value + '는 사용하실 수 없습니다.');
	  return;
	}
	
	//전송 요청 
	try {
		const params = new URLSearchParams({ checkField : field, checkValue : value}); //"JS 객체 → 안전한 URL 쿼리 문자열” 변환기
		
		const url = contextPath + 'api/join/userInfo?' + params.toString(); 
		
		const response = await fetch(url, {
			method : "GET",
			// -- GET에서는 필요 없음 --
			//headers : {"Content-Type" : "application/json"},
			//body : JSON.stringify({ "checkField": field,  "checkValue" : value}) //JS객체 -> JSON문자열로 변환 
		});	
		
		if (!response.ok) { // 200~299 아니면 여기로
		  const errorText = await response.text();
		  console.error("중복체크 실패", response.status, errorText);
		  alert("중복체크 중 오류 발생: " + response.status);
		  return;
		}
		
		const isDuplicated = await response.json();
		
		if(isDuplicated){
			alert(value + "는 사용하실 수 없습니다.");
		}
		if(!isDuplicated){
			const conf = confirm(value + "는 사용가능합니다.\n사용하시겠습니까?");
			if(conf){
				if(field == 'loginId'){
					document.getElementById('user-id').readOnly = true;
					document.getElementById('id-dupl-btn').disabled = true;
					validationState.id = true; 
				}
				if(field == 'email'){
					document.getElementById('user-email').readOnly = true;
					document.getElementById('email-dupl-btn').disabled = true;
					validationState.email = true;
				}
				if(field == 'nickname'){
					document.getElementById('nickname').readOnly = true;
					document.getElementById('nickname-dupl-btn').disabled = true;
					validationState.nickname = true;
				}
			}
			
		}
		
	} catch (error) {
			console.error("중복 체크 에러:", error);
      alert("서버와 통신 중 오류가 발생했습니다.");
	}
	
}

/**
 * input창 예외처리 
 */
function chkInputExeption(){
	
	document.querySelectorAll('.form-control').forEach( input => {
		input.addEventListener('input', (e) => {
			if(e.target.id === 'user-id'){
				if(!/^[a-z0-9]{4,15}$/.test(e.target.value)){
					document.getElementById('id-alert-msg').style.display = 'block';
					document.getElementById('id-dupl-btn').disabled = true;
					validationState.id = false;
				} else {
					document.getElementById('id-alert-msg').style.display = 'none';
					document.getElementById('id-dupl-btn').disabled = false;
				}
			}
			if(e.target.id === 'user-password'){
				if(!/^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*()_\-+=\[\]{};:'",.<>/?\\|])[A-Za-z\d!@#$%^&*()_\-+=\[\]{};:'",.<>/?\\|]{6,15}$/.test(e.target.value)){
					document.getElementById('pwd-alert-msg').style.display = 'block';
					validationState.password = false;
				} else {
					document.getElementById('pwd-alert-msg').style.display = 'none';
					validationState.password = true;
				}
			}
			if(e.target.id === 'user-email'){
				if(!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[A-Za-z]{2,}$/.test(e.target.value)){
					document.getElementById('email-alert-msg').style.display = 'block';
					document.getElementById('email-dupl-btn').disabled = true;
					validationState.email = false;
				} else {
					document.getElementById('email-alert-msg').style.display = 'none';
					document.getElementById('email-dupl-btn').disabled = false;
				}
			}
			if(e.target.id === 'nickname'){
				if(!/^[a-zA-Z0-9가-힣]{2,8}$/.test(e.target.value)){
					document.getElementById('nickname-alert-msg').style.display = 'block';
					document.getElementById('nickname-dupl-btn').disabled = true;
					validationState.nickname = false;
				} else {
					document.getElementById('nickname-alert-msg').style.display = 'none';
					document.getElementById('nickname-dupl-btn').disabled = false;
				}
			}
		});
	});
	
}
 

/**
 *  약관동의 체크 함수 
 */
function agreeTerms(){
	
	let agreeAllChkbox = document.getElementById('agreeAll')
	let agreeServiceChkbox = document.getElementById('agreeTotalService')
	let agreePrivacyChkbox = document.getElementById('privacPolicy')
	
	//전체동의 체크박스 
	document.getElementById('agreeAll').addEventListener('change', function(){
		const checkBoxes = document.querySelectorAll('.checkbox-input:not(#agreeAll)');
		checkBoxes.forEach(checkbox => {
			checkbox.checked = this.checked;
		});
		//전체동의 체크박스가 체크되면 버튼 활성화 
		document.getElementById('join-btn').disabled = !agreeAllChkbox.checked
	});
	
	// 개별 체크박스
	document.querySelectorAll('.checkbox-input:not(#agreeAll)').forEach(checkbox => {
	  checkbox.addEventListener('change', function () {
	    const allCheckboxes = document.querySelectorAll('.checkbox-input:not(#agreeAll)');
	    const checkedBoxes = document.querySelectorAll('.checkbox-input:not(#agreeAll):checked');
	    const agreeAll = document.getElementById('agreeAll');
	    const joinBtn = document.getElementById('join-btn');

	    // 전체동의 체크박스 상태 갱신
	    agreeAll.checked = allCheckboxes.length === checkedBoxes.length;

	    // 모든 체크박스가 체크되면 버튼 활성화
	    joinBtn.disabled = !agreeAll.checked;
	  });
	});
	
	
}


/******************************************************************
 
// 1. debounce 함수 정의
function debounce(func, delay) {
  let timer;
  return function(...args) {
    clearTimeout(timer);               // 기존 타이머 제거
    timer = setTimeout(() => {
      func.apply(this, args);          // delay 후 실행
    }, delay);
  };
}

// 2. 콘솔 찍는 함수
function sayHello(text) {
  console.log("Hello!", text);
}

// 3. 디바운스 적용
const debouncedHello = debounce(sayHello, 1500);

// 4. 이벤트 시뮬레이션
document.addEventListener('input', () => {
  debouncedHello("민기님");
});

위처럼 재사용 하지 않을거면, 함수하나에 그냥 박아버려
=> 

let timer;
input.addEventListener('input', () => {
  clearTimeout(timer);
  timer = setTimeout(() => {
    duplicateUser('email', emailValue);
  }, 1500);
});
 ******************************************************************/
	