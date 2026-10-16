# Sprint-01 계획: 인증 (Authentication)

> 작성일: 2026-10-16  
> 브랜치: `docs/sprint-01-design` → `feature/sprint-01-auth`

---

## 목표

Google OAuth2 기반 로그인 및 JWT 인증 플로우 완성.
이후 모든 기능의 기반이 되는 인증·인가 레이어를 확립한다.

---

## 범위

### Backend
- Google OAuth2 로그인 리다이렉트 및 콜백 처리
- JWT Access Token 발급 (단기, 메모리/헤더 전달)
- JWT Refresh Token 발급 및 DB 저장 (`refresh_tokens`)
- HttpOnly Cookie로 Refresh Token 전달
- Access Token 재발급 (`/auth/refresh`)
- 로그아웃 — Refresh Token 무효화 + 쿠키 삭제 (`/auth/logout`)
- Spring Security 필터 구성 (JWT 검증, 인증 컨텍스트 설정)
- 역할 기반 접근 제어 (RBAC) 기본 설정
- 최초 로그인 시 `users` 레코드 자동 생성 (Google 계정 정보로 초기화)

### Frontend
- Google OAuth 로그인 버튼 및 리다이렉트 처리
- 콜백 페이지 — URL 파라미터에서 Access Token 추출 후 메모리 저장
- Axios 인터셉터 — 요청마다 `Authorization: Bearer <token>` 헤더 자동 주입
- 401 응답 시 `/auth/refresh` 자동 호출 후 원래 요청 재시도
- 인증 상태 전역 관리 (Context API)
- 보호 라우트 (`PrivateRoute`) — 비인증 접근 시 로그인 페이지로 리다이렉트
- 로그아웃 처리

---

## 태스크 목록

### 설계 (현재 브랜치)
- [x] Sprint-01 계획 문서 작성 (`docs/sprints/sprint-01.md`)
- [ ] 인증 플로우 결정 사항 문서화 (`docs/decisions/003_auth_flow.md`)

### Backend (`feature/sprint-01-auth`)
- [ ] Google OAuth2 의존성 추가 (`spring-boot-starter-oauth2-client`)
- [ ] `application.yml` Google OAuth 클라이언트 설정 (환경 변수로 관리)
- [ ] `User`, `RefreshToken` JPA 엔티티 작성
- [ ] `UserRepository`, `RefreshTokenRepository` 작성
- [ ] `JwtProvider` — 토큰 생성·검증·파싱 유틸리티
- [ ] `JwtAuthenticationFilter` — 요청마다 Access Token 검증 후 SecurityContext 설정
- [ ] `SecurityConfig` — Spring Security 필터 체인 구성
- [ ] `OAuth2UserService` — Google 계정 정보로 User 조회/생성
- [ ] `AuthController` — `/auth/refresh`, `/auth/logout` 엔드포인트
- [ ] `OAuth2SuccessHandler` — 로그인 성공 시 JWT 발급 및 프론트엔드 리다이렉트
- [ ] `GlobalExceptionHandler` — 인증·인가 에러 응답 표준화

### Frontend (`feature/sprint-01-auth`)
- [ ] React Router 설정 (기본 라우트 구조)
- [ ] `AuthContext` 및 `useAuth` 훅
- [ ] `PrivateRoute` 컴포넌트
- [ ] 로그인 페이지 (`/login`) — Google OAuth 버튼
- [ ] OAuth 콜백 페이지 (`/auth/callback`) — 토큰 파싱 및 저장
- [ ] Axios 인스턴스 — 인터셉터 (요청 헤더 주입, 401 자동 재시도)
- [ ] 기본 레이아웃 + 헤더 (로그아웃 버튼 포함)
- [ ] 홈 페이지 skeleton (인증 후 진입점)

---

## 인증 플로우 요약

```
[Browser]                    [Frontend]                  [Backend]
    |                             |                           |
    | 로그인 버튼 클릭             |                           |
    |----------------------------->|                           |
    |                             | GET /auth/google          |
    |                             |-------------------------->|
    |                             |       302 → Google OAuth  |
    |<------------------------------------------------------------
    | Google 인증 완료            |                           |
    |------------------------------------------------------------>
    |                             |    /auth/google/callback  |
    |                             |       ?code=...           |
    |                             |     Google 토큰 교환      |
    |                             |     User 조회/생성        |
    |                             |     JWT 발급              |
    |<------------------------------------------------------------
    |    302 → /auth/callback     |                           |
    |      ?token=<accessToken>   |                           |
    |----------------------------->|                           |
    |                             | 토큰 파싱 & 메모리 저장   |
    |                             | (Refresh Token은 쿠키)    |
    |                             |                           |
```

**Access Token 전달 방식**: 콜백 URL 쿼리 파라미터 → 프론트에서 즉시 파싱 후 메모리 저장  
**Refresh Token 전달 방식**: HttpOnly Secure Cookie (SameSite=Lax)  
**Access Token 만료**: 30분  
**Refresh Token 만료**: 14일  

---

## 기술 결정 사항

| 항목 | 결정 | 이유 |
|------|------|------|
| Access Token 저장 | 메모리 (JS 변수) | XSS 공격으로 localStorage 탈취 방지 |
| Refresh Token 저장 | HttpOnly Cookie | JS에서 접근 불가 → XSS 방어 |
| 토큰 재발급 트리거 | 401 응답 인터셉터 | 사용자 경험 끊김 없이 자동 갱신 |
| 최초 로그인 처리 | OAuth 콜백에서 User 자동 생성 | 별도 회원가입 플로우 없음 |
| 프론트엔드 리다이렉트 URI | 환경 변수 (`FRONTEND_REDIRECT_URI`) | 개발/운영 환경 분리 |

세부 결정 배경 → [`docs/decisions/003_auth_flow.md`](../decisions/003_auth_flow.md)

---

## 완료 기준

- [ ] Google 계정으로 로그인 → 홈 페이지 진입 성공
- [ ] Access Token 만료 시 자동 재발급 후 원래 요청 정상 처리
- [ ] 로그아웃 시 Refresh Token 무효화 확인 (재사용 불가)
- [ ] 미인증 상태에서 `/` 접근 시 `/login`으로 리다이렉트
- [ ] `GET /api/users/me` 정상 응답 (인증 후)

---

## 예상 소요

| 영역 | 예상 시간 |
|------|----------|
| Backend | 1~2 세션 |
| Frontend | 1 세션 |
| 통합 테스트 | 0.5 세션 |
