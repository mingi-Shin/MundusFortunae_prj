package com.mingisoft.mf.exception;

import java.net.BindException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 
 */
@ControllerAdvice
public class GlobalExcepttionHandler {
  
  private static final Logger logger = LoggerFactory.getLogger(GlobalExcepttionHandler.class);

  //404
  @ExceptionHandler({BoardNotFoundException.class})
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public String handleBoardNotFound(Exception ex, HttpServletResponse response, Model model) {
    
    logger.warn("Board not found", ex); // 스택트레이스 포함
    model.addAttribute("message", ex.getMessage());
    return "error/404";
  }
  
  
  
  
}
