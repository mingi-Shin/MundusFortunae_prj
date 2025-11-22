package com.mingisoft.mf.jwt;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JwtRepository extends JpaRepository<JwtEntity, Long> {

}
