package com.mingisoft.mf.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mingisoft.mf.jwt.CustomUserDetails;
import com.mingisoft.mf.user.UserDto;
import com.mingisoft.mf.user.UserEntity;
import com.mingisoft.mf.user.UserRepository;

/**
1. UsernamePasswordAuthenticationFilter (혹은 커스텀 LoginFilter)
  사용자가 로그인 요청 시 username, password 를 꺼냄
  AuthenticationManager 에게 인증 요청 위임

2. AuthenticationManager → DaoAuthenticationProvider
  loadUserByUsername(username) 호출하여 DB 사용자 정보를 조회
  → 여기서는 “아이디 존재 여부” 만 판단하고 UserDetails 객체를 반환
  UserDetails.getPassword() 로 DB에 저장된 암호화된 비밀번호를 가져옴
  사용자가 입력한 비밀번호(Authentication 객체의 credentials)와 비교
  → passwordEncoder.matches(rawPassword, encodedPassword) 실행
  일치하지 않으면
  👉 BadCredentialsException: Bad credentials 발생
  (로그에 찍힌 것처럼 password does not match stored value)

3. LoginFilter (혹은 Success/FailureHandler)
  예외 발생 시 인증 실패 로직 실행 (onAuthenticationFailure)
  성공 시 토큰 발급 혹은 세션 생성 (onAuthenticationSuccess), 검증(매칭)은 다른 클래스인 DaoAuthenticationProvider 가 담당
*/
@Service("customUserDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

  private final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
  private final UserRepository userRepository;
  
  public UserDetailsServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }
  
  //커스텀 중 
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    
    logger.info("--- loadUserByUsername() 실행 ---");
    UserEntity userEntity = userRepository.findByLoginId(username)
                              .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다 : " + username)); //Optional 적용중 
    //예외는 DaoAuthenticationProvider내부로 전달되며, InternalAuthenticationServiceException 으로 감싸 던져집니다.
    
    switch (userEntity.getAccountStatus()) {
      case "ACTIVE" :
        break;
      case "SUSPENDED" : 
        throw new LockedException("계정이 정지되었습니다. 고객센터에 문의하세요.");
      case "DELETED" : 
        throw new AccountExpiredException("탈퇴된 계정입니다. 다시 가입해주세요.");
      case "PENDING" : 
        throw new DisabledException("아직 계정이 활성화되지 않았습니다. 이메일 인증을 완료해주세요.");
    }
    
    UserDto userDto = UserDto.from(userEntity);
    CustomUserDetails cUserDetails = new CustomUserDetails(userDto);
    
    return cUserDetails;
  }
  
}
