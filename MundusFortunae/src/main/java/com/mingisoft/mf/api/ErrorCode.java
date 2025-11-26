package com.mingisoft.mf.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
//@RequiredArgsConstructor
public enum ErrorCode {

  // ---------- 2xx: 성공 ----------
  SUCCESS(HttpStatus.OK, "S200", "요청이 정상적으로 처리되었습니다."),
  CREATED(HttpStatus.CREATED, "S201", "리소스가 정상적으로 생성되었습니다."),
  NO_CONTENT(HttpStatus.NO_CONTENT, "S204", "정상 처리되었지만 반환할 데이터가 없습니다."),
  
  // ---------- 4xx: 클라이언트 오류 ----------
  BAD_REQUEST(HttpStatus.BAD_REQUEST, "C400", "요청 값이 올바르지 않습니다."),
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C401", "인증이 필요합니다."),
  FORBIDDEN(HttpStatus.FORBIDDEN, "C403", "접근 권한이 없습니다."),
  NOT_FOUND(HttpStatus.NOT_FOUND, "C404", "요청하신 리소스를 찾을 수 없습니다."),
  CONFLICT(HttpStatus.CONFLICT, "C409", "리소스 상태 충돌이 발생했습니다."),
  
  // ---------- 5xx: 서버 오류 ----------
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S500", "서버 내부 오류가 발생했습니다."),
  SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "S503", "일시적으로 서비스를 사용할 수 없습니다.");
  
  /**
   * HTTP 응답 상태 코드 (ResponseEntity.status(...)에서 사용)
   */
  private final HttpStatus httpStatus;
  
  /**
   * 서비스 비즈니스용 코드 (로그, 모니터링, 프론트 구분용)
   * 예: S200, C400, S500 ...
   */
  private final String code;
  
  /**
   * 기본 메시지 (message 필드를 별도로 안 채우면 이걸 기본값으로 써도 됨)
   */
  private final String defaultMessage;
  
  private ErrorCode(HttpStatus httpStatus, String code, String defaultMessage) {
    this.httpStatus = httpStatus;
    this.code = code;
    this.defaultMessage = defaultMessage;
  }

}
