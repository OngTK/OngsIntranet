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
- [x] 인증 플로우 결정 사항 문서화 (`docs/decisions/003_auth_flow.md`)

### Backend (`feature/sprint-01-auth`)
- [x] `departments` 테이블 기본 구조 생성 (users FK 선행 조건) — `id`, `name`, `created_at`만 포함, 상세 설계는 이후 스프린트
- [x] Google OAuth2 의존성 추가 (`spring-boot-starter-oauth2-client`)
- [x] `application.yml` Google OAuth 클라이언트 설정 (환경 변수로 관리)
- [x] `User`, `RefreshToken` JPA 엔티티 작성
- [x] `UserRepository`, `RefreshTokenRepository` 작성
- [x] `JwtProvider` — 토큰 생성·검증·파싱 유틸리티
- [x] `JwtAuthenticationFilter` — 요청마다 Access Token 검증 후 SecurityContext 설정
- [x] `SecurityConfig` — Spring Security 필터 체인 구성
- [x] `OAuth2UserService` — Google 계정 정보로 User 조회/생성
- [x] `AuthController` — `/auth/refresh`, `/auth/logout` 엔드포인트
- [x] `OAuth2SuccessHandler` — 로그인 성공 시 JWT 발급 및 프론트엔드 리다이렉트
- [x] `GlobalExceptionHandler` — 인증·인가 에러 응답 표준화

### Frontend (`feature/sprint-01-auth`)
- [x] React Router 설정 (기본 라우트 구조)
- [x] `AuthContext` 및 `useAuth` 훅
- [x] `PrivateRoute` 컴포넌트
- [x] 로그인 페이지 (`/login`) — Google OAuth 버튼
- [x] OAuth 콜백 페이지 (`/auth/callback`) — 토큰 파싱 및 저장
- [x] Axios 인스턴스 — 인터셉터 (요청 헤더 주입, 401 자동 재시도)
- [x] 기본 레이아웃 + 헤더 (로그아웃 버튼 포함)
- [x] 홈 페이지 skeleton (인증 후 진입점)

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

## 환경변수 목록

| 변수명 | 설명 | 예시 |
|--------|------|------|
| `GOOGLE_CLIENT_ID` | Google OAuth 클라이언트 ID | `123...apps.googleusercontent.com` |
| `GOOGLE_CLIENT_SECRET` | Google OAuth 클라이언트 시크릿 | `GOCSPX-...` |
| `JWT_SECRET` | JWT 서명 키 (256bit 이상) | — |
| `JWT_ACCESS_EXPIRATION` | Access Token 만료 (ms) | `1800000` (30분) |
| `JWT_REFRESH_EXPIRATION` | Refresh Token 만료 (ms) | `1209600000` (14일) |
| `FRONTEND_REDIRECT_URI` | 로그인 성공 후 리다이렉트 URL | `http://localhost:5173/auth/callback` |
| `DB_URL` | MySQL 접속 URL | `jdbc:mysql://localhost:3306/ongs_intranet` |
| `DB_USERNAME` | DB 계정 | — |
| `DB_PASSWORD` | DB 비밀번호 | — |

> 로컬 개발: `application-local.yml` + `.env` 파일 사용 (`.gitignore` 등록 필수)  
> 운영: AWS SSM Parameter Store 또는 환경 변수 직접 주입

---

## 패키지 구조 (Backend)

```
com.ongsintranet
  ├── auth        — OAuth2 핸들러, JWT 유틸, Security 설정, AuthController
  ├── user        — User 엔티티, UserRepository, UserService, UsersController
  └── common      — ApiResponse 래퍼, GlobalExceptionHandler, 공통 상수
```

---

## 라우트 구조 (Frontend)

| 경로 | 컴포넌트 | 인증 필요 |
|------|----------|----------|
| `/login` | `LoginPage` | ✗ |
| `/auth/callback` | `OAuthCallbackPage` | ✗ |
| `/` | `HomePage` | ✓ |

> 인증 필요 라우트는 `PrivateRoute`로 감싸며, 미인증 시 `/login`으로 리다이렉트.

---

## DB 테이블 스키마

### users

```sql
CREATE TABLE users (
    id                           BIGINT        NOT NULL AUTO_INCREMENT,
    google_id                    VARCHAR(255)  NOT NULL,
    email                        VARCHAR(255)  NOT NULL,
    name                         VARCHAR(100)  NOT NULL,
    department_id                BIGINT        NULL,
    role                         ENUM('EMPLOYEE','MANAGER','CEO','ADMIN')
                                               NOT NULL DEFAULT 'EMPLOYEE',
    position                     VARCHAR(100)  NULL     COMMENT '직책 (예: 과장)',
    phone                        VARCHAR(20)   NULL,
    profile_image_url            VARCHAR(500)  NULL,
    google_calendar_refresh_token TEXT         NULL     COMMENT '암호화 저장',
    is_active                    BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at                   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                                               ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_google_id (google_id),
    UNIQUE KEY uq_users_email    (email),
    CONSTRAINT fk_users_department
        FOREIGN KEY (department_id) REFERENCES departments (id)
        ON DELETE SET NULL
);
```

### refresh_tokens

```sql
CREATE TABLE refresh_tokens (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    user_id    BIGINT       NOT NULL,
    token      TEXT         NOT NULL COMMENT 'SHA-256 해시 저장',
    expires_at DATETIME     NOT NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE
);
```

> **설계 노트**
> - `refresh_tokens.token`은 원문이 아닌 SHA-256 해시값을 저장한다.  
>   쿠키 탈취 후 DB 직접 접근으로 토큰을 복원하는 공격을 방어하기 위함.
> - `users.google_calendar_refresh_token`은 AES-256 암호화 후 저장 (Spring `@Converter` 사용).
> - `departments` 테이블이 먼저 생성되어 있어야 한다.

---

## API Request / Response 구조

공통 응답 래퍼:
```json
{ "success": true,  "data": { ... }, "error": null }
{ "success": false, "data": null,    "error": { "code": "...", "message": "..." } }
```

---

### GET /auth/google

Google OAuth 로그인 페이지로 리다이렉트. 인증 불필요.

```
GET /auth/google
→ 302 Redirect: https://accounts.google.com/o/oauth2/auth?...
```

---

### GET /auth/google/callback

Google 인증 완료 후 호출되는 콜백. JWT 발급 후 프론트엔드로 리다이렉트.

```
GET /auth/google/callback?code=<authorization_code>
→ 302 Redirect: {FRONTEND_REDIRECT_URI}?token=<accessToken>
  Set-Cookie: refreshToken=<token>; HttpOnly; Secure; SameSite=Lax; Max-Age=1209600; Path=/
```

**에러 시 (비활성 계정 등)**
```
→ 302 Redirect: {FRONTEND_REDIRECT_URI}/login?error=ACCOUNT_DISABLED
```

---

### POST /auth/refresh

Access Token 재발급. Refresh Token은 HttpOnly 쿠키에서 자동 전송.

**Request**
```
POST /auth/refresh
Cookie: refreshToken=<token>
(Body 없음)
```

**Response 200**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGci...",
    "expiresIn": 1800
  },
  "error": null
}
```

**Response 401** — Refresh Token 만료 또는 존재하지 않음
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "REFRESH_TOKEN_INVALID",
    "message": "Refresh Token이 유효하지 않습니다. 다시 로그인해주세요."
  }
}
```

---

### POST /auth/logout

**Request**
```
POST /auth/logout
Authorization: Bearer <accessToken>
Cookie: refreshToken=<token>
(Body 없음)
```

**Response 200**
```json
{
  "success": true,
  "data": null,
  "error": null
}
```
- 응답과 함께 `Set-Cookie: refreshToken=; Max-Age=0; HttpOnly; Path=/` 헤더 포함 (쿠키 삭제)

---

### GET /api/users/me

로그인한 사용자 본인 프로필 조회.

**Request**
```
GET /api/users/me
Authorization: Bearer <accessToken>
```

**Response 200**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "홍길동",
    "email": "hong@company.com",
    "department": "개발팀",
    "position": "과장",
    "profileImageUrl": "https://lh3.googleusercontent.com/...",
    "phone": "010-1234-5678",
    "role": "EMPLOYEE",
    "isActive": true,
    "createdAt": "2026-10-16T09:00:00Z"
  },
  "error": null
}
```

**Response 401** — 토큰 없음 또는 만료
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "인증이 필요합니다."
  }
}
```

---

## 테스트 케이스

### TC-AUTH-01: 정상 로그인

| 항목 | 내용 |
|------|------|
| 전제 조건 | 유효한 Google Workspace 계정 |
| 입력 | `GET /auth/google` 접근 후 Google 계정 선택 |
| 기대 결과 | 프론트엔드 홈(`/`)으로 리다이렉트, Access Token 메모리 저장 확인 |
| 검증 방법 | 리다이렉트 후 `GET /api/users/me` 200 응답 확인 |

### TC-AUTH-02: 최초 로그인 — User 자동 생성

| 항목 | 내용 |
|------|------|
| 전제 조건 | DB에 해당 google_id 없음 |
| 입력 | Google OAuth 로그인 완료 |
| 기대 결과 | `users` 테이블에 레코드 생성, `role=EMPLOYEE`, `is_active=true` |
| 검증 방법 | `GET /api/users/me` 응답의 `role`, `isActive` 확인 |

### TC-AUTH-03: Access Token 만료 시 자동 재발급

| 항목 | 내용 |
|------|------|
| 전제 조건 | 로그인 완료, Access Token 만료된 상태 (만료 시간 단축하여 테스트) |
| 입력 | 만료된 Access Token으로 `GET /api/users/me` 요청 |
| 기대 결과 | Axios 인터셉터가 401 감지 → `/auth/refresh` 자동 호출 → 원래 요청 재시도 → 200 응답 |
| 검증 방법 | Network 탭에서 `/auth/refresh` 요청 및 원래 요청 재시도 확인 |

### TC-AUTH-04: Refresh Token 만료 시 로그인 페이지 이동

| 항목 | 내용 |
|------|------|
| 전제 조건 | Access Token 만료, Refresh Token도 만료 또는 DB에서 삭제된 상태 |
| 입력 | 만료된 Access Token으로 API 요청 |
| 기대 결과 | `/auth/refresh` 401 응답 → 프론트엔드 `/login`으로 리다이렉트 |
| 검증 방법 | `/login` 페이지 진입 확인 |

### TC-AUTH-05: 로그아웃

| 항목 | 내용 |
|------|------|
| 전제 조건 | 로그인 완료 상태 |
| 입력 | 로그아웃 버튼 클릭 → `POST /auth/logout` |
| 기대 결과 | 응답 헤더에 쿠키 삭제 확인, 프론트엔드 `/login` 이동 |
| 검증 방법 | 로그아웃 후 `POST /auth/refresh` 요청 시 401 응답 확인 (Refresh Token 무효화) |

### TC-AUTH-06: 미인증 접근 차단

| 항목 | 내용 |
|------|------|
| 전제 조건 | 비로그인 상태 |
| 입력 | 브라우저에서 `/` 직접 접근 |
| 기대 결과 | `/login`으로 리다이렉트 |
| 검증 방법 | 주소창 URL 확인 |

### TC-AUTH-07: 공개 엔드포인트 인증 불필요 확인

| 항목 | 내용 |
|------|------|
| 전제 조건 | Authorization 헤더 없음 |
| 입력 | `POST /auth/refresh` (쿠키 포함), `GET /auth/google` |
| 기대 결과 | 401 없이 정상 처리 |
| 검증 방법 | HTTP 응답 코드 확인 |

### TC-AUTH-08: 비활성 계정 로그인 차단

| 항목 | 내용 |
|------|------|
| 전제 조건 | `users.is_active = false`인 계정 |
| 입력 | Google OAuth 로그인 완료 |
| 기대 결과 | JWT 미발급, 에러 응답 (`code: ACCOUNT_DISABLED`) |
| 검증 방법 | 로그인 페이지에 에러 메시지 표시 확인 |

---

## 예상 소요

| 영역 | 예상 시간 |
|------|----------|
| Backend | 1~2 세션 |
| Frontend | 1 세션 |
| 통합 테스트 | 0.5 세션 |
