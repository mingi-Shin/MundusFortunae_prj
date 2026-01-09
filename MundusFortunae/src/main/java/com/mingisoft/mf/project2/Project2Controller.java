package com.mingisoft.mf.project2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.mingisoft.mf.jwt.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class Project2Controller {
  
  private static final Logger logger = LoggerFactory.getLogger(Project2Controller.class);


  //관리자 메인 페이지 접속
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/admin/project2main")
  public String getProject2Mainpage() {
    
    //방 상태 관리 
    
    return "project2/project2main";
  }
  
  // 각 방 
  @GetMapping("/celebrating/room/{roomSeq}")
  public String getCelebratingRoom(@PathVariable Long roomSeq, Model model, @AuthenticationPrincipal CustomUserDetails user) {
    
    logger.info(" user : {}", user);
    
    return "";
  }
  
  
  
}
