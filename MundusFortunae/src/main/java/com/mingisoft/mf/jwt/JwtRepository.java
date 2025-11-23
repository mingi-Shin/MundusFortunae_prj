package com.mingisoft.mf.jwt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JwtRepository extends JpaRepository<JwtEntity, Long> {

  /**
   * 엔티티를 변경하는 작업은 반드시 활성화된 @트랜잭션 안에서만 실행해야 한다.(예. Modifying + Query)
   * Entity 기준으로 Query작성해야한다.
   * """ -> 보기좋으라고.. 
   * @param refreshToken
   * @return
   */
  @Modifying 
  @Query("""
      UPDATE JwtEntity j
         SET j.isActive = 'N'
       WHERE j.refreshToken = :token
      """)
  public int deactivateToken(@Param("token") String refreshToken);
  
  public JwtEntity findByRefreshToken(String refreshToken);
  
  
}
