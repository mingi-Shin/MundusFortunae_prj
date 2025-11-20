package com.mingisoft.mf.board.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "category")
public class BoardCategoryEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "category_seq")
  private Long categorySeq;
  
  @Column(name = "category_name")
  private String categoryName;
  
  @Column(name = "category_describe")
  private String categoryDescribe;
  
}
