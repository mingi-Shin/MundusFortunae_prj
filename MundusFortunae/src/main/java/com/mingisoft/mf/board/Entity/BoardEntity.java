package com.mingisoft.mf.board.Entity;

import java.time.LocalDateTime;

import com.mingisoft.mf.user.UserEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "board")
public class BoardEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "board_seq")
  private Long boardSeq;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_seq", nullable = false)
  private BoardCategoryEntity category;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_seq", nullable = false)
  private UserEntity user;
  
  @Column(name = "title", nullable = false, length = 50)
  private String title;
  
  @Column(name = "content", nullable = false)
  private String content;
  
  @Column(name = "hit")
  private int hit = 0;
 
  //insertable = false 없으면 JPA에서 null이 들어갈수 있으므로 주의, DB에서 default NOW()가 되지 않을수 있음 
  @Column(name = "reg_date", updatable = false, insertable = false)
  private LocalDateTime regDate;
  
  @Column(name = "modify_date")
  private LocalDateTime modifyDate;
  
  @Column(name = "is_deleted")
  private boolean isDeleted;
  
}
