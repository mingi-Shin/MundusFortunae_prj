package com.mingisoft.mf.user;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

  private Long userSeq;

  private LocalDateTime createdAt;

  private String loginId;   

  private String password;  

  private LocalDateTime pwChangedAt;

  private String nickname;

  private String email;

  private String accountStatus;

  private LocalDateTime statusChangedAt;

  private LocalDateTime lastLoginAt;

  private boolean emailSubscribed; 

  private boolean loginStatus;

  private String role;
  
  public static UserDto from(UserEntity entity) {
    UserDto userDto = new UserDto();
    userDto.userSeq = entity.getUserSeq();
    userDto.createdAt = entity.getCreatedAt();
    userDto.loginId = entity.getLoginId();
    userDto.password = entity.getPassword();
    userDto.pwChangedAt = entity.getPwChangedAt();
    userDto.nickname = entity.getNickname();
    userDto.email = entity.getEmail();
    userDto.accountStatus = entity.getAccountStatus();
    userDto.statusChangedAt = entity.getStatusChangedAt();
    userDto.lastLoginAt = entity.getLastLoginAt();
    userDto.emailSubscribed = entity.isEmailSubscribed();
    userDto.loginStatus = entity.isLoginStatus();
    userDto.role = entity.getRole();
    
    return userDto;
  }
  
  public static UserEntity toEntity(UserDto dto) {
    UserEntity entity = new UserEntity();

    /**
     * JPA는 save() 호출 시 이렇게 판단해:
     * PK가 null → 새로운 row INSERT
     * PK가 값 있음 → UPDATE 시도
     * 회원가입 시에는 entity.setUserSeq()를 빼는게 깔끔하지만, 동작은 된다.  
     */
    entity.setUserSeq(dto.getUserSeq());   // 보통 회원가입에서는 null 이고, 수정 시에만 사용
    
    entity.setLoginId(dto.getLoginId());
    entity.setPassword(dto.getPassword());
    entity.setNickname(dto.getNickname());
    entity.setEmail(dto.getEmail());
    entity.setAccountStatus(dto.getAccountStatus());
    entity.setEmailSubscribed(dto.isEmailSubscribed());
    entity.setLoginStatus(dto.isLoginStatus());

    // 아래 필드들은 DB 또는 비즈니스 로직이 관리하는 영역이라 DTO에서 그대로 받지 않음
    // entity.setCreatedAt(dto.getCreatedAt());
    // entity.setPwChangedAt(dto.getPwChangedAt());
    // entity.setStatusChangedAt(dto.getStatusChangedAt());
    // entity.setLastLoginAt(dto.getLastLoginAt());
    // entity.setRole(dto.getRole());

    return entity;
    
  }
  
}
