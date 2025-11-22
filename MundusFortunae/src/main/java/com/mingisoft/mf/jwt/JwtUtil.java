package com.mingisoft.mf.jwt;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;


@Component
public class JwtUtil {

  private final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
  
  //JWT 서명/검증에 사용할 비밀키 객체
  private SecretKey secretKey;
  
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
  
  
  
}
