package com.mingisoft.mf.board.repository;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.mingisoft.mf.board.Entity.BoardEntity;

public interface BoardRepository extends JpaRepository<BoardEntity, Long> {

  // 게시물 조회수 증가 : @Modifying 필수(업데이트 쿼리라서)
  @Modifying
  @Query("update BoardEntity b set b.hit = b.hit + 1 where b.boardSeq = :boardSeq")
  public int viewHitPlus(@Param("boardSeq") Long boardSeq);
  
  
}
