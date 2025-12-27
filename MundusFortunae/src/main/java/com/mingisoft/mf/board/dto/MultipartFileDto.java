package com.mingisoft.mf.board.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MultipartFileDto {

  private Long boardSeq;
  private Long attachmentSeq;
  private String storedName;
  private String contentType;
  private String originName;
  private String storageKey;
  
  private LocalDateTime createdAt;
  
  public String getCreatedAtFormatted() {
    if(createdAt == null) {
      return "";
    } else {
      return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
  }
}
