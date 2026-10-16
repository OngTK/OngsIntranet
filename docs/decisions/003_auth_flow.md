# 003 — 인증 플로우 설계 결정

> 작성일: 2026-10-16  
> 관련 스프린트: Sprint-01

---

## 결정 사항

### 1. Access Token — 메모리 저장 (localStorage 미사용)

**결정**: Access Token은 JavaScript 변수(메모리)에만 보관한다.

**근거**:
- `localStorage`는 같은 오리진의 모든 JS에서 접근 가능 → XSS 공격 시 토큰 탈취 위험
- 메모리 저장은 탭/새로고침 시 소멸하지만, Refresh Token 쿠키로 자동 복원 가능
- 만료 시간을 30분으로 짧게 설정해 탈취 피해를 최소화

**트레이드오프**:
- 새로고침 시 잠깐의 토큰 재발급 요청 발생 (사용자에게 체감 없음)

---

### 2. Refresh Token — HttpOnly Cookie

**결정**: Refresh Token은 `HttpOnly; Secure; SameSite=Lax` 쿠키로 전달·저장한다.

**근거**:
- `HttpOnly` → JavaScript에서 접근 불가 → XSS로 탈취 불가
- `Secure` → HTTPS 통신에서만 전송
- `SameSite=Lax` → CSRF 방어 (GET 요청 외 크로스 사이트 전송 차단)
- 만료 시간 14일

**트레이드오프**:
- CSRF 취약점이 완전히 제거되지 않으므로, `/auth/refresh` 엔드포인트는 POST만 허용
- 추후 필요 시 CSRF 토큰 추가 검토

---

### 3. OAuth 콜백 — 쿼리 파라미터로 Access Token 전달

**결정**: 백엔드가 프론트엔드로 리다이렉트 시 `?token=<accessToken>` 형태로 Access Token을 URL에 포함한다.

**근거**:
- 브라우저 리다이렉트 구조상 응답 바디 전달이 불가
- URL fragment(`#token=...`) 방식도 고려했으나, 일부 서버 환경에서 fragment 누락 이슈 존재
- 쿼리 파라미터는 HTTPS 환경에서 전송 중 암호화되므로 네트워크 도청은 방어됨
- 프론트엔드 콜백 페이지에서 즉시 파싱 후 `history.replaceState`로 URL에서 제거

**보완 조치**:
- 콜백 페이지에서 토큰 파싱 즉시 `window.history.replaceState({}, '', '/auth/callback')`로 URL 정리
- Access Token 수명 30분으로 제한

---

### 4. 최초 로그인 시 User 자동 생성

**결정**: Google OAuth 콜백에서 `google_id`로 기존 User를 조회하고, 없으면 자동 생성한다 (별도 회원가입 플로우 없음).

**근거**:
- Google Workspace 계정 기반 사내 서비스 → 외부 가입자 없음
- 신규 직원은 Google 계정 생성 후 첫 로그인만으로 시스템 등록

**초기 생성 필드**:
- `google_id`: OAuth sub
- `email`: Google 계정 이메일
- `name`: Google 프로필 이름
- `profile_image_url`: Google 프로필 사진 URL
- `role`: `EMPLOYEE` (기본값, 관리자가 이후 변경)
- `is_active`: `true`
- `department_id`: `NULL` (관리자가 이후 배정)

---

### 5. Spring Security 필터 구성

**결정**: `JwtAuthenticationFilter`를 `UsernamePasswordAuthenticationFilter` 앞에 등록한다.

**공개(인증 불필요) 엔드포인트**:
- `GET /auth/google`
- `GET /auth/google/callback`
- `POST /auth/refresh`
- `GET /actuator/health` (배포 후 헬스체크용)

**나머지 모든 경로**: 인증 필수

---

## 관련 문서

- ERD: [`docs/erd.md`](../erd.md) — `users`, `refresh_tokens` 엔티티
- API 스펙: [`docs/api_spec.yaml`](../api_spec.yaml) — `/auth/*` 엔드포인트
- Sprint 계획: [`docs/sprints/sprint-01.md`](../sprints/sprint-01.md)
