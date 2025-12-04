package com.mingisoft.mf.socketCommon;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocketPlayerDto {

  private Long roomSeq;
  private int playerSeq;
  private String nickname;
  private int gameScore;
  
}
