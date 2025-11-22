package com.mingisoft.mf.user;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name="users")
public class UserEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_seq")
  private Long userSeq;

  // DEFAULT now(), DB에서 채우게 할거면 insertable=false
  @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
  private LocalDateTime createdAt;

  @Column(name = "login_id", length = 100, unique = true)
  private String loginId;   // NULL 허용 (social login 대비)

  @Column(name = "password", length = 255)
  private String password;  // NULL 허용 (social login 대비)

  // 기본값은 DB에서 now(), 이후 비밀번호 변경 시 어플리케이션에서 업데이트
  @Column(name = "pw_changed_at", nullable = false, insertable = false)
  private LocalDateTime pwChangedAt;

  @Column(name = "nickname", length = 30, nullable = false, unique = true)
  private String nickname;

  @Column(name = "email", length = 255, nullable = false, unique = true)
  private String email;

  // CHECK (account_status IN ('ACTIVE','SUSPENDED','DELETED'))
  @Column(name = "account_status", length = 50)
  private String accountStatus = "ACTIVE";

  @Column(name = "status_changed_at")
  private LocalDateTime statusChangedAt;

  @Column(name = "last_login_at")
  private LocalDateTime lastLoginAt;

  @Column(name = "email_subscribed", nullable = false)
  private boolean emailSubscribed = false;   // DEFAULT FALSE

  @Column(name = "login_status")
  private boolean loginStatus = false;       // DEFAULT FALSE

  @Column(name = "role", length = 100, nullable = false)
  private String role = "ROLE_USER";              // DEFAULT 'USER'

}
