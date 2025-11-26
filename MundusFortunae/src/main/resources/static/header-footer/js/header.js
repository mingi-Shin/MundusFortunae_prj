/**
 * ✅ JWT 로그아웃
 * - 로컬스토리지 accessToken을 Authorization 헤더로 전송
 * - 서버에서 refresh 쿠키 정리 및 DB 비활성화 처리
 * - 완료 후 토큰 삭제 & 홈으로 이동
 */
document.addEventListener("DOMContentLoaded", () => {
	
	wireLogout();
});

function wireLogout(){
  const btn = document.getElementById('al-logout-btn');
  if(!btn) return;
	
	const logoutUrl = contextPath + "api/auth/logout";
	
  btn.addEventListener('click', async () => {
    const token = localStorage.getItem('accessToken');
		console.log(token);
		
    try {
     const response = await fetch( logoutUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + token
        }
      });
			
			const result = await response.json();
			
			if(!response.ok){
				alert(result.message || '에러가 발생했습니다.');
				return;
			}
			
			if(response.ok){
				alert(result.message + "\n응답코드 : " + result.statusCode);
			}
    } catch (e) {
    	alert('로그아웃 실패 : ' + (e.message || e));
			//Failed to execute 'json' on 'Response': Unexpected end of JSON input
    } finally {
      localStorage.removeItem('accessToken');
      location.href = contextPath;
    }
  });
}