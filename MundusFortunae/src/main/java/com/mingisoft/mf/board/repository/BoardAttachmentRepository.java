package com.mingisoft.mf.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mingisoft.mf.board.Entity.BoardAttachmentEntity;

public interface BoardAttachmentRepository extends JpaRepository<BoardAttachmentEntity, Long> {

  
}
