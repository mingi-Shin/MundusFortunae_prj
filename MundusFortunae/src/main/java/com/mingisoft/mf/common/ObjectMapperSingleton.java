package com.mingisoft.mf.common;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ObjectMapperSingleton {
  
  //애플리케이션 전체에서 하나만 쓸 인스턴스
  private static final ObjectMapper INSTANCE = create();
  
  // 외부에서 new 못하게 막기 
  private ObjectMapperSingleton() {};
  
  //설정 넣고 싶으면 여기서 한번만 구성
  private static ObjectMapper create() {
    ObjectMapper mapper = new ObjectMapper();
    // 예시: 설정이 필요하면 여기서
    // mapper.findAndRegisterModules();
    // mapper.enable(SerializationFeature.INDENT_OUTPUT);
    return mapper;
  }
  
  public ObjectMapper getInstance() {
    return INSTANCE;
  }
  

}
