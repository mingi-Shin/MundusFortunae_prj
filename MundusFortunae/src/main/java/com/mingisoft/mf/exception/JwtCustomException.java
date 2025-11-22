package com.mingisoft.mf.exception;

public class JwtCustomException extends BusinessException {

  private Throwable e;
  
  public JwtCustomException(String message, String errorCode, Throwable e) {
    super(message, errorCode);
    this.e = e;
  }
  
  public static JwtCustomException forGenerateJwt(Throwable e) {
    return new JwtCustomException("JWT 생성 중 오류가 발생했습니다", "JWT_CREATION_ERROR", e);
  }
  

}
