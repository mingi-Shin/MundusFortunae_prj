package com.mingisoft.mf.game;

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
public class RoomDto<T> {

  private Long roomSeq;
  private String title;
  private String password;
  private List<PlayerDto<T>> PlayerList;
  private int maxPlayerCount;
  
  public RoomDto(Long roomSeq, String title, String password) {
    this.roomSeq = roomSeq;
    this.title = title;
    this.password = password;
    this.PlayerList = new ArrayList<PlayerDto<T>>();
    this.maxPlayerCount = 6; //최대 6인묭
  }
}
