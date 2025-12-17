package com.mingisoft.mf.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mingisoft.mf.board.Entity.BoardEntity;

public interface BoardRepository extends JpaRepository<BoardEntity, Long> {

  
}
