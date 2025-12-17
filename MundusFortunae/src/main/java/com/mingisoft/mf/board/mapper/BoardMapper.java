package com.mingisoft.mf.board.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.mingisoft.mf.board.dto.BoardDto;
import com.mingisoft.mf.board.dto.BoardDto;

/**
 * 복잡한 조회 담당 
 */
@Mapper
public interface BoardMapper {
  
  //게시물 작성 
  public Long insertNewBoard(Map<String, Object> param);
  
  //카테고리별 게시물 리스트 조회
  public List<BoardDto> selectBoardListByCategory(String categoryName);
  
  //게시물 상세 조회
  //public BoardDto selectBoardDetail(Long boardSeq);
  
  //공지사항 최신 3개 조회
  public List<BoardDto> selectLatestThreeNotice();
  
}
