package com.mingisoft.mf.board.service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

  private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

  public Map<String, Object> createFile() throws IOException {
    Map<String, Object> data = new HashMap<String, Object>();
    
    String path = "C:" + File.separator + "Users" + File.separator + "SHIN_Arthur" + File.separator + "Downloads";
    File file = new File(path, "example.txt");
    
    if(file.exists()) {
      logger.warn("파일이 이미 존재합니다, 삭제를 실행합니다. : {}", file.getName());
      file.delete();
      getInfoFile(file);
      data.put("result", false);
      data.put("fileName", file.getName());
      return data;
    }

    //생성
    file.createNewFile();
    getInfoFile(file);
    data.put("result", true);
    data.put("fileName", file.getName());
    return data;
  }
  
  public Map<String, Object> createDir() throws IOException {
    Map<String, Object> data = new HashMap<String, Object>();
    
    String path = "C:" + File.separator + "Users" + File.separator + "SHIN_Arthur" + File.separator + "Downloads";
    File dir = new File(path, "dir");
    
    if(dir.exists()) {
      logger.warn("폴더가 이미 존재합니다, 삭제를 실행합니다. : {}", dir.getName());
      dir.delete(); //폴더안에 파일이 존재하면 삭제안됨 
      data.put("result", false);
      data.put("fileName", dir.getName());
      return data;
    }
    
    //생성
    //dir.mkdir();
    dir.mkdirs(); //필요한 부모 폴더들을 전부 포함해서 연쇄생성
    //new File(dir, "새파일.text").createNewFile();
    
    data.put("result", true);
    data.put("fileName", dir.getName());
    return data;
  }
  
  
  
  
  
  
  
  
  
  private void getInfoFile(File file) throws IOException {
    /**
     * 운영체제에 맞게 파일 경로를 설정하기 위한 메서드
     */
    logger.info(" --- File.separator, 폴더 구분 : {}", File.separator);
    logger.info(" --- File.separator, 경로 구분 : {}", File.pathSeparator);
    logger.info(" --- File.canExecute: {}", file.canExecute());
    logger.info(" --- File.canRead : {}", file.canRead());
    logger.info(" --- File.canWrite : {}", file.canWrite());
    logger.info(" --- File.getAbsolutePath : {}", file.getAbsolutePath());
    logger.info(" --- File.getCanonicalPath : {}", file.getCanonicalPath());
    logger.info(" --- File.getParent : {}", file.getParent());
    logger.info(" --- File.getTotalSpace : {}", file.getTotalSpace());
    logger.info(" --- File.getFreeSpace : {}", file.getFreeSpace());
    logger.info(" --- File.getUsableSpace : {}", file.getUsableSpace());
    logger.info(" --- File.isDirectory : {}", file.isDirectory());
    logger.info(" --- File.isFile : {}", file.isFile());
  }
  
}
