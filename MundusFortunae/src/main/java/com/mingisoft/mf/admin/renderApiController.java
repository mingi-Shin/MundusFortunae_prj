package com.mingisoft.mf.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class renderApiController {

  /**
   * render잠들지 않게 uptimeRobot에서 보내는 ping 요청 컨트롤러 : jsp렌더링 부하 줄이기 위함 
   * @return
   */
  @GetMapping("/health")
  @ResponseBody
  public String getHealth() {
    return "server is ok";
  }
  
  
}
