package com.mingisoft.mf.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDto {

  private Long roomSeq;
  private int playerSeq;
  private String nickname;
  private String role; // HOST or USER
  //private T data; //나중에 쓸일이 있겠지?
  
}
