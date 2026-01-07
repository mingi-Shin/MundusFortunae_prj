package com.mingisoft.mf.user;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

  //가입 유저수 
  public Long selectTotalUserCount();
  
  
}
