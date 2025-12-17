package com.mingisoft.mf.jwt;

import java.io.IOException;
import java.util.Map;

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
    
    /**
     * 로그인 페이지, 회원가입, 정적 리소스 등 JWT 검사 안 함
     */
    String uri = request.getRequestURI();
    if (uri.startsWith("/login") || uri.startsWith("/resources")) {
        filterChain.doFilter(request, response);
        return;
    }
    
    //일단 header or 쿠키에서 access 토큰 꺼내기
    String accessToken = resolveAccessToken(request);  
    
    if(accessToken != null) { 
      logger.info("accessToken 존재 : {}", accessToken);
      
      // 1-2. access 토큰 검증 (try_catch는 util이 아니라 filter에서 하는게 역할에 맞음) 
      try {
        Claims claims = jwtUtil.validateToken(accessToken); // 서명, 만료, 포맷, issuer 검증
        
        //securityContextHolder 저장 
        setAuthenticationFromClaims(claims);
        logger.info("현재 요청 스레드 보관 정보 : {}", SecurityContextHolder.getContext().getAuthentication());
        
        filterChain.doFilter(request, response);
        
      } catch (ExpiredJwtException e) {
        logger.warn("만료된 access 토큰입니다. token={}", accessToken, e);
        
        try {
          //1. refresh 토큰 검증
          Map<String, Object> mapRes = jwtService.isValidRefreshToken(request); //검증통과시 userSeq, refreshToken 값 반환하여 재활용 
          
          //2. 검증통과 -> access, refresh 쿠키, 헤더 재발급
          Long userSeq = Long.valueOf(String.valueOf(mapRes.get("userSeq")));
          Map<String, Object> newTokenRes = jwtService.tokenNcookieResend(userSeq, response);
          
          //3. refresh토큰 DB 갱신 
          String oldRefreshToken = (String) mapRes.get("refreshToken");
          String newRefreshToken = (String) newTokenRes.get("newRefresh");
          jwtService.deleteOldRefreshToken(oldRefreshToken); // (구)refresh 객체 N으로 업데이트 
          jwtService.insertNewRefreshToken(userSeq, newRefreshToken); //(신)refresh 등록 
          
          //4. 새 토큰 기준으로 인증 세팅
          Object claimsObj = mapRes.get("claims");
          if(!(claimsObj instanceof Claims claims)) { //자바 16이상의 패턴 매칭 활용 
            throw new IllegalStateException("claims 타입이 Claims가 아닙니다: " + claimsObj);
          }
          // 여기서부터 claims는 이미 Claims 타입으로 확정
          setAuthenticationFromClaims(claims);
          
          //5. 원래 요청 계속 진행 (로그아웃 컨트롤러로 보냄)
          filterChain.doFilter(request, response);
          
          return;
          
        } catch (JwtException ex) {
          // refresh까지 만료 or 위조 → 완전 로그아웃 상태
          logger.warn("refresh 토큰도 유효하지 않습니다. 다시 로그인 필요", ex);
          cookieUtil.clearAllTokenCookie(response);
          response.sendRedirect(request.getContextPath() + "/login?invalid=true"); //세션값 만료, 재로그인 안내 
          return;
        }
        
      } catch (JwtException e) {
        // access 자체가 서명 위조, 포맷 깨짐 등
        logger.warn("유효하지 않은 access 토큰입니다. token={}", accessToken, e);
        cookieUtil.clearAllTokenCookie(response);
        response.sendRedirect(request.getContextPath() + "/login?invalid=true");
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
        logger.info("-- Header의 Authorization에서 access 토큰 발견 --");
        return header.substring(7);
    }

    // 2순위: 쿠키
    if (request.getCookies() != null) {
        for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
            if ("ACCESS_TOKEN".equals(cookie.getName())) {
              logger.info("-- 2차시도, 쿠키에서 access 토큰 발견 --");
              return cookie.getValue();
            }
        }
    }

    return null; // 토큰 없음
  }
  
  /**
   * ---------------------------- SecurityContextHolder 초기화 -----------------------------------
   */
  private void setAuthenticationFromClaims(Claims claims) {
    
    // a. Authentication 객체를 만들기 위해서, jwt.claims에서 정보꺼내서 UserDetails 객체 생성 
    //  dto 생성 -> UserDetails에 dto를 삽입 
    UserDto userDto = new UserDto();
    userDto.setUserSeq(Long.parseLong(claims.getSubject()));
    userDto.setNickname(String.valueOf(claims.get("nickname")));
    userDto.setRole(String.valueOf(claims.get("role")));
    
    CustomUserDetails customeUserDetails = new CustomUserDetails(userDto);
    
    // b. Authentication객체를 setAuthentication 해주기 
    Authentication authToken = new UsernamePasswordAuthenticationToken(customeUserDetails, null, customeUserDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authToken);
    
  }

}
