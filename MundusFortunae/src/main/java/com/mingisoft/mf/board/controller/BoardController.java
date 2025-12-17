package com.mingisoft.mf.board.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.mingisoft.mf.api.ApiResponse;
import com.mingisoft.mf.api.ErrorResponse;
import com.mingisoft.mf.board.Entity.BoardEntity;
import com.mingisoft.mf.board.dto.BoardDto;
import com.mingisoft.mf.board.repository.BoardRepository;
import com.mingisoft.mf.board.service.BoardService;
import com.mingisoft.mf.jwt.CustomUserDetails;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class BoardController {

  private final static Logger logger = LoggerFactory.getLogger(BoardController.class);
  
  private final BoardService boardService;
  
  //공지사항 
  @GetMapping("/board/notice")
  public String getNoticeList(Model model) {
    
    model.addAttribute("boardType", "notice");
    model.addAttribute("noticeList", null);
    
    return "board/list";
  }
  
  //자유게시판
  @GetMapping("/board/free")
  public String getFreeList(Model model) {
    
    model.addAttribute("boardType", "free");
    
df    
    model.addAttribute("noticeList", null);
    model.addAttribute("freeList", null);
    
    return "board/list";
  }
  
  //form 페이지 이동 
  /**
   * 아래와 같은 종류가 있다. 
   * @PreAuthorize("hasRole('ADMIN')")
   * @PreAuthorize("hasAuthority('ROLE_ADMIN')")
   * @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
   * @return
   */
  @PreAuthorize("isAuthenticated()") //메서드가 실행되기 “직전”에 Spring Security가 권한을 검사하게 만드는 메서드 보안(annotation)
  @GetMapping("/board/form")
  public String getWriteForm() {
    logger.info("test_board/form 통과중");
    
    return "board/form";
  }
  
  //글쓰기
  @PostMapping("/api/board/new")
  public ResponseEntity<?> createNewBoard(@AuthenticationPrincipal CustomUserDetails me, @RequestBody BoardDto boardDto,
      HttpServletRequest request){
    
    //logger.info("글쓰기 요청 유저(AuthenticationPrincipal) : {}", me);
    Long userSeq = me.getUserDto().getUserSeq();
    boardDto.setUserSeq(userSeq);
    
    try {
      boardService.createNewBoard(boardDto);
      return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(boardDto));
      
    } catch (Exception e) {
      logger.warn("글작성 요청 오류 : {}", e.getMessage(), e);
      ErrorResponse body = new ErrorResponse().of(HttpStatus.BAD_REQUEST, e.getMessage(), request.getRequestURI());
      return ResponseEntity
               .status(HttpStatus.BAD_REQUEST)
               .body(body);
    }
    
  }
  
  
  
  
}
