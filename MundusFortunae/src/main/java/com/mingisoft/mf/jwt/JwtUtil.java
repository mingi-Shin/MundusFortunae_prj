package com.mingisoft.mf.jwt;

import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mingisoft.mf.exception.JwtCustomException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;


@Component
public class JwtUtil {

  private final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
  
  //JWT 서명/검증에 사용할 비밀키 객체
  private SecretKey secretKey;
  
  private static final String JWT_ISSUER = "mundusFortunae-auth";
  
  /**
   * JwtUtil 생성자
   * @param jwtKey application.properties(or yml)의 jwt.secret 값
   */
  public JwtUtil(@Value("${jwt.secret}") String jwtKey) {
    // jwtKey -> Base64 로 인코딩된 키일 때 :
    // 1) Base64 디코딩
    //    - 암호 알고리즘(HMAC-SHA256)은 문자열이 아니라 "바이트 배열"을 키로 사용한다.
    //    - 따라서 문자열(Base64) → 실제 바이트 배열(byte[]) 로 복원해야한다.
    //    - Decoders.BASE64 는 JJWT(io.jsonwebtoken)에서 제공하는 유틸리티 클래스
    byte[] keyBytes = Decoders.BASE64.decode(jwtKey);

    // 2) HMAC용 SecretKey 생성
    //    - Keys.hmacShaKeyFor(...)
    //      → HMAC-SHA 계열(HS256/384/512)에 사용할 SecretKey 객체를 만들어준다.
    //    - 내부에서 키 길이도 검사해서, 너무 짧은 키(보안에 취약)는 예외를 던진다.
    //    - 이렇게 생성된 secretKey 를 JWT 생성(.signWith) / 검증(parse) 에 재사용한다.
    this.secretKey = Keys.hmacShaKeyFor(keyBytes);
  }
  
  /**
   * JWT 생성 시에는 UserEntity/UserDto 자체를 쓰기보다는, CustomUserDetails나 그 안/밖의 필드들(userId, loginId, role)을 빼서 사용하는 구조가 가장 바람직하다.
   * userSeq, Role 이 기본이고, nickname같은건 보통 db로 가져오나.. 편의성때문에 넣기도 함 
   * @param tokenType
   * @return
   */
  public String generateJwtToken(String tokenType, Long userSeq, String role, String loginId, String nickname, Long expiredMs) {
    
    try {
      long now = System.currentTimeMillis();
      long expiresAt = now + expiredMs;
      
      logger.info("JWT 토큰 생성 - type: {}, 생성시간: {}, 소멸시간: {}", tokenType, now, expiresAt);
      
      return Jwts.builder()
          .subject(String.valueOf(userSeq)) //고유필드명 
          .claim("tokenType", tokenType)
          .claim("nickname", nickname)
          .claim("role", role)
          .claim("issuedAtEpoch", now)
          .claim("expiresAtEpoch", expiresAt)
          .issuedAt(new Date(now))
          .expiration(new Date(expiresAt))
          .issuer("mundusFortunae-auth")
          .signWith(secretKey)
          .compact();
          
    } catch (JwtException | IllegalArgumentException e) {
      logger.error("JWT 생성 실패 - userSeq: {}, tokenType: {}", userSeq, tokenType, e);
      throw JwtCustomException.forGenerateJwt(e); //커스텀 exception 
    }
  }
  
  // ------------------- 유틸에서는 “검사하고, 문제 있으면 그냥 예외 던지기만” ------------------- 
  /**
   * 공통 로직 묶어주기 : 토큰 parser() -> return claims 
   */
  private Claims getClaimsFromToken(String token) {
      Claims claims = Jwts.parser()
          .verifyWith(secretKey) // 여기서 서명 검증
          .build()
          .parseSignedClaims(token) // exp/nbf 등 함께 검증 : 예외던짐 ExpiredJwtException
          .getPayload();
      
      return claims;
  }
  
  /**
   * 토큰 검증 : 서명/만료/포맷 + issuer 검증까지 하고 Claims 반환 -> 재활용 up! 
   */
  public Claims validateToken(String token) {
    Claims claims = getClaimsFromToken(token);
    
    if (!JWT_ISSUER.equals(claims.getIssuer())) {
      throw new JwtException("Invalid JWT Token issuer");
    }
    
    return claims;
  }
  
  /**
   * JWT에서 각종 값 꺼내기 
   */
  public String getTokenType(String token) { //토큰타입 
    Claims claims = validateToken(token); // 여기서 이미 파싱 + 검증 모두 수행 
    return claims.get("tokenType", null);
  }
  public String getUserSeq(String token) { //userSeq
    Claims claims = validateToken(token);  
    return claims.getSubject();
  }
  public String getUserRole(String token) {  //role
    Claims claims = validateToken(token);  
    return claims.get("role", String.class);
  }
  public String getUserEmail(String token) { //email
    Claims claims = validateToken(token); 
    return claims.get("email", String.class);
  }
  public String getUserNickname(String token) { //nickname
    Claims claims = validateToken(token);  
    return claims.get("nickname", String.class);
  }
  public Date getIssuedAt(String token) { //issuedAt
    Claims claims = validateToken(token); 
    return claims.getIssuedAt();
  }
  public Date getExpiration(String token) { //expiration
    Claims claims = validateToken(token); 
    return claims.getExpiration();
  }
  

  
  

  
  

  
}
