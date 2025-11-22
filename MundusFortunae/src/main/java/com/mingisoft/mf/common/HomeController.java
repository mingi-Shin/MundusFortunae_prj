package com.mingisoft.mf.common;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HomeController {

  Logger logger = LoggerFactory.getLogger(HomeController.class);
  
  private final String title;
  
  public HomeController() {
    this.title = "MundusFortunae - All That Destiny";
  }
  
  @GetMapping("/")
  public String mainPage(Model model) { //@AuthenticationPrincipal CustomUser userInfo
    
    logger.info("메인페이지 요청");
    
    model.addAttribute("title", title);
    
    /**
       if (userInfo != null) {
            // 사용자 정보 가져오기
            UserEntity userEntity = ur.findById(userInfo.getEmail()).orElse(null);
            if(userEntity == null) { 
                return "redirect:/accessDenied"; 
            }
            
            // 기업회원이면 사용자 메인페이지 접근 차단
            if (userEntity.getCorpEntity() != null) {
                return "redirect:/corp/main";
            }
            
            // UserDTO로 변환 후 model에 추가
            UserDTO user = new UserDTO(userEntity);
            model.addAttribute("user", user);
        }
        
        
        List<NoticeDTO> noticeList = noticeService.getLatestNotices();
        model.addAttribute("noticeList", noticeList);
     */
    
    return "index";
  }
  
  /**
   * 로그인 폼 페이지 
   */
  @GetMapping({"/login", "/login/{username}"})
  public String getLoginForm(@PathVariable(required = false)  String username, Model model) {
    
    model.addAttribute("title", title);
    model.addAttribute("newJoin", username);
    return "join-login/login";
  }
  
  /**
   * 회원가입 폼 페이지 
   */
  @GetMapping("/join")
  public String getJoinForm(Model model) {
    model.addAttribute("title", title);
    
    return "join-login/join";
  }
  
  /**
   * 약관 페이지 
   */
  @GetMapping("/legal/privacy")
  public String getPrivacyPage() {
    return "join-login/privacy";
  }
  @GetMapping("/legal/terms")
  public String getTermsPage() {
    return "join-login/terms";
  }
  
}
