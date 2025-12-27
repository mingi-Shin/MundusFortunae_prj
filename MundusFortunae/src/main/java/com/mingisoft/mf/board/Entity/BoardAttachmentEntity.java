package com.mingisoft.mf.board.Entity;

import java.time.LocalDateTime;

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
@Table(name = "board_attachment")
public class BoardAttachmentEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY) //연관 객체는 필요할 때 DB에서 늦게 가져와라
  @Column(name = "attachment_seq")
  private Long attachmentSeq;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "board_seq", nullable = false)
  private BoardEntity boardEntity;
  
  @Column(name = "origin_name", nullable = false ) //원본 파일명(사용자 업로드 명)
  private String originName;
  
  @Column(name = "stored_name", nullable = false ) //서버 저장 파일명(UUID 등)
  private String storedName;
  
  @Column(name = "storage_key", nullable = false ) //S3 key 또는 저장 경로(/uploads/2025/12/..)
  private String storageKey;
  
  @Column(name = "content_type", nullable = false ) //MIME type (image/png 등)
  private String contentType;
  
  @Column(name = "file_ext", nullable = false ) //확장자
  private String fileExt;
  
  @Column(name = "file_size", nullable = false ) // bytes
  private Long fileSize;
  
  @Column(name = "file_type", nullable = false) // img || doc
  private String fileType;
  
  /**
   * 새 엔티티 만들고 아무것도 안 넣어도 Java에서 0/false로 들어감 (가장 안전)
   */
  @Column(name = "sort_order", nullable = false) //첨부 순서(대표 이미지 등)
  private int sortOrder;
  @Column(name = "is_deleted", nullable = false ) //논리적 삭제 
  private boolean isDeleted;
  
  @Column(name = "deleted_at", nullable = true )
  private LocalDateTime deletedAt;
  
  @Column(name = "created_by", nullable = true ) //업로더(게시물 작성자와 다를 수도)
  private Long createdBy;
  
  /**
   * insertable=false : INSERT SQL에 created_at을 포함하지 않음 → DB의 DEFAULT NOW()가 들어감
   * updatable=false : 수정 방지(생성일은 보통 고정)
   */
  @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
  private LocalDateTime createdAt; // Postgres TIMESTAMP -> LocalDateTime
  
}
