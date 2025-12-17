package com.mingisoft.mf.jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.mingisoft.mf.security.UserDetailsServiceImpl;
import com.mingisoft.mf.user.UserDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  (CustomUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 *  로 꺼내올수 있게된다. 
 *  꺼내서 쓸 값들을 여기서 get메서드로 만들어주면 된다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {
  
  private final Logger logger = LoggerFactory.getLogger(CustomUserDetails.class);
  private UserDto userDto;
  
  /**
   * 유저 권한 꺼내기 (role이 한개일때)
   */
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    String role = userDto.getRole(); //ROLE_ADMIN 이어야 정상
    if(role == null) return List.of();
    
    role = role.replace("[", "").replace("]", "").trim(); //[ ] 삭제
    return List.of(new SimpleGrantedAuthority(role));
  }

  /**
   * 시큐리티가 PasswordEncoder.matches() 자동 호출하여 비교
   */
  @Override
  public String getPassword() {
    return userDto.getPassword();
  }

  @Override
  public String getUsername() {
    return userDto.getLoginId();
  }
  
  public String getEmail() {
    return userDto.getEmail();
  }
  
  public String getNickname() {
    return userDto.getNickname();
  }
  
  public Long getUserSeq() {
    return userDto.getUserSeq();
  }

  
}
