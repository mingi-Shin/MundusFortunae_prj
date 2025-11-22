package com.mingisoft.mf.exception;

public class DuplicateUserException extends BusinessException {

  public DuplicateUserException(String message) {
    super(message, "DUPLICATE_USER");
  }
  
  public static DuplicateUserException forUsername(String loginId ) {
    return new DuplicateUserException("이미 존재하는 아이디입니다: " + loginId);
  }

  public static DuplicateUserException forEmail(String email) {
      return new DuplicateUserException("이미 존재하는 이메일입니다: " + email);
  }

  public static DuplicateUserException forNickname(String nickname) {
      return new DuplicateUserException("이미 존재하는 닉네임입니다: " + nickname);
  }
  
}
/**
 * BusinessException 확장 → 구체적인 예외들 정의, 도메인별, 상황별로 이름이 살아 있는 예외들.
 */
