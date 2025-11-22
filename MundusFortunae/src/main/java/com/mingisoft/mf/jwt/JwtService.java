package com.mingisoft.mf.jwt;

import org.springframework.stereotype.Service;

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

  private final JwtRepository jwtRepository;
  
  public JwtService(JwtRepository jwtRepository) {
    this.jwtRepository = jwtRepository;
  }
  
  //refresh 토큰 save
  public boolean saveNewRefreshToken(JwtEntity jwtEntity) {
   
    return false;
  }
  
}
