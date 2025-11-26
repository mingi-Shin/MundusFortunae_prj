document.addEventListener('DOMContentLoaded', () => {
	console.log('로그인 페이지');
	
	//예외처리

	//로그인 post 비동기 처리 	
	loginProcess();

	//로컬스토리지 저장된 아이디 불러오기 
	getSavedId();
	
});

/**
 * 로그인 함수 
 */
function loginProcess() { 
	document.getElementById("login-process-btn").addEventListener("click", async () => {
	  const username = document.getElementById("login-id").value.trim();
	  const password = document.getElementById("login-pwd").value.trim();
	  const errorDiv = document.getElementById("login-error");
	  
	  //아이디 저장 여부 
	  let rememberId = document.querySelector('#save-id').checked;
	  if(rememberId){
		  localStorage.setItem('rememberId', username);
	  } else {
		  localStorage.removeItem('rememberId');
	  }
	  
	  // 로그인 빈칸 예외처리 
	  if(!username||!password){
		  errorDiv.textContent = "아이디와 비밀번호를 입력해주세요.";
		  errorDiv.style.display='block';
		  return;
	  }
		
		//n초동안 로그인 중..
		loginProcessing();
		await new Promise(resolve => setTimeout(resolve, 4200)); //4초 기다리기 
		
	  // 로그인 api요청 
	  try {
			
		  const response = await fetch( contextPath + "api/auth/login", {
			  method : "POST",
			  headers : {"Content-Type" : "application/json"},
			  body : JSON.stringify({username, password})
		  });
		  
	/* 
		-- 이후 다른 API 호출 시
		const response = await fetch("/api/some-endpoint", {
		    method: "GET",
		    headers: {
		        "Authorization": `Bearer ${localStorage.getItem("accessToken")}`
		    }
		});
	*/
		  const result = await response.json();
		  
		  if(result.success){
			  localStorage.setItem("accessToken", result.accessToken);
				location.href = contextPath;
			  
		  } else {
			  const failedCause = result.message;
			  errorDiv.textContent = failedCause;
			  errorDiv.style.display = 'block';
		  }
			
	  } catch(err) {
			//fetch 자체를 실패
		  console.log(err);
		  errorDiv.textContent ="서버 오류가 발생했습니다. 고객센터에 문의해주세요.";
		  errorDiv.style.display = 'block';
	  }
	});
}//loginProcess
	
/**
 * 저장된 아이디 불러오기 함수 
 */
function getSavedId(){
	const userId = localStorage.getItem('rememberId');
	if(userId !== null){
		document.getElementById("login-id").value = userId;
		document.querySelector('#save-id').checked = true;
	} 
}

/**
 * 3초동안 접속중.. 텍스트 띄우는 함수 
 */
function loginProcessing(){
const errorDiv = document.getElementById("login-error");
	
 const messages = [
	"잠시만 기다려주세요. 문을 여는중.",
	"잠시만 기다려주세요. 문을 여는중..",
	"잠시만 기다려주세요. 문을 여는중...",
	"모험가의 신원을 조회중.",
	"모험가의 신원을 조회중..",
	"모험가의 신원을 조회중...",
 ];
 
 messages.forEach((msg, index) => {
	setTimeout(() => {
		errorDiv.textContent = msg;
		errorDiv.style.display = 'block';
	}, index * 500); //0.5초 간격
 });
	
}
