package com.mingisoft.mf.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.javassist.bytecode.DuplicateMemberException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mingisoft.mf.exception.DuplicateUserException;

@Service
public class UserService {

  private final Logger logger = LoggerFactory.getLogger(UserService.class);
  private final UserRepository userRepository;
  
  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }
  
  /**
   * 회원가입
   */
  public UserDto saveUser(UserDto userDto) {
    
    //아이디 중복 검사 
    if(userRepository.findByLoginId(userDto.getLoginId())) {
      throw new DuplicateUserException("이미 사용 중인 아이디입니다.");
    }
    //닉네임 중복 검사
    if(userRepository.findByNickname(userDto.getNickname())) {
      throw new DuplicateUserException("이미 사용 중인 닉네임입니다.");
    }
    //이메일 중복 검사
    if(userRepository.findByEmail(userDto.getEmail())) {
      throw new DuplicateUserException("이미 사용 중인 이메일입니다.");
    }
    //DB UNIQUE 예외처리 한번더 
    try {
      UserEntity user = UserDto.toEntity(userDto); //받아온 dto를 entity로 해줘야 Repository 사용가능
      userRepository.save(user);
    } catch (Exception e) {
      throw new DuplicateUserException("이미 중복된 회원이 존재합니다.");
    }
    return userDto;
    
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
