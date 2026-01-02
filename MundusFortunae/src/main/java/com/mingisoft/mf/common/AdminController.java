package com.mingisoft.mf.common;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.mingisoft.mf.api.ApiResponse;
import com.mingisoft.mf.api.ErrorResponse;
import com.mingisoft.mf.board.service.AdminService;

@Controller
public class AdminController {

  private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
  
  private final AdminService adminService;
  
  public AdminController(AdminService adminService) {
    this.adminService = adminService;
    
  }
  
  
  @GetMapping("/admin/filePractice")
  public String getFilePacticeHtml() {
    return "test/filePractice";
  }
  
  @PostMapping("/admin/createFile")
  public ResponseEntity<?> createFile() throws IOException{
    Map<String, Object> data = adminService.createFile();
    
    if((boolean)data.get("result")) {
      ApiResponse<String> res = ApiResponse.of(HttpStatus.ACCEPTED, "파일 생성 성공", data.get("fileName").toString()); 
      return ResponseEntity
              .status(HttpStatus.OK)
              .body(res);//statusCode, message, data
      
    } else {
      return ResponseEntity
              .status(HttpStatus.CONFLICT)
              .body(ErrorResponse.of(HttpStatus.CONFLICT, "파일 존재, 삭제 진행", "admin/createFile"));
    }
  }
  
  @PostMapping("/admin/createDir")
  public ResponseEntity<?> createDir() throws IOException{
    Map<String, Object> data = adminService.createDir();
    
    if((boolean)data.get("result")) {
      ApiResponse<String> res = ApiResponse.of(HttpStatus.ACCEPTED, "폴더 생성 성공", data.get("fileName").toString()); 
      return ResponseEntity
          .status(HttpStatus.OK)
          .body(res);//statusCode, message, data
      
    } else {
      return ResponseEntity
          .status(HttpStatus.CONFLICT)
          .body(ErrorResponse.of(HttpStatus.CONFLICT, "폴더 존재, 삭제 진행", "admin/createDir"));
    }
  }
  
  
  
  
}
