package com.mingisoft.mf.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {

  @GetMapping("/error/403")
  public String get403Error() {
    return "error/403";
  }
}
