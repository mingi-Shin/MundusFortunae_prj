package com.mingisoft.mf.exception;

/**
 * 커스텀 Exception
 */
public class DuplicateUserException extends RuntimeException {

  public DuplicateUserException(String message) {
    super(message);
  }
}

/**
왜 RuntimeException 을 상속하냐면:
  • 체크 예외(Exception)를 쓰면 try/catch를 강제해야 해서 불편함
  • 스프링에서 비즈니스 예외는 대부분 RuntimeException 사용
  • @Transactional 롤백도 RuntimeException일 때 자동 적용됨
*/