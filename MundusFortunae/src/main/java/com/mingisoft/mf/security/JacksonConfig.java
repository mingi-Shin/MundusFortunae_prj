package com.mingisoft.mf.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class JacksonConfig {

  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    
    return mapper;
  }
}

/**
이 방법의 장점:

✅ ObjectMapper가 싱글톤으로 관리됨 (하나만 생성)
✅ 모든 곳에서 동일한 설정 사용
✅ 순환 참조 문제 해결
✅ 나중에 설정 변경 시 한 곳만 수정
✅ Spring의 의존성 관리 패턴 준수

결론: 별도 Configuration 클래스로 분리하는 방법을 추천합니다! 🎯

*/
