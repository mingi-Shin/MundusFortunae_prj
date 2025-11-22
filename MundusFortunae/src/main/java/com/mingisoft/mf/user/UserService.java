package com.mingisoft.mf.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.javassist.bytecode.DuplicateMemberException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import com.mingisoft.mf.exception.BusinessException;
import com.mingisoft.mf.exception.DuplicateUserException;

@Service
public class UserService {

  private final Logger logger = LoggerFactory.getLogger(UserService.class);
  private final UserRepository userRepository;
  private final BCryptPasswordEncoder encoder;
  
  public UserService(UserRepository userRepository, BCryptPasswordEncoder encoder) {
    this.userRepository = userRepository;
    this.encoder = encoder;
  }
  
  /**
   * 회원가입
   */
  public UserDto saveUser(UserDto userDto) {
    
    // 2차 중복 검사 (애플리케이션 레벨) 
    if(userRepository.existsByLoginId(userDto.getLoginId())) {
      throw DuplicateUserException.forUsername(userDto.getLoginId());
    }
    if(userRepository.existsByNickname(userDto.getNickname())) {
      throw DuplicateUserException.forNickname(userDto.getNickname());
    }
    if(userRepository.existsByEmail(userDto.getEmail())) {
      throw DuplicateUserException.forEmail(userDto.getEmail());
    }
    
    UserEntity user = UserDto.toEntity(userDto); //받아온 dto를 entity로 해줘야 Repository 사용가능
    user.setPassword(encoder.encode(userDto.getPassword()));

    //DB UNIQUE 예외처리 한번더 
    try {
      UserEntity saved = userRepository.save(user);
      return UserDto.from(saved);
    } catch (Exception e) {
      // 여기서만 정말 DB UNIQUE 제약 위반 등 "진짜 중복"을 잡아줌
      logger.warn("DB 제약 위반으로 회원가입 실패: {}", userDto.getLoginId(), e);
      throw new DuplicateUserException("이미 중복된 회원이 존재합니다.");
    }
    
  }
  
  /**
   * 회원가입시 아이디, 닉네임, 이메일 중복 이벤트 호출 
   */
  public boolean checkDuplUserInfo(String field, String value) {
    
    boolean duplCheck = false;
    
    if(field == "loginId") {
      duplCheck = userRepository.existsByLoginId(value);
    }
    if(field == "nickname") {
      duplCheck = userRepository.existsByNickname(value);
    }
    if(field == "email") {
      duplCheck = userRepository.existsByEmail(value);
    }
    
    return duplCheck;
  }
  
  /**
   * 회원 목록불러오기 (Entity -> Dto)
   * @return 회원리스트(Dto)
   */
  public List<UserDto> getUsers(){
    List<UserDto> userList = new ArrayList<UserDto>();
    
    List<UserEntity> userEList = userRepository.findAll();
    for(UserEntity userE : userEList) {
      userList.add(UserDto.from(userE));
    }
    
    return userList;
  }
  
}
