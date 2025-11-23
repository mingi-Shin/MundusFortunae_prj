package com.mingisoft.mf.jwt;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import com.mingisoft.mf.user.UserEntity;
import com.mingisoft.mf.user.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;

/**
로그인 시:
User 검증 → access/refresh 발급
refresh 저장(DB or Redis) / isActive 관리

토큰 재발급 시:
refresh 검증
새 access 발급

로그아웃 시:
refresh 무효화, 블랙리스트 등록 등
 */
@Service
public class JwtService {


  private final static Logger logger = LoggerFactory.getLogger(JwtService.class);
  private final JwtRepository jwtRepository;
  private final UserRepository userRepository;
  private final CookieUtil cookieUtil;
  private final JwtUtil jwtUtil;
  
  public JwtService(JwtRepository jwtRepository, UserRepository userRepository, CookieUtil cookieUtil, JwtUtil jwtUtil) {
    this.jwtRepository = jwtRepository;
    this.userRepository = userRepository;
    this.cookieUtil = cookieUtil;
    this.jwtUtil = jwtUtil;
  }
  
  /**
   * 새 refresh 토큰 저장 (내부에 expiresAt을 30일로 설정)
   * @param userSeq
   * @param refreshToken
   * @param createdAt
   * @param expiresAt
   * @return
   */
  public JwtEntity insertNewRefreshToken(Long userSeq, String refreshToken) {
    Long nowEpoch = System.currentTimeMillis();
    long refreshExpiresAt = nowEpoch + 30 * 24 * 60 * 60 * 1000L; //30일
    
    UserEntity user = userRepository.findById(userSeq).orElseThrow();
    JwtEntity jwtEntity = new JwtEntity();
    jwtEntity.setUser(user);
    jwtEntity.setRefreshToken(refreshToken);
    jwtEntity.setCreatedAt(nowEpoch);
    jwtEntity.setExpiresAt(refreshExpiresAt);
    
    jwtRepository.save(jwtEntity);
    logger.info("jwtEntity 저장 성공 : {}", jwtEntity.getRefreshSeq());
   
    return jwtEntity;
  }
  
  /**
   * 재발급 및 로그아웃 : refresh(구) 토큰 업데이트 
   */
  @Transactional
  public boolean deleteOldRefreshToken(String Token) {
    int result = jwtRepository.deactivateToken(Token);
    return result > 0;
  }
  
  /**
   * access 토큰 만료시, refresh 토큰 검증
   */
  public Map<String, Object> isValidRefreshToken(HttpServletRequest request) {
    Map<String, Object> mapRes = new HashMap<>();

    String refreshToken = cookieUtil.resolveToken(request, "REFRESH_TOKEN");
    if (refreshToken == null) {
        throw new JwtException("쿠키에 refresh 토큰이 없습니다.");
    }

    // 1) JWT 포맷/서명/만료 검사
    Claims claims = jwtUtil.validateToken(refreshToken);

    // 2) DB에서 찾기
    JwtEntity jwtEntity = jwtRepository.findByRefreshToken(refreshToken);
    if (jwtEntity == null) {
        throw new JwtException("DB에 해당 refresh 토큰이 존재하지 않습니다.");
    }

    // 3) DB 기준 상태 검사
    if ("N".equals(jwtEntity.getIsActive())
        || jwtEntity.getExpiresAt() < System.currentTimeMillis()) {
        throw new JwtException("DB의 리프레시 토큰값이 만료되었거나 비활성화되었습니다.");
    }

    mapRes.put("refreshToken", refreshToken);
    mapRes.put("userSeq", claims.getSubject());
    mapRes.put("claims", claims);

    return mapRes;
  } 
  
  /**
   * access, refresh 토큰 쿠키, 헤더 재발급 
   */
  public Map<String, Object> tokenNcookieResend(Long userSeq, HttpServletResponse response) {
    
    Map<String, Object> res = new HashMap<String, Object>();
    
    UserEntity user = userRepository.findById(userSeq).orElseThrow();
    
    if(user == null) {
      throw new UsernameNotFoundException("유저가 존재하지 않습니다.");
    }
    
    Long nowEpoch = System.currentTimeMillis();
    long accessExpiresAt = nowEpoch + 10 * 60 * 1000L; //10분 
    long refreshExpiresAt = nowEpoch + 30 * 24 * 60 * 60 * 1000L; //30일
    String accessToken = jwtUtil.generateJwtToken("access", user.getUserSeq(), user.getRole(), user.getNickname(), accessExpiresAt);
    String refreshToken = jwtUtil.generateJwtToken("refresh", user.getUserSeq(), user.getRole(), user.getNickname(), refreshExpiresAt); 
    
    response.setHeader("Authorization", "Bearer " + accessToken);
    
    ResponseCookie accessCookie = cookieUtil.generateCookie("ACCESS_TOKEN", accessToken, 1);// 쿠키 1일
    ResponseCookie refreshCookie = cookieUtil.generateCookie("REFRESH_TOKEN", refreshToken, 24 * 30); //쿠키 30일
    response.addHeader("Set-Cookie", accessCookie.toString());
    response.addHeader("Set-Cookie", refreshCookie.toString());
    
    res.put("newRefresh", refreshToken);
    res.put("newAccess", accessToken);
    
    return res;
  }
  
  
  
}
