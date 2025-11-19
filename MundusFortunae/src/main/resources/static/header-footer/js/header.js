/**
 * ✅ JWT 로그아웃
 * - 로컬스토리지 accessToken을 Authorization 헤더로 전송
 * - 서버에서 refresh 쿠키 정리 및 DB 비활성화 처리
 * - 완료 후 토큰 삭제 & 홈으로 이동
 */
const logoutUrl = /*[[@{/api/auth/logout}]]*/ "";

(function wireLogout(){
  const btn = document.getElementById('al-logout-btn');
  if(!btn) return;
  btn.addEventListener('click', async () => {
    const token = localStorage.getItem('accessToken');
    try {
      await fetch( logoutUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(token ? { 'Authorization': 'Bearer ' + token } : {}) 
          // ...{} 전개 연산자 꼭 필요 
          //토큰이 존재하면? 삼항연산자
        }
      });
    } catch (e) {
    	alert('로그아웃 실패 : ', e);
    } finally {
      localStorage.removeItem('accessToken');
      location.href='${contextPath}/';
    }
  });
})();