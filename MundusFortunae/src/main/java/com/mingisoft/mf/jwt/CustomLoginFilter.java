package com.mingisoft.mf.jwt;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mingisoft.mf.exception.LoginExceptionn;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT 발급 담당 
 * UsernamePasswordAuthenticationFilter가 jwt를 못쓰고, json을 못쓰는 등의 한계를 넘기위해 새로만듬 
 * 로그인 시도 필터 : 토큰 생성 및 응답헤더, 쿠키 처리 
 * 커스텀 로그인(formLogin, httpBasic제외)은 인증 객체를 직접 만들어서 setAuthentication 해줘야해. 
 * SecurityContextHolder는 내부적으로 AuthenticationManager가 설정. 
 */

public class CustomLoginFilter extends UsernamePasswordAuthenticationFilter {

  private static final Logger logger = LoggerFactory.getLogger(CustomLoginFilter.class);
  
  private final JwtUtil jwtUtil;
  private final CookieUtil cookieUtil;
  private final JwtService jwtService;
  private ObjectMapper objectMapper;
  
  public CustomLoginFilter(JwtUtil jwtUtil, CookieUtil cookieUtil, JwtService jwtService,  ObjectMapper objectMapper) {
    this.jwtUtil = jwtUtil; //토큰생성 
    this.objectMapper = objectMapper; //json 처리 객체 
    this.jwtService = jwtService;
    this.cookieUtil = cookieUtil;
  }
  /**
   *  로그인 시도 메서드 
   */
  @Override
  public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
      throws AuthenticationException {
    
    logger.info("=== JWT 로그인 시도 ===");
    
    //JSON 타입 검증
    if(!MediaType.APPLICATION_JSON_VALUE.equals(request.getContentType())){
      response.setStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
      logger.error("잘못된 Content_Type : {} ", request.getContentType());
      return null;
    }
    
    try {
      // .readValue() -> JSON을 Java객체(LoginRequestDTO)로 변환 && JSON 파싱 = .getInputStream() -> HTTP요청 바디 읽기
      LoginRequestDto loginRequestVo = objectMapper.readValue(request.getInputStream(), LoginRequestDto.class);
      String username = loginRequestVo.getUsername();
      String password = loginRequestVo.getPassword();
      
      // 2차검증 (사실 필요한지 모르겟음)
      if(username == null || username.trim().isEmpty() ||
          password == null || password.trim().isEmpty()) {
        throw new LoginExceptionn("아이디 또는 비밀번호가 비어 있습니다."); //→ HTTP 응답(상태코드 + 바디) 로 바꿔줘야, JS가 그걸 읽고 alert()을 띄울 수 있어.
      }
      
      logger.info("로그인 아이디 : {}, 비밀번호 : {}", username, password);
      
      //Authentication (인터페이스) -> UsernamePasswordAuthenticationToken (구체 클래스)
      Authentication authToken = new UsernamePasswordAuthenticationToken(username, password);
      
      logger.info("로그인 '시도' 정보(아직 인증 안 됨 (authenticated == false)) = 생성된 Authentication 토큰: {}", authToken);
      
      /** -------------------- .authenticate(authToken); -------------------
       * AuthenticationManager가 등록된 AuthenticationProvider들에게 토큰을 던짐
          (보통 DaoAuthenticationProvider)
          DaoAuthenticationProvider가:
          UserDetailsService로 DB에서 사용자 조회
          PasswordEncoder.matches(입력비번, DB비번) 검사
          
          검증에 성공하면 새로운 Authentication 객체를 만들어서 리턴해 줌
          (여기엔 DB 기준 UserDetails, 권한 목록, 기타 정보가 다 들어있음)
          
          실패하면 BadCredentialsException, UsernameNotFoundException 같은 예외를 던짐
          그래서 authentication 변수에 들어오는 건:
          이제 authenticated = true 상태인
          “검증 완료된 사용자 정보”라고 보면 돼.  */
      Authentication authentication = this.getAuthenticationManager().authenticate(authToken); //필터에서 상속받았음 
      
      logger.info(" '인증' 성공 - principal: {}, authorities: {}",
          authentication.getName(),
          authentication.getAuthorities());
      
      return authentication;
      
    } catch (IOException e) {
      // JSON 파싱 실패도 "인증 관련 오류"로 취급하고 밖으로 던져버림
      throw new AuthenticationServiceException("로그인 요청 JSON 파싱 실패", e);
    }
    
  }
  
  /**
   *  로그인 성공시 ( = .authenticate(authToken) 성공, AuthenticationSuccessHandler를 대신함 )
   */
  @Override //@Override로 빼앗아 왔으면 .. super() or 직접 setAuthentication 해줘야 한다. 
  protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain, Authentication authResult) throws IOException, ServletException {
    
    
    try {
      CustomUserDetails customUser  = (CustomUserDetails) authResult.getPrincipal();
      
      // 1) SecurityContexHolder 초기화 
      SecurityContext context = SecurityContextHolder.createEmptyContext();
      context.setAuthentication(authResult);
      SecurityContextHolder.setContext(context);
      
      // 2) JWT 생성 + 응답
      long nowEpoch = System.currentTimeMillis();
      long accessExpiresAt = nowEpoch + 10 * 60 * 1000L; //10분 
      long refreshExpiresAt = nowEpoch + 30 * 24 * 60 * 60 * 1000L; //30일
      String accessToken = jwtUtil.generateJwtToken("access", customUser.getUserSeq(), customUser.getAuthorities().toString(), customUser.getNickname(), accessExpiresAt);
      String refreshToken = jwtUtil.generateJwtToken("refresh", customUser.getUserSeq(), customUser.getAuthorities().toString(), customUser.getNickname(), refreshExpiresAt); 
      
      // 3) DB저장 
      jwtService.insertNewRefreshToken(customUser.getUserSeq(), refreshToken);
      
      // 4) access -> 응답헤더, access&refresh -> 쿠키 == 하이브리드!!
      response.setHeader("Authorization", "Bearer " + accessToken);
      
      ResponseCookie accessCookie = cookieUtil.generateCookie("ACCESS_TOKEN", accessToken, 1);// 쿠키 1일
      ResponseCookie refreshCookie = cookieUtil.generateCookie("REFRESH_TOKEN", refreshToken, 24 * 30); //쿠키 30일
      response.addHeader("Set-Cookie", accessCookie.toString());
      response.addHeader("Set-Cookie", refreshCookie.toString());
      
      // 5) 응답타입 설정 
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");
      response.setStatus(HttpStatus.OK.value()); //프론트엔드 if문 분기점 
      
      Map<String, Object> body = new HashMap<String, Object>();
      body.put("success", true);
      body.put("accessToken", accessToken);
      
      // 6) 실제 데이터 전송 (java객체 -> josn문자열), 프론트엔드에서 받아서 여러가지 작업 처리 
      objectMapper.writeValue(response.getWriter(), body);
      
    } catch (Exception e) {
      logger.info("로그인 성공 처리중 오류 ", e);
      
      // 6-2. 응답타입 설정 
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");
      response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value()); //500
      
      logger.error("로그인 성공처리 에러 : {} ", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
      
      // 7-2. 오류 메시지 JSON 전송 
      Map<String, Object> errorResponse = new HashMap<String, Object>();
      errorResponse.put("success", false);
      errorResponse.put("message", "로그인 처리 중 서버 오류 발생 ");
      objectMapper.writeValue(response.getWriter(), errorResponse);
    }
    

    
  }
  
  
  /**
   *  로그인 실패시 ( = .authenticate(authToken)에서 예외발생, AuthenticationFailureHandler를 대신함 )
   */
  @Override
  protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
          AuthenticationException failed) throws IOException, ServletException {
    
    logger.error("=== JWT 로그인 실패 === : {}", failed);
    
    /**
     *  유저 값에 따른 예외처리 오류메시지 작성 및 전송 (UserDetailsServiceImpl 에서 정의중)
     *  DaoAuthenticationProvider 에서 예외처리를 InternalAuthenticationServiceException에 감싸서 던지고 있음.
     */
    String failMessage = "알 수 없는 이유로 로그인에 실패했습니다.";
    
    //BadCredentialsException는 사용자 오류 입력이기 때문에, InternalAuthenticationServiceException로 감싸서 던져지지 않음
    if (failed instanceof BadCredentialsException) {
      failMessage = "아이디 또는 비밀번호를 다시 한번 확인해주세요.";
    }
    
    //DaoAuthenticationProvider가 Spring Security는 보안상 이유로 UsernameNotFoundException을 
    // BadCredentialsException으로 통일해서 던진다.
    if (failed instanceof UsernameNotFoundException) {
      failMessage = "사용자 정보를 찾을 수 없습니다(failed)";
    }
    
    if(failed instanceof InternalAuthenticationServiceException) {
      Throwable cause = failed.getCause(); 

      if(cause instanceof LockedException) {
        failMessage = "정지된 계정입니다. 고객센터에 문의해주세요.";
      }
      if(cause instanceof DisabledException) {
        failMessage = "이메일 인증을 완료해주세요.";
      }
      if(cause instanceof AccountExpiredException) {
        failMessage = "탈퇴 처리된 계정입니다.";
      }
    } 
    
    // 로그인 실패 응답 설정 
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.setStatus(HttpStatus.UNAUTHORIZED.value()); //401
    
    // 응답 전송
    Map<String, Object> failResponse = new HashMap<String, Object>();
    failResponse.put("success", false);
    failResponse.put("message", failMessage);
    objectMapper.writeValue(response.getWriter(), failResponse);
    }
    
  
  
}
