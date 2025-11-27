package com.mingisoft.mf.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 *  리프레시 토큰 삭제 프로시저 
 */
@Component
public class RefreshTokenCleaner {

  private static final Logger logger = LoggerFactory.getLogger(RefreshTokenCleaner.class);
  private final JdbcTemplate jdbcTemplate;
  
  public RefreshTokenCleaner(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }
  
  // (하루 4번 프로시저 호출! 단 서버가 켜져있어야 함 )
  @Scheduled(cron = "0 30 2,8,14,20 * * *")
  public void deleteExpiredTokens() {
    jdbcTemplate.execute("CALL delete_expired_refresh_token_proc()");
    logger.info("Expired tokens deleted at " + java.time.LocalDateTime.now());
  }
}
