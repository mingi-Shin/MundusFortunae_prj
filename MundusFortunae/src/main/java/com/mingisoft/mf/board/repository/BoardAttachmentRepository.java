package com.mingisoft.mf.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mingisoft.mf.board.Entity.BoardAttachmentEntity;

public interface BoardAttachmentRepository extends JpaRepository<BoardAttachmentEntity, Long> {

  // WHERE board_entity.board_seq = ? AND file_type = ?
  public int deleteByBoardEntity_BoardSeqAndFileType(Long boardSeq, String fileType); 
  //단, 조건 많음 / 대량 삭제 → @Modifying @Query 활용이 더 나음 
  
  
}
