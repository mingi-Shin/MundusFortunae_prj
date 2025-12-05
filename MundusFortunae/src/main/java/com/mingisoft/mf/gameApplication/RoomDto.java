package com.mingisoft.mf.gameApplication;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDto {

  private Long roomSeq;
  private String title;
  private String password;
  
  @Builder.Default
  private int maxPlayerCount = 6;
  
  @Builder.Default
  private List<PlayerDto> playerList = new ArrayList<PlayerDto>();
  
  //  builder로 maxPlayerCount, playerList 안 채웠을 때도 기본값이 들어감
  // 굳이 따로 생성자를 안 만들어도 됨 (직접 생성자 필요하면 유지해도 되고)
}
