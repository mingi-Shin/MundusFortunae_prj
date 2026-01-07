package com.mingisoft.mf.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PagenationDto {

  // ====== 입력값 ======
  private Long categorySeq; //카테고리 넘버 
  private int page;        // 현재 페이지 (1부터 시작)
  private int size;        // 페이지당 게시글 수
  private Long totalCount;  // 전체 게시글 수

  // ====== 계산값 ======
  private int totalPage;   // 전체 페이지 수
  private int offset;      // DB 조회 offset

  // ====== 화면용 ======
  private int startPage;   // 페이지네이션 시작 번호
  private int endPage;     // 페이지네이션 끝 번호
  private boolean hasPrev;
  private boolean hasNext;

  private final int blockSize = 10; // 페이지 번호 묶음 크기
  
 //----------------------------------------------------------------------------------------------------------------- 
  
  public PagenationDto(Long categorySeq, int page, int size, Long totalCount) {
    this.categorySeq = categorySeq;
    
    //현재페이지, 페이지당 글자수, 전체 게시물 수 
    this.page = page <= 0 ? 1 : page;
    this.size = size;
    this.totalCount = totalCount;

    // 전체 페이지 수 = (올림) 전체 게시물 수 / 페이지당 게시물 수 
    this.totalPage = (int) Math.ceil((double) totalCount / size);

    // offset 계산 = DB에서 게시물 가져올 때 몇개를 넘길지 
    this.offset = (this.page - 1) * size;

    // 페이지 블록 계산
    this.startPage = ((this.page - 1) / blockSize) * blockSize + 1;
    this.endPage = Math.min(startPage + blockSize - 1, totalPage);

    // 이전 / 다음 버튼 여부
    this.hasPrev = startPage > 1;
    this.hasNext = endPage < totalPage;
  }
  
}
