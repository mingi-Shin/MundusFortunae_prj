package com.mingisoft.mf.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mingisoft.mf.jwt.CookieUtil;
import com.mingisoft.mf.jwt.CustomLoginFilter;
import com.mingisoft.mf.jwt.JwtFilter;
import com.mingisoft.mf.jwt.JwtService;
import com.mingisoft.mf.jwt.JwtUtil;

@Configuration
@EnableWebSecurity(debug=false) //개발중 디버깅 
public class SecurityConfig {

  private final JwtUtil jwtUtil;
  private final JwtService jwtService;
  private final CookieUtil cookieUtil;
  private final ObjectMapper objectMapper;
  
  public SecurityConfig(JwtUtil jwtUtil, ObjectMapper objectMapper, JwtService jwtService, CookieUtil cookieUtil) {
    this.jwtUtil = jwtUtil;
    this.jwtService = jwtService;
    this.cookieUtil = cookieUtil;
    this.objectMapper = objectMapper;
  }
  
  @Bean
  public BCryptPasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }
/**  
  @Bean
  public AuthenticationManager authManager(HttpSecurity http) throws Exception {
    //로그인 검증 설정 빌더의 객체 생성  
    AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
    //객체를 커스터마이징 
    authenticationManagerBuilder
      //아이디로 로그인 찾는 방법은 이거 써라 
      .userDetailsService(null)
      //비밀번호 비교는 이 인코더를 써라 
      .passwordEncoder(bCryptPasswordEncoder()); //DaoAuthenticationProvider가 내부적으로: passwordEncoder.matches(rawPassword, encodedPasswordFromDB) 수행
    
    return authenticationManagerBuilder.build();
    //자세한 설명 : https://hushed-scallop-89c.notion.site/SecurityConfig-2b0e2244683d800897d9d822727a6b0e
  }
*/
  
  /**스프링부트 3 + 시큐리티 6에서는
   * AuthenticationManager는 직접 빌더로 만들지 말고,
   * Spring이 미리 구성한 걸 AuthenticationConfiguration에서 꺼내쓴다. 안그럼 충돌오류나 
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
      return config.getAuthenticationManager(); // 부트가 만든 전역 Manager 참조만 함
  }
  
  @Bean
  public SecurityFilterChain customSecurityFilterChain(HttpSecurity http, AuthenticationManager authManager) 
    throws Exception {
    
    http
      .csrf(csrf -> csrf.disable()) //jwt를 SPA환경에서 쓰면 (header), CSRF토큰 사용할 필요 없음
      .authorizeHttpRequests(auth -> auth
        //극초반에는 모두 오픈하고 개발
        .requestMatchers("/**").permitAll()
        
        /**
         * hasRole은 내부적으로 ROLE_을 붙여서 비교한다. SecurityContextHolder에 ROLE_이 붙여 있어야 매치 가능 
         */
        // --  좁은 경로 → 넓은 경로 순서 -- 
          // --- 정적 리소스
          
      )
      .exceptionHandling(exception -> exception
        // 비로그인 접근 시 → 로그인 페이지로
        .authenticationEntryPoint((req, res, ex) -> res.sendRedirect(req.getContextPath() + "/login"))
        // 로그인은 했지만 권한이 부족할 때
        .accessDeniedHandler((req, res, ex) -> res.sendRedirect(req.getContextPath() + "/error/403"))
      )
      .httpBasic(basic -> basic.disable()) //HTTP Basic 인증필터 추가: 브라우저의 인증 팝업 창이나 API 클라이언트(Postman, curl 등)를 통해 인증이 가능
      .formLogin(form -> form.disable())
      .logout(logout -> logout.disable())
      //아래는 JWT작업 
      .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); //세션사용안함, 
    
      //jwt용 LoginFilter 생성 및 연동 
      CustomLoginFilter customLoginFilter = new CustomLoginFilter(jwtUtil, cookieUtil, jwtService, objectMapper);
      customLoginFilter.setAuthenticationManager(authManager); // 부모 클래스 필드에 주입, 내부에서 get으로 꺼내씀 -> 정석 
      customLoginFilter.setFilterProcessesUrl("/api/auth/login");
    
      //필터 등록 (Jwtfilter -> loginFilter)
      http
        .addFilterAt(customLoginFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(new JwtFilter(jwtService, jwtUtil, cookieUtil), UsernamePasswordAuthenticationFilter.class);
    
    return  http.build();
  }

}
