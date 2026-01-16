# MundusFortunae [실시간 주사위게임+채팅 및 커뮤니티] 
> 본 프로젝트는 학습목적의 개인 프로젝트이지만, <br>
취업 이후에도 참고할 수 있는 기술·설계 자산을 목표로 개발했으며 <br>
기능 추가와 리팩토링을 전제로 한 구조로 설계했습니다.

URL : [https://mundusfortunae-prj.onrender.com/](https://mundusfortunae-prj.onrender.com/)

## 🧩 개발 환경

![Windows 11](https://img.shields.io/badge/OS-Windows%2011-0078D6?logo=windows&logoColor=white)
![Render](https://img.shields.io/badge/Deploy-Render-46E3B7?logo=render&logoColor=white)
![Java](https://img.shields.io/badge/Java-25%20LTS-007396?logo=openjdk&logoColor=white)

## 💻 기술 스택

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?logo=springboot&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?logo=thymeleaf&logoColor=white)
![Spring WebSocket](https://img.shields.io/badge/Spring-WebSocket-6DB33F?logo=spring&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?logo=postgresql&logoColor=white)
![JPA](https://img.shields.io/badge/JPA-Hibernate-59666C?logo=hibernate&logoColor=white)
![MyBatis](https://img.shields.io/badge/MyBatis-3-000000?logo=mybatis&logoColor=white)

## 📖 목차
| 항목 |
|--------|
| [📌 소개](#-소개) |
| [🌟 주요 기능](#-주요-기능) |
| [🔐 인증 및 보안](#-인증-및-보안) |
| [🧠 설계 의사결정](#-설계-의사과정) |
| [🔍 테스트 도구](#-테스트-도구) |

---
## 🎲 소개

이 프로젝트는  
<strong>실시간 통신, 인증/인가, 상태 동기화</strong>와 같은  <span class="mf-accent">웹 백엔드 핵심 개념</span>을  
직접 구현하며 학습하기 위해 설계된 개인 프로젝트입니다.

WebSocket 기반 실시간 통신 경험이 없는 상태에서  
<strong>“직접 만들면서 이해해보자”</strong>는 목표로 시작했으며,  
방 생성, 참여자 관리, 이벤트 브로드캐스트 등  
소켓 통신의 핵심 흐름을 게임이라는 작은 도메인에 담아냈습니다.

완성도를 과시하기보다는  
<strong>기술을 이해해가는 과정과 설계 선택의 이유</strong>를  
기록하고 정리하는 데 더 큰 의미를 두고 있으며,  
현재도 구조 개선과 리팩토링을 중심으로 지속적으로 발전 중입니다.

---
## 🌟 주요 기능 
✨️ 사용자 인증 기반 회원가입 및 로그인 ✨️ 

✨️ 인증 사용자 기반 게시물 작성 및 수정 ✨️ 

✨️ 실시간 보드게임 방 생성 및 참여 ✨️ 

✨️ 게임 참여자 상태의 실시간 동기화 ✨️

---
## 🔐 인증 및 보안

✨️ <a href="https://github.com/mingi-Shin/MundusFortunae_prj/blob/aff782ef55b9d5b30fdd720a55cf7868e55aaab8/MundusFortunae/src/main/java/com/mingisoft/mf/jwt/CustomLoginFilter.java">
LoginFilter</a> ✨️

0. UsernameAuthenticationFilter 클래스 상속
1. 로그인 요청 처리
2. Access/Refresh Token 발급
3. Refresh Token DB 저장
4. SecurityContextHolder 초기화 
   
✨️<a href="https://github.com/mingi-Shin/MundusFortunae_prj/blob/bceff30e154377811b45f4583a1a409f2a45848d/MundusFortunae/src/main/java/com/mingisoft/mf/jwt/JwtFilter.java"> 
JWTFilter </a>✨️

0. 요청마다 Access Token 추출
1. 토큰 검증
2. Claims 기반으로Authentication 생성
3. 인증 및 SecurityContextHolder 초기화
4. Refresh Token Rotation 기반 재발급 및 DB갱신

✨️ 보안 ✨️

<strong>프론트엔드(UI계층)</strong>

Thymeleaf의 Spring Security(sec 문법)을 활용하여,
SecurityContext에 설정된 정보를 기준으로 
UI요소를 조건부로 렌더링하여 사용자 권한에 따른 화면 제어를 수행합니다. 

<strong>컨트롤러(서버계층)</strong>

Controller레벨에서는 @PreAuthorize를 통해 
SecurityContextHolder에 저장된 Authentication을 기반으로 
메서드 실행 전 인가 검증을 수행합니다. <br>
또한 @AuthenticationPincipal을 활용하여
인증된 사용자 정보를 받아 비즈니스 로직에 활용합니다.

---
## 🧠 설계 의사과정

#### ✨️ JWT의 Stateless 인증 설계 및 고찰 ✨️
본 프로젝트는 JWT의 Stateless 특성을 활용한 인증 구조를 설계하였다. <br>
초기 과정에서 accessToekn을 localStorage에 저장하고 클라이언트에서 추출하는 방식은,
SSR환경에서는 페이지 이동시 인증 정보를 활용할 수 없어 (SPA흉내를 낼수는 있겠지만..) 구조적으로 부적합함을 확인.
<br>
이에 따라 인증 시 **1차적으로 HTTP Header**,  
SSR 요청을 고려하여 **2차적으로 HttpOnly Cookie에서 accessToken을 추출**하도록 설계하였다.  
<br>
accessToken은 짧은 유효기간(10분)을 부여하고,  
refreshToken은 탈취 위험을 고려하여 DB와 쿠키에서 이중 관리하며  
Token Rotation 전략을 도입하였다.

- JWT 검증 로직  
  → [JwtUtil.java](src/main/java/com/mingisoft/mf/jwt/JwtUtil.java)

- 인증 쿠키 생성  
  → [CookieUtil.java](src/main/java/com/mingisoft/mf/jwt/CookieUtil.java)

- refreshToken 검증 및 재발급 흐름  
  → [JwtFilter.java](https://github.com/mingi-Shin/MundusFortunae_prj/blob/663b8a5f097781948f726a488af3f40de8ef9b13/MundusFortunae/src/main/java/com/mingisoft/mf/jwt/JwtFilter.java#L70)

#### ✨️ 동시성 + 컬렉션 문제 ✨️
Jmeter를 통해 동시에 여러개의 게임방 생성 요청을 테스트한 결과, 100개 중 4개 꼴로 ConcurrentModificationException 예외가 발생. <br>
현재 원인 파악 중


---
## 🔍 테스트 도구
- Jmeter
- Postman

