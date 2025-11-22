package com.mingisoft.mf.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.mingisoft.mf.user.UserEntity;
import com.mingisoft.mf.user.UserRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdminAccountInitializer {

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;
  private final Logger logger = LoggerFactory.getLogger(AdminAccountInitializer.class);
  
  /**
   *  서버실행시 자동실행되는 어노테이션
   */
  @PostConstruct
  @Transactional
  public void initAdminAccount() {
    
    try {
      boolean exists = userRepository.findByLoginId("admin").isPresent();
      
      if (exists) {
        logger.info("admin 계정 이미 존재");
        return;
      }
      
      UserEntity adminEntity = new UserEntity();
      adminEntity.setLoginId("admin");
      adminEntity.setNickname("관리자");
      adminEntity.setPassword(passwordEncoder.encode("tlsalsrl4260!"));
      adminEntity.setAccountStatus("ACTIVE");
      adminEntity.setEmail("mundusfortunae.help@gmail.com");
      adminEntity.setEmailSubscribed(true);
      adminEntity.setRole("ROLE_ADMIN");
      
      userRepository.save(adminEntity);
      logger.info("admin 계정 자동 생성 완료");
      
    } catch (Exception e) {
      logger.error("AdminAccountInitializer 실행 중 예외: {}", e.getMessage(), e);
    }
    
  }

}
