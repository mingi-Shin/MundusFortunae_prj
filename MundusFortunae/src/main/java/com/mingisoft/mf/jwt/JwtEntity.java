package com.mingisoft.mf.jwt;

import com.mingisoft.mf.user.UserEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="JWT_REFRESH")
public class JwtEntity {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "refresh_seq")
  private Long refreshSeq;
  
  // FK: users.user_seq
  @ManyToOne(fetch = FetchType.LAZY) //jwt : many, user : one  
  @JoinColumn(name = "user_seq", nullable = false)
  private UserEntity user;
  
  @Column(name = "refresh_token", nullable = false, length = 512)
  private String refreshToken;

  // EPOCH(ms)
  @Column(name = "created_at", nullable = false)
  private Long createdAt;

  // EPOCH(ms)
  @Column(name = "expires_at", nullable = false)
  private Long expiresAt;

  // CHAR(1) 'Y' / 'N'
  @Column(name = "is_active", length = 1)
  private String isActive = "Y";

}
