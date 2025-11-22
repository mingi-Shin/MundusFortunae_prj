package com.mingisoft.mf.exception;

/**
 * 커스텀 Exception
 */
public class BusinessException extends RuntimeException {

  private final String errorCode;
  
  public BusinessException(String message, String errorCode) {
    super(message);
    this.errorCode = errorCode;
  }
  
  public String getErrorCode() {
    return errorCode;
  }
}

/**
왜 RuntimeException 을 상속하냐면:
  • 체크 예외(Exception)를 쓰면 try/catch를 강제해야 해서 불편함
  • 스프링에서 비즈니스 예외는 대부분 RuntimeException 사용
  • @Transactional 롤백도 RuntimeException일 때 자동 적용됨
  
  또한, 코드 보는 사람 입장에서 RuntimeException으로 다 해결해버리면 뭔 에러인지 알수가 없어.
  그래서 도메인에 맞는 예외를 우리가 직접 만드는게 정석이지 
*/