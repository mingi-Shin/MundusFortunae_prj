package com.mingisoft.mf.board.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import com.mingisoft.mf.api.ApiResponse;
import com.mingisoft.mf.api.ErrorResponse;
import com.mingisoft.mf.board.Entity.BoardEntity;
import com.mingisoft.mf.board.dto.BoardDto;
import com.mingisoft.mf.board.dto.MultipartFileDto;
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
    
    Long categorySeq = 1L;
    List<BoardDto> boardList = boardService.getBoardListByCategorySeq(categorySeq);
    
    model.addAttribute("boardType", "notice");
    model.addAttribute("noticeList", boardList);
    
    return "board/list";
  }
  
  //자유게시판
  @GetMapping("/board/free")
  public String getFreeList(Model model) {
    
    Long categorySeq = 3L;
    List<BoardDto> boardList = boardService.getBoardListByCategorySeq(categorySeq);
    List<BoardDto> boardNotice3 = boardService.getNoticeBoardThree();
    
    model.addAttribute("boardType", "free");
    
    //여기 
    model.addAttribute("noticeList", boardNotice3);
    model.addAttribute("freeList", boardList);
    
    return "board/list";
  }
  
  //게시물 상세보기 요청 
  @PreAuthorize("isAuthenticated()") //메서드가 실행되기 “직전”에 Spring Security가 권한을 검사하게 만드는 메서드 보안(annotation)
  @GetMapping("/board/detail/{boardSeq}")
  public String getOneBoardByBoardSeq(@PathVariable Long boardSeq, @AuthenticationPrincipal CustomUserDetails me, Model model) {
    
    logger.info("요청자, me.getAuthorities : {}", me.getAuthorities());
    
    // 1. 게시물 컨텐츠
    BoardDto board = boardService.getOneBoardByBoardSeq(boardSeq);
    if(board != null) {
      model.addAttribute("board", board);
    }
    
    // 2. 게시물 첨부파일
    List<MultipartFileDto> files = boardService.getBoardFilesByBoardSeq(boardSeq);
    model.addAttribute("files", files);
    
    // 3. 내가 쓴 글을 "수정", "삭제" 버튼 생성
    boolean isAdmin = me.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    boolean isMine = me.getUserSeq().equals(board.getUserSeq());
    
    if(isAdmin || isMine) {
      model.addAttribute("isEditable", true);
    }
    
    //게시물 제목
    String title = board.getTitle() + " | 문두스 포르투나이(Mundus Fortunae)";
    model.addAttribute("title", title);
    
    return "board/detail";
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
  @PreAuthorize("isAuthenticated()")
  @PostMapping("/api/board/new")
  public ResponseEntity<?> createNewBoard(@AuthenticationPrincipal CustomUserDetails me, @ModelAttribute BoardDto boardDto,
      HttpServletRequest request){
    
    //요청자 정보  
    logger.info("요청자(CustomUserDetails) 정보 : {}", me);
    
    //로그인 풀렸을 경우, 거절 
    if(me == null) {
      return ResponseEntity
              .status(HttpStatus.UNAUTHORIZED)
              .body(ErrorResponse.of(HttpStatus.UNAUTHORIZED, "로그인 세션이 만료되었습니다.", request.getRequestURI()));
    }
    
    //문서 첨부 파일이 있는데, 로그인 권한이 부족한 경우(관리자가 아닐때)
    MultipartFile doc = boardDto.getDocumentFile();
    if(doc != null && !doc.isEmpty()) {
      boolean isAdmin = me.getAuthorities().stream().anyMatch( a -> "ROLE_ADMIN".equals(a.getAuthority())); //이게 더 안전 
      
      if(!isAdmin) {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse.of(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.", request.getRequestURI()));
      }
    }
    
    Long userSeq = me.getUserDto().getUserSeq();
    boardDto.setUserSeq(userSeq);
    
    try {
      Long boardSeq = boardService.createNewBoard(boardDto);
      ApiResponse<Long> res = ApiResponse.of(HttpStatus.CREATED, "게시물 작성 성공", boardSeq);
      return ResponseEntity.status(HttpStatus.CREATED).body(res);
      
    } catch (Exception e) {
      logger.warn("글작성 요청 오류 : {}", e.getMessage(), e);
      ErrorResponse body = ErrorResponse.of(HttpStatus.BAD_REQUEST, e.getMessage(), request.getRequestURI());
      return ResponseEntity
               .status(HttpStatus.BAD_REQUEST)
               .body(body);
    }
  }
  
  //글삭제
  @PreAuthorize("isAuthenticated()")
  @PostMapping("/board/delete/{boardSeq}")
  public String deleteBoard(@AuthenticationPrincipal CustomUserDetails me, @PathVariable Long boardSeq) {
    
    logger.info("me.getUserDto().getRole() : {}", me.getUserDto().getRole()); //ROLE_ADMIN
    
    // 권한 확인
    BoardDto board = boardService.getOneBoardByBoardSeq(boardSeq);
    
    if("notice".equals(board.getCategoryName()) && "ROLE_ADMIN".equals(me.getUserDto().getRole())) {
      삭제 --
      boardService.
    }
    
    
    return "";
  }
  
  
  
}
