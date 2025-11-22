package com.mingisoft.mf.exception;

public class LoginExceptionn extends BusinessException {

  public LoginExceptionn(String message) {
    super(message, "INVALID_LOGIN_REQUEST");
    
  }
  

}
