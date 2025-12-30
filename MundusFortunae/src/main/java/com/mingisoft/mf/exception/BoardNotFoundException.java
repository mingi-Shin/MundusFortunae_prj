package com.mingisoft.mf.exception;

public class BoardNotFoundException extends BusinessException {

  public BoardNotFoundException(String message) {
    super(message, "BoardNotFoundException");
  }

  public static BoardNotFoundException forNoBoard(Long boardSeq) {
    String boardSeqStr = String.valueOf(boardSeq);
    return new BoardNotFoundException("해당 게시물을 찾을 수 없습니다 : (" + boardSeqStr + ")");
  }

  
}
