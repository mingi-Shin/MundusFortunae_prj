package com.mingisoft.mf.board.service;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mingisoft.mf.board.Entity.BoardAttachmentEntity;
import com.mingisoft.mf.board.Entity.BoardCategoryEntity;
import com.mingisoft.mf.board.Entity.BoardEntity;
import com.mingisoft.mf.board.dto.BoardDto;
import com.mingisoft.mf.board.dto.MultipartFileDto;
import com.mingisoft.mf.board.dto.PagenationDto;
import com.mingisoft.mf.board.mapper.BoardMapper;
import com.mingisoft.mf.board.repository.BoardAttachmentRepository;
import com.mingisoft.mf.board.repository.BoardCategoryRepository;
import com.mingisoft.mf.board.repository.BoardRepository;
import com.mingisoft.mf.exception.BoardNotFoundException;
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
  private final BoardAttachmentRepository attachRepository;
  private final BoardMapper boardMapper;
  
  //총 게시물 수
  public Long countTotalBoards() {
    return boardMapper.countTotalBoards();
  }
  
  //금일 게시물 수 
  public Long countTodayLong() {
    return boardMapper.countTodayNewBoards();
  }
  
  /**
   * 게시물 리스트 조회 
   * @param categorySeq
   */
  public Map<String, Object> getBoardListByCategorySeq(Long categorySeq, int page){
    
    // 해당 카테고리의 총 게시글 수 가져오고 
    Long countTotalBoards = boardMapper.selectCountBoardByCategorySeq(categorySeq);
    
    // 카테고리 번호, 현재 페이지, 페이지당 게시물 수(고정), 총게시물수 
    PagenationDto pageDto = new PagenationDto(categorySeq, page, 5, countTotalBoards);
    
    List<BoardDto> boardList = boardMapper.selectBoardListByCategorySeq(pageDto);
    if(boardList == null) {
      boardList = Collections.emptyList();
    }
    
    Map<String, Object> boardMap = new HashMap<String, Object>();
    boardMap.put("pageDto", pageDto);
    boardMap.put("boardList", boardList);
    
    return boardMap;
  }
  
  /**
   * 공지 게시물 3개 가져오기 
   */
  public List<BoardDto> getNoticeBoardThree(){
    
    List<BoardDto> boardList = boardMapper.selectLatestThreeNotice();
    if(boardList == null) {
      boardList = Collections.emptyList();
    }
    return boardList;
  }
  
  /**
   * 새로운 게시물 작성 
   * @param boardDto
   * @return
   */
  @Transactional // save/update/delete에 반드시 
  public Long createNewBoard(BoardDto boardDto) {
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

    //1.게시물 테이블 작성
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
    
    BoardEntity result = boardRepository.save(boardEntity);
    
//-------------------------------------------------------------------------------------------------------------------
    
    //2.첨부파일 테이블 작성
    Long boardSeq = result.getBoardSeq();
    MultipartFile image = boardDto.getImageFile();
    //2-1. 이미지 파일 (모두)
    if(image != null && !image.isEmpty()) {
      BoardAttachmentEntity attEntity = new BoardAttachmentEntity();
      
      BoardEntity board = boardRepository.getReferenceById(boardSeq);
      attEntity.setBoardEntity(board);
      
      attEntity.setOriginName(boardDto.getImageFile().getOriginalFilename());
      attEntity.setStoredName(boardDto.getUserSeq() + "_" + boardSeq + "_" + String.valueOf(System.currentTimeMillis()));
      attEntity.setStorageKey("Image Test Storage");//저장경로 
      attEntity.setContentType(boardDto.getImageFile().getContentType());
      attEntity.setFileExt(getExtFromFile(boardDto.getImageFile().getOriginalFilename()));
      attEntity.setFileSize(boardDto.getImageFile().getSize());
      attEntity.setCreatedBy(boardDto.getUserSeq());
      attEntity.setFileType("img");
       
      BoardAttachmentEntity attachImage = attachRepository.save(attEntity);
      logger.info("Image AttachmentSeq() : {}", attachImage.getAttachmentSeq());
    }
    
    //2-2. 문서 파일 (관리자만)
    MultipartFile doc = boardDto.getDocumentFile();
    if(doc != null && !doc.isEmpty()) {
      BoardAttachmentEntity attEntity = new BoardAttachmentEntity();
      
      BoardEntity board = boardRepository.getReferenceById(boardSeq);
      attEntity.setBoardEntity(board);
      
      attEntity.setOriginName(boardDto.getDocumentFile().getOriginalFilename());
      attEntity.setStoredName(boardDto.getUserSeq() + "_" + String.valueOf(System.currentTimeMillis()));
      attEntity.setStorageKey("Doc Test Storage");//저장경로 
      attEntity.setContentType(boardDto.getDocumentFile().getContentType());
      attEntity.setFileExt(getExtFromFile(boardDto.getDocumentFile().getOriginalFilename()));
      attEntity.setFileSize(boardDto.getDocumentFile().getSize());
      attEntity.setCreatedBy(boardDto.getUserSeq());
      attEntity.setFileType("doc");
      
      BoardAttachmentEntity attachDoc = attachRepository.save(attEntity);
      logger.info("Doc AttachmentSeq() : {}", attachDoc.getAttachmentSeq());
    }
   
    return boardSeq;
    
  }

  /**
   * 게시물 업데이트 메서드
   * @param boardDto
   * @return
   */
  @Transactional
  public Long updateOneBoard(BoardDto boardDto) {
    
    //1.게시물 테이블 수정
    BoardCategoryEntity categoryEntity =  categoryRepository.getReferenceById(boardDto.getCategorySeq());
    logger.info("categorySeq={}", boardDto.getCategorySeq());
    
    UserEntity userEntity = userRepository.getReferenceById(boardDto.getUserSeq());
    logger.info("userSeq={}", boardDto.getUserSeq());
    
    BoardEntity boardEntity = boardRepository.findById(boardDto.getBoardSeq())
                                .orElseThrow(() -> BoardNotFoundException.forNoBoard(boardDto.getBoardSeq()));
    
    boardEntity.setTitle(boardDto.getTitle());
    boardEntity.setContent(boardDto.getContent());
    boardEntity.setCategory(categoryEntity);
    
    logger.info("boardEntity : {}", boardEntity);
    
    /**
     * UPDATE는 save()가 필요없다.
     */
    //BoardEntity result = boardRepository.save(boardEntity);
    
//-------------------------------------------------------------------------------------------------------------------
    
    
    //originImageFile 이 존재 O -> board_attachment 테이블 수정 x
    boolean hasOriginImageFile = boardDto.getOriginImageFile() != null && !boardDto.getOriginImageFile().isBlank(); // ""도 안됨 
    boolean hasNewImageFile = boardDto.getImageFile() != null;

    // 1. originImageFile 이 존재 O 
    if(hasOriginImageFile) {
      //board_attachment 수정 없음
    }
    
    // 2. originImageFile 이 존재 X && imageFile이 존재 X 
    if(!hasOriginImageFile && !hasNewImageFile) {
      //기존 이미지 파일 삭제
      attachRepository.deleteByBoardEntity_BoardSeqAndFileType(boardDto.getBoardSeq(), "img");
    }
    
    
    // 3. originImageFile 이 존재 X && imageFile이 존재 O 
    if(!hasOriginImageFile && hasNewImageFile) {
      //기존 이미지 파일 삭제 후에 새로운 이미지 파일 저장 
      attachRepository.deleteByBoardEntity_BoardSeqAndFileType(boardDto.getBoardSeq(), "img");
      
      MultipartFile image = boardDto.getImageFile();
      if(image != null && !image.isEmpty()) {
        BoardAttachmentEntity attEntity = new BoardAttachmentEntity();
        
        BoardEntity board = boardRepository.getReferenceById(boardDto.getBoardSeq());
        attEntity.setBoardEntity(board);
        
        attEntity.setOriginName(boardDto.getImageFile().getOriginalFilename());
        attEntity.setStoredName(boardDto.getUserSeq() + "_" + boardDto.getBoardSeq() + "_" + String.valueOf(System.currentTimeMillis()));
        attEntity.setStorageKey("Image Test Storage");//저장경로 
        attEntity.setContentType(boardDto.getImageFile().getContentType());
        attEntity.setFileExt(getExtFromFile(boardDto.getImageFile().getOriginalFilename()));
        attEntity.setFileSize(boardDto.getImageFile().getSize());
        attEntity.setCreatedBy(boardDto.getUserSeq());
        attEntity.setFileType("img");
         
        BoardAttachmentEntity attachImage = attachRepository.save(attEntity);
        logger.info("Image AttachmentSeq() : {}", attachImage.getAttachmentSeq());
      }
    }
    
//-------------------------------------------------------------------------------------------------------------------
    
    // originDocFile이 존재 O -> board_attachment 테이블 수정 x
    boolean hasOriginDocFile = boardDto.getOriginDocFile() != null && !boardDto.getOriginDocFile().isBlank();
    boolean hasNewDocFile = boardDto.getDocumentFile() != null;
    
    
    // 1. originDocFile 이 존재 O 
    if(hasOriginDocFile) {
      //board_attachment 수정 없음
    }
    
    // 2. originDocFile 이 존재 X && hasNewDocFile이 존재 X 
    if(!hasOriginDocFile && !hasNewDocFile) {
      //기존 이미지 파일 삭제
      attachRepository.deleteByBoardEntity_BoardSeqAndFileType(boardDto.getBoardSeq(), "doc");
    }
    
    // 3. originDocFile 이 존재 X && hasNewDocFile이 존재 O 
    if(!hasOriginDocFile && hasNewDocFile) {
      //기존 문서 파일 삭제 후에 새로운 문서 파일 저장 
      attachRepository.deleteByBoardEntity_BoardSeqAndFileType(boardDto.getBoardSeq(), "doc");
      
      MultipartFile doc = boardDto.getDocumentFile();
      if(doc != null && !doc.isEmpty()) {
        BoardAttachmentEntity attEntity = new BoardAttachmentEntity();
        
        BoardEntity board = boardRepository.getReferenceById(boardDto.getBoardSeq());
        attEntity.setBoardEntity(board);
        
        attEntity.setOriginName(boardDto.getDocumentFile().getOriginalFilename());
        attEntity.setStoredName(boardDto.getUserSeq() + "_" + boardDto.getBoardSeq() + "_" + String.valueOf(System.currentTimeMillis()));
        attEntity.setStorageKey("Image Test Storage");//저장경로 
        attEntity.setContentType(boardDto.getDocumentFile().getContentType());
        attEntity.setFileExt(getExtFromFile(boardDto.getDocumentFile().getOriginalFilename()));
        attEntity.setFileSize(boardDto.getDocumentFile().getSize());
        attEntity.setCreatedBy(boardDto.getUserSeq());
        attEntity.setFileType("doc");
         
        BoardAttachmentEntity attachDoc = attachRepository.save(attEntity);
        logger.info("Doc AttachmentSeq() : {}", attachDoc.getAttachmentSeq());
      }
    }
    
    return boardDto.getBoardSeq();
  }
  
  /**
   * ext 이름 꺼내기 
   * @param fileName
   * @return ext
   */
  private String getExtFromFile(String fileName) {
    int dot = fileName.lastIndexOf(".");
    String ext = fileName.substring(dot+1);
    return ext;
  }
  
  /**
   * 게시물 자세히 조회
   */
  @Transactional
  public BoardDto getOneBoardByBoardSeq(Long boardSeq) {
    if(boardSeq == null) throw BoardNotFoundException.forNoBoard(boardSeq);
    
    BoardDto board = boardMapper.selectBoardDetailByBoardSeq(boardSeq);
    //클릭햇는데 글이 그새 사라졌을때
    if(board == null) throw BoardNotFoundException.forNoBoard(boardSeq); // RuntimeExeption 
    
    //조회수 증가 
    int updated = boardRepository.viewHitPlus(boardSeq);
    //클릭햇는데 글이 그새 사라졌을때 에러처리 
    if(updated == 0) throw BoardNotFoundException.forNoBoard(boardSeq);
    
    //--------------------------------테스트 : 에러코드 몇을 내보내나 
    //if(updated == 1) throw BoardNotFoundException.forNoBoard(boardSeq); 
    
    return board;
  }
  
  /**
   * 게시물 조회 : 첨부파일들 가져오기 (서비스는 null 금지, 빈거라면 empty 주기)
   */
  public List<MultipartFileDto> getBoardFilesByBoardSeq(Long boardSeq) {
    
    if(boardSeq == null) throw new IllegalArgumentException("boardSeq is null");
    
    List<MultipartFileDto> fileList = boardMapper.selectAttachmentsByBoardSeq(boardSeq);
    
    return fileList == null ? Collections.emptyList() : fileList;
  }
  
  @Transactional
  public void deleteBoardByBoardSeq(Long boardSeq) {
    
    boardRepository.deleteById(boardSeq);
    //공지사항이라면 BoardDeleteLog를 테이블에서 관리해도 좋음 
  }
  
  
}
