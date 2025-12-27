package com.mingisoft.mf.exception;

public class BoardNotFoundException extends BusinessException {

  public BoardNotFoundException(String message) {
    super(message, "BoardNotFoundException");
  }

  public static BoardNotFoundException forNoBoard(Long boardSeq) {
    String boardSeqStr = String.valueOf(boardSeq);
    return new BoardNotFoundException("존재하지 않는 게시물입니다 : " + boardSeqStr);
  }

  
}
