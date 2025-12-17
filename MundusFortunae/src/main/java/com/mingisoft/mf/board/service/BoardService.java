package com.mingisoft.mf.board.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mingisoft.mf.board.Entity.BoardCategoryEntity;
import com.mingisoft.mf.board.Entity.BoardEntity;
import com.mingisoft.mf.board.dto.BoardDto;
import com.mingisoft.mf.board.mapper.BoardMapper;
import com.mingisoft.mf.board.repository.BoardCategoryRepository;
import com.mingisoft.mf.board.repository.BoardRepository;
import com.mingisoft.mf.jwt.CustomUserDetails;
import com.mingisoft.mf.user.UserEntity;
import com.mingisoft.mf.user.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 *  쓰기(Insert/Update/Delete)는 Repository (엔티티/변경감지/트랜잭션 이점)
 *  읽기(복잡한 조회/통계/조인/검색)는 Mapper
 */
@Service
@RequiredArgsConstructor
public class BoardService {

  private final static Logger logger = LoggerFactory.getLogger(BoardService.class);

  private final BoardRepository boardRepository;
  private final BoardCategoryRepository categoryRepository;
  private final UserRepository userRepository;
  private final BoardMapper boardMapper;
  
  @Transactional // save/update/delete에 반드시 
  public void createNewBoard(BoardDto boardDto) {
    //getReferenceById()는 DB에서 바로 가져오는 게 아니라 ‘가짜 대리인(프록시)’을 먼저 준다
/**
JPA에서 왜 엔티티를 넣게 되어있나???
boardEntity.setUser(userEntity) 형태로 객체를 넣게 해둔 건 객체 지향적으로 모델링하기 위해서예요.

JPA는 boardEntity.setCategory(...)에 카테고리 엔티티 객체를 넣게 설계돼 있어요.
하지만 이건 “객체 지향 모델” 때문에 그런 거고, 실제 저장할 때는 결국 그 엔티티의 ID만 꺼내서 FK로 넣습니다.
getReferenceById()가 하는 일 = “ID만 가진 가짜 카테고리(프록시)”
getReferenceById(3)은 이렇게 이해하면 됩니다:
“category_seq=3을 가리키는 표지판(참조) 하나 줄게”
“카테고리의 상세 정보(name 등)가 필요해지면 그때 DB에서 가져올게”
그래서 boardEntity.setCategory(categoryProxy)는 완전 OK예요.
왜냐면 글 저장엔 ID만 있으면 되니까요.
그럼 언제 문제가 되냐?
프록시는 “상세 정보가 필요할 때” DB 조회를 합니다. 예를 들어:
categoryEntity.getName() 같은 필드 접근
logger.info("{}", categoryEntity)가 내부적으로 toString() 하면서 필드를 건드림
컨트롤러에서 JSON 응답으로 엔티티를 그대로 내려서 직렬화가 필드를 건드림
이런 순간에 DB 조회가 발생하고, 그때 트랜잭션/세션이 닫혀있으면 예외가 터질 수 있어요.
*/
    BoardCategoryEntity categoryEntity =  categoryRepository.getReferenceById(boardDto.getCategorySeq());
    logger.info("categorySeq={}", boardDto.getCategorySeq());
    
    UserEntity userEntity = userRepository.getReferenceById(boardDto.getUserSeq());
    logger.info("userSeq={}", boardDto.getUserSeq());
    
    BoardEntity boardEntity = new BoardEntity();
    boardEntity.setCategory(categoryEntity);
    boardEntity.setUser(userEntity);
    boardEntity.setTitle(boardDto.getTitle());
    boardEntity.setContent(boardDto.getContent());
    
    logger.info("boardEntity : {}", boardEntity);
    
    boardRepository.save(boardEntity);
  }
  
  
}
