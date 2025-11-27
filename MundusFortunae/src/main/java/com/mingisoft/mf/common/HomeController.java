package com.mingisoft.mf.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mingisoft.mf.api.ApiResponse;
import com.mingisoft.mf.game.GameService;
import com.mingisoft.mf.jwt.CookieUtil;
import com.mingisoft.mf.jwt.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {


  Logger logger = LoggerFactory.getLogger(HomeController.class);
  
  private final String title;
  private final JwtService jwtService;
  private final CookieUtil cookieUtil;
  
  public HomeController(JwtService jwtService, CookieUtil cookieUtil) {
    this.title = "MundusFortunae - All That Destiny";
    this.jwtService = jwtService;
    this.cookieUtil = cookieUtil;
  }
  
  @GetMapping("/")
  public String mainPage(Model model) { //@AuthenticationPrincipal CustomUser userInfo
    
    model.addAttribute("title", title);
    
    return "index";
  }
  
  /**
   * 로그인 폼 페이지 
   */
  @GetMapping({"/login", "/login/{username}"})
  public String getLoginForm(@PathVariable(required = false)  String username, @RequestParam(required = false) String invalid, Model model) {
    
    model.addAttribute("title", title);
    model.addAttribute("newJoin", username);
    model.addAttribute("invalid", invalid);
    
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
  
  /**
   * 로그아웃 
   */
  @PostMapping("/api/auth/logout")
  public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response){
    
    //DB수정 (is_Active = N)
    String refreshToken = cookieUtil.resolveToken(request, "REFRESH_TOKEN");
    boolean isUpdated = jwtService.deleteOldRefreshToken(refreshToken);
    
    //쿠키 삭제(access & refresh)
    cookieUtil.clearAllTokenCookie(response);
    
    //응답값 설정(필요없긴함, 연습용임 )
    Map<String, Object> body = new HashMap<String, Object>();
    body.put("data", "정상적으로 로그아웃 되셨습니다.");
    
    // 로그만 다르게 찍고, 응답은 통일
    if (!isUpdated) {
        logger.info("로그아웃 요청: DB에서 해당 refreshToken을 찾지 못했습니다. token={}", refreshToken);
    }
    
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(ApiResponse.of(HttpStatus.OK, "로그아웃 처리되었습니다.", body));
  }
}
