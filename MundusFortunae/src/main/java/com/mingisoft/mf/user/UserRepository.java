package com.mingisoft.mf.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long>{

  public boolean findByLoginId(String loginId);
  public boolean findByEmail(String email);
  public boolean findByNickname(String nickname);
  
  
  
}
/**
Spring Data JPA는 메서드 이름을 분석해서 JPQL을 자동 생성하는 기능이 있다.

대표적인 자동 생성 규칙들:
  • findByNickname(String nick)
  • existsByEmail(String email)
  • deleteByLoginId(String loginId)
  • countByRole(String role)
  • findByEmailContaining(String keyword) (LIKE %keyword%)
  • findByCreatedAtAfter(LocalDateTime time) (createdAt > time)

주의!! 필드명은 Entity를 베이스로 따진다. 

*
*/