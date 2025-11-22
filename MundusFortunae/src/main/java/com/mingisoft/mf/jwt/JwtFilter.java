package com.mingisoft.mf.jwt;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mingisoft.mf.user.UserDto;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtFilter extends OncePerRequestFilter {
  
  private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

  private final JwtUtil jwtUtil;
  private final JwtService jwtService;
  private final CookieUtil cookieUtil;
  
  public JwtFilter(JwtService jwtService, JwtUtil jwtUtil, CookieUtil cookieUtil) {
    this.jwtService = jwtService;
    this.jwtUtil = jwtUtil;
    this.cookieUtil = cookieUtil;
  }
  
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    logger.info("---------- JwtFilter 통과 시작 --------------");
    
    String accessToken = resolveAccessToken(request); // header or 쿠키에서 access 토큰 꺼내기 
    
    if(accessToken != null) { 
      logger.info("accessToken 존재 : {}", accessToken);
      
      // 1-2. access 토큰 검증 (try_catch는 util이 아니라 filter에서 하는게 역할에 맞음) 
      try {
        Claims claims = jwtUtil.validateToken(accessToken); // 서명, 만료, 포맷, issuer 검증
        
        // a. Authentication 객체를 만들기 위해 jwt.claims에서 정보꺼내서 UserDetails 객체 생성 
        //  dto 생성 -> UserDetails에 dto를 삽입 
        UserDto userDto = new UserDto();
        userDto.setUserSeq(Long.parseLong(claims.getSubject()));
        userDto.setNickname(String.valueOf(claims.get("nickname")));
        userDto.setRole(String.valueOf(claims.get("role")));
        CustomUserDetails customeUserDetails = new CustomUserDetails(userDto);
        
        // b. Authentication객체를 setAuthentication 해주기 
        Authentication authToken = new UsernamePasswordAuthenticationToken(customeUserDetails, null, customeUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
        
        logger.info("현재 요청 스레드 보관 정보 : {}", SecurityContextHolder.getContext().getAuthentication());
        
        filterChain.doFilter(request, response);
        
      } catch (ExpiredJwtException e) {
        logger.warn("만료된 JWT 토큰입니다. token={}", accessToken, e);
        // REFRESH_TOKEN 쿠키 검증 후, 새 access 발급 + 쿠키 갱신 
        
      } catch (JwtException e) {
        logger.warn("유효하지 않은 JWT 토큰입니다. token={}", accessToken, e);
        // 서명 위조, 포맷 깨짐 등 → 401 or 그냥 익명 사용자 취급
        cookieUtil.clearAccessTokenCookie(response); //잘못된 ACCESS_TOKEN 삭제, 무한루프 방지
        response.sendRedirect(request.getContextPath() + "/login?invalid=true"); 
        //response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }
      
      
    } else {
      // Authorization 헤더도 없고 쿠키도 없을 때
      logger.info("토큰이 존재하지 않음 (헤더/쿠키 모두 없음)");
      filterChain.doFilter(request, response);
    } // if() End 
    
  } //doFilterInternal() End 
  
  
  /**
   * ---------------------- acccess 토큰 꺼내기 ---------------------- 
   * @param request
   * @return
   */
  private String resolveAccessToken(HttpServletRequest request) {
    // 1순위: Authorization 헤더 (나중에 React/Vue 같은 SPA 붙여도 기존 구조 그대로 재사용 가능)
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
        return header.substring(7);
    }

    // 2순위: 쿠키
    if (request.getCookies() != null) {
        for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
            if ("ACCESS_TOKEN".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
    }

    return null; // 토큰 없음
}

}
