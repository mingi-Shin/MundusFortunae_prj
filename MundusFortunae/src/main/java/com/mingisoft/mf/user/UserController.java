package com.mingisoft.mf.user;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mingisoft.mf.api.ApiResponse;
import com.mingisoft.mf.api.ErrorResponse;
import com.mingisoft.mf.exception.DuplicateUserException;

import jakarta.servlet.http.HttpServletRequest;

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
  @PostMapping("/api/join")
  @ResponseBody
  public ResponseEntity<?> joinUser(@RequestBody UserDto userDto, HttpServletRequest request){
    logger.info("회원가입 요청 유저 정보 : {}", userDto);
    
    try {
      UserDto user = userService.saveUser(userDto);
      //Map.of 는 실무에서 잦은 실수가 발생해서 잘안씀 
      return ResponseEntity
          .status(HttpStatus.CREATED) // 201
          .body(ApiResponse.created(user));
      
    } catch (DuplicateUserException e) {
      logger.warn("회원가입 중복 에러 : {}", e.getMessage(), e);
      
      ErrorResponse body = new ErrorResponse().of(HttpStatus.CONFLICT, e.getMessage(), request.getRequestURI());
      logger.debug("에러 path : {}", " / 발생시간" , body.getPath(), body.getTimestamp());
      return ResponseEntity
          .status(HttpStatus.CONFLICT) //409
          .body(body);
    }
  }
  
  /**
   * 회원가입시 아이디, 이메일, 닉네임 중복검사 버튼에 대한 메서드
   */
  @GetMapping("/api/join/userInfo")
  @ResponseBody
  public ResponseEntity<?> checkDuplUser(
      @RequestParam("checkField") String checkField, 
      @RequestParam("checkValue") String checkValue){
    
    logger.info("중복처리 호출 ");
    
    try {
      boolean isDuplicated =userService.checkDuplUserInfo(checkField, checkValue);
      return ResponseEntity.ok(isDuplicated);
    } catch (Exception e) {
      logger.warn("중복체크 통신 오류 : {}", e.getMessage(), e);
      return ResponseEntity
              .status(HttpStatus.INTERNAL_SERVER_ERROR)
              .build();
    }
  }
  
}
/**
@Controller: 뷰 템플릿(HTML)을 반환. @ResponseBody를 각 메서드에 추가해야 JSON 반환 가능
@RestController: @Controller + @ResponseBody의 조합. 자동으로 JSON/XML 반환
@Controller에서 ResponseEntity를 반환해도, @ResponseBody가 없으면 Spring은 여전히 뷰 리졸버를 통해 뷰를 찾으려고 합니다.
*/
