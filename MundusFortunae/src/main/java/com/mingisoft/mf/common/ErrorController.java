package com.mingisoft.mf.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {

  @GetMapping("/error/400")
  public String get400Error() {
    return "error/400";
  }
  @GetMapping("/error/401")
  public String get401Error() {
    return "error/401";
  }
  @GetMapping("/error/403")
  public String get403Error() {
    return "error/403";
  }
  @GetMapping("/error/500")
  public String get500Error() {
    return "error/500";
  }
}
