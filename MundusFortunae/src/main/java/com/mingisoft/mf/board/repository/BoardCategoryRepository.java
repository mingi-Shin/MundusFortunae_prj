package com.mingisoft.mf.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mingisoft.mf.board.Entity.BoardCategoryEntity;

public interface BoardCategoryRepository extends JpaRepository<BoardCategoryEntity, Long> {

}
