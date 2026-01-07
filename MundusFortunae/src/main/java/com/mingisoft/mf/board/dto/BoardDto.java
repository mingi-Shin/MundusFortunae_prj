package com.mingisoft.mf.board.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoardDto {

  private Long boardSeq; //게시물 번호
  private Long categorySeq; //카테고리 번호
  private Long userSeq; //유저번호
  private String title; //제목
  private String content; //내용
  private int hit; //조회수
  private LocalDateTime regDate; //작성날짜 
  private LocalDateTime modifyDate; //수정날짜
  private List<String> tags; //태그 (현재미지원)
  
  private String originImageFile; //수정할때 필요한 변수 
  
  private String nickname; //닉네임
  private String categoryName; //카테고리이름 
  
  /** fm:formatDate 가 LocalDateTime을 지원하지 않기때문에, 만들어주면 편함 */
  public String getRegDateFormatted() {
    if(regDate == null) {
      return "";
    } else {
      return regDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
  }
  
  //파일 (여러개면 List<MultipartFile> 로..)
  private MultipartFile imageFile;
  private MultipartFile documentFile;
  
}
