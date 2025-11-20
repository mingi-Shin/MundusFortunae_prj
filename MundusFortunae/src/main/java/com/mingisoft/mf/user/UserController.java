package com.mingisoft.mf.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

@Controller
public class UserController {

  private final Logger logger = LoggerFactory.getLogger(UserController.class);
  
  private final UserService userService;
  
  public UserController(UserService userService) {
    this.userService = userService;
  }
  
  /**
   * 회원가입 처리 
   */
  //entity dto repository 오늘 한거 정리좀 하자. 깔끔하게. 
  
}
