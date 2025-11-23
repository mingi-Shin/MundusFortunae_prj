package com.mingisoft.mf.jwt;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CookieUtil {

  /**
   *  쿠키 생성 메서드
   * @param type access | refresh
   * @param token
   * @param hours 쿠키 생존 시간 
   * @return
   */
  public ResponseCookie generateCookie(String type, String token, int hours) {
    return ResponseCookie.from(type, token)
        .httpOnly(true) //js에서 쿠키 접근 불가
        .secure(false) //운영환경에서는 true! 
        .sameSite("Lax") // CSRF 방어 (Lax : 크로스사이트 GET 요청만 허용, 보안성은 Strict가 더 높음)
        .path("/")
        .maxAge(hours * 3600L) // ← 정수 오버로드 사용 권장 (0이면 즉시 삭제)
        .build();
  }
  
  /**
   *  쿠키 꺼내기 (refresh같은 거)
   */
  public String resolveToken(HttpServletRequest request, String type) {
    Cookie[] cookies = request.getCookies();
    
    if(cookies != null) {
      for(Cookie cookie : cookies) {
        if(cookie.getName().equals(type)) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }
  
  /**
   * ACCESS, Refresh 쿠키 지워버리기 
   */
  public void clearAllTokenCookie(HttpServletResponse response) {
    jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("ACCESS_TOKEN", "");
    jakarta.servlet.http.Cookie cookie2 = new jakarta.servlet.http.Cookie("REFRESH_TOKEN", "");
    cookie.setPath("/");     // 저장할 때 썼던 path와 똑같이 맞추기
    cookie2.setPath("/");     // 저장할 때 썼던 path와 똑같이 맞추기
    cookie.setMaxAge(0);     // 브라우저에서 즉시 삭제
    cookie2.setMaxAge(0);     // 브라우저에서 즉시 삭제
    response.addCookie(cookie);
    response.addCookie(cookie2);
  }
  
  
  
}
