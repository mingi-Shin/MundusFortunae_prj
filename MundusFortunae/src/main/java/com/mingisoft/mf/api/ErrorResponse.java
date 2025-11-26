package com.mingisoft.mf.api;

import java.time.Instant;
import java.util.Date;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 보통 4xx / 5xx에 대응
  -잘못된 입력값 (validate 실패, 형식 오류) → 400
  -인증 안 됨 → 401
  -권한 없음 → 403
  -없는 리소스 요청 → 404
  -중복 회원가입 시도 → 409 등
  
  -NullPointerException, DB 에러, 우리가 예상 못한 버그들 → 500
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponse {

  private int statusCode; // HTTP 상태 코드 (400, 404, 500...)
  private String message; // 사용자용/로그용 메시지
  private String path;    // 어느 API에서 터졌는지 -> 요청 경로 (/api/join 등)
  private long timestamp; // 언제 터졌는지 
  private String code;    // 비즈니스 에러 코드 (USER_NOT_FOUND 등) -> 나중에 enum 으로 바꿔도 외부 json은 그대로
  
  public static <T> ErrorResponse of(HttpStatus status, String message, String path){
    return ErrorResponse.<T>builder()
            .statusCode(status.value())
            .message(message)
            .path(path) //프론트엔드와 협업용 코드 
            .timestamp(Instant.now().toEpochMilli()) // System.currentTimeMillis() 써도 됨
            .code(status.name())
            .build();
  }
  
}
