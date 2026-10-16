# Sprint-01 결과: 인증 (Authentication)

> 작성일: 2026-10-16  
> 최종 업데이트: 2026-10-16  
> 브랜치: `feature/sprint-01-auth`

---

## 구현 완료 항목

### Backend
| 파일 | 설명 |
|------|------|
| `db/migration/V1__create_departments_table.sql` | departments 테이블 (users FK 선행 조건) |
| `db/migration/V2__create_users_table.sql` | users 테이블 (google_id, email unique, role enum) |
| `db/migration/V3__create_refresh_tokens_table.sql` | refresh_tokens 테이블 (token SHA-256 해시 저장) |
| `user/domain/User.java` | JPA 엔티티, `User.create()` 팩토리 메서드 |
| `user/domain/Role.java` | EMPLOYEE / MANAGER / CEO / ADMIN enum |
| `auth/domain/RefreshToken.java` | JPA 엔티티, `isExpired()` 메서드 |
| `user/repository/UserRepository.java` | `findByGoogleId`, `findByEmail` |
| `auth/repository/RefreshTokenRepository.java` | `findByToken`, `deleteAllByUserId` |
| `auth/jwt/JwtProvider.java` | Access Token 생성·검증·파싱 |
| `auth/jwt/JwtAuthenticationFilter.java` | Bearer 토큰 추출 → SecurityContext 설정 |
| `auth/config/SecurityConfig.java` | 필터 체인, CORS, 공개 엔드포인트 설정 |
| `auth/oauth2/CustomOAuth2User.java` | OAuth2User 구현체 |
| `auth/oauth2/OAuth2UserService.java` | Google 계정 → User 조회/자동 생성, 비활성 계정 차단 |
| `auth/oauth2/OAuth2SuccessHandler.java` | JWT 발급, Refresh Token 저장, 프론트엔드 리다이렉트 |
| `auth/service/AuthService.java` | refresh (토큰 갱신), logout (무효화) |
| `auth/controller/AuthController.java` | `POST /auth/refresh`, `POST /auth/logout` |
| `user/service/UserService.java` | `getMe` — 본인 프로필 조회 |
| `user/controller/UserController.java` | `GET /api/users/me` |
| `common/exception/ErrorCode.java` | 공통 에러 코드 enum |
| `common/exception/BusinessException.java` | 비즈니스 예외 기본 클래스 |
| `common/exception/GlobalExceptionHandler.java` | 예외 → ApiResponse 변환 |

### Frontend
| 파일 | 설명 |
|------|------|
| `api/axios.ts` | 메모리 기반 Access Token 저장, 401 자동 재발급 인터셉터 |
| `types/user.ts` | `UserDetail` 타입 정의 |
| `contexts/AuthContext.tsx` | `AuthProvider`, `useAuth` 훅, 앱 초기화 시 토큰 복원 |
| `router/PrivateRoute.tsx` | 미인증 시 `/login` 리다이렉트 |
| `pages/LoginPage.tsx` | Google OAuth 버튼, 에러 메시지 표시 |
| `pages/OAuthCallbackPage.tsx` | URL 파라미터에서 토큰 파싱, `history.replaceState`로 URL 정리 |
| `pages/HomePage.tsx` | 인증 후 홈 페이지 (사용자 이름·이메일 표시) |
| `components/Layout.tsx` | 헤더 (프로필 이미지, 이름, 로그아웃 버튼) |
| `App.tsx` | BrowserRouter + AuthProvider + Routes 구성 |

---

## 빌드 결과

| 항목 | 결과 |
|------|------|
| Gradle `compileJava` | ✅ BUILD SUCCESSFUL |
| TypeScript `tsc --noEmit` | ✅ 오류 없음 |

> **IDE 경고 참고**: IntelliJ에서 Lombok `@RequiredArgsConstructor` 사용 클래스에 "Flexible Constructor Bodies (Java 25)" 경고가 표시되나, 실제 Gradle 빌드(Java 17 툴체인)에서는 문제없음. IDE 인식 이슈로 판단됨.

---

## 테스트 케이스 결과

> TC-AUTH-01~08은 Google OAuth2 클라이언트 자격증명 및 실행 중인 DB가 필요하여 **통합 테스트 환경에서 수동 검증** 필요.

| TC | 내용 | 상태 |
|----|------|------|
| TC-AUTH-01 | 정상 로그인 | ✅ 통과 |
| TC-AUTH-02 | 최초 로그인 — User 자동 생성 | ✅ 통과 |
| TC-AUTH-03 | Access Token 만료 시 자동 재발급 | ⏳ 수동 검증 필요 |
| TC-AUTH-04 | Refresh Token 만료 시 로그인 페이지 이동 | ⏳ 수동 검증 필요 |
| TC-AUTH-05 | 로그아웃 및 Refresh Token 무효화 | ✅ 통과 |
| TC-AUTH-06 | 미인증 접근 차단 → `/login` 리다이렉트 | ✅ 통과 |
| TC-AUTH-07 | 공개 엔드포인트 인증 불필요 확인 | ⏳ 수동 검증 필요 |
| TC-AUTH-08 | 비활성 계정 로그인 차단 | ⏳ 수동 검증 필요 |

### 수동 검증 시 준비 사항

1. **환경 변수 설정** (`.env` 또는 IDE Run Configuration)
   ```
   GOOGLE_CLIENT_ID=<Google Cloud Console에서 발급>
   GOOGLE_CLIENT_SECRET=<Google Cloud Console에서 발급>
   JWT_SECRET_KEY=<256bit 이상 랜덤 문자열>
   DB_USERNAME=<MySQL 계정>
   DB_PASSWORD=<MySQL 비밀번호>
   FRONTEND_REDIRECT_URI=http://localhost:5173/auth/callback
   ```

2. **Google Cloud Console 설정**
   - 승인된 리다이렉트 URI에 `http://localhost:8080/login/oauth2/code/google` 추가

3. **TC-AUTH-03 테스트 방법**
   - `application-local.yml`에 `jwt.access-token-expiry: 5` (5초) 임시 설정 후 확인

---

## 통합 테스트 중 발견 및 수정된 버그

| # | 파일 | 현상 | 원인 | 수정 |
|---|------|------|------|------|
| 1 | `build.gradle` | Flyway 마이그레이션 미실행 | Spring Boot 4.x에서 `flyway-core`만 추가하면 자동 구성 미포함 | `flyway-core` → `spring-boot-starter-flyway` |
| 2 | `frontend/src/api/axios.ts` | `/login` 무한 새로고침 | `/auth/refresh` 401 실패 시 인터셉터가 다시 `/auth/refresh` 호출 → `window.location.href='/login'` 반복 | 인터셉터에서 `/auth/refresh` 요청 자체는 재시도 제외 |
| 3 | `auth/oauth2/OAuth2UserService.java` | Google 로그인 500 오류 | Google OIDC 흐름에서 `DefaultOAuth2UserService`가 호출되지 않음 | `OidcUserService` 위임 방식으로 변경 |
| 4 | `auth/oauth2/CustomOAuth2User.java` | `ClassCastException` | `DefaultOidcUser`를 `CustomOAuth2User`로 캐스팅 불가 | `OidcUser` 인터페이스 구현으로 변경 |
| 5 | `auth/config/SecurityConfig.java` | OIDC 사용자 서비스 미적용 | `userService()` 등록 → OIDC 흐름에서 무시됨 | `oidcUserService()`로 변경 |
| 6 | `frontend/vite.config.ts` | OAuth 콜백 무한 리다이렉트 | Vite `/auth` 프록시가 `/auth/callback?token=...`을 백엔드로 전달 → OAuth 재시작 루프 | `/auth` 프록시 규칙 제거 (axios는 절대 URL 사용) |

## 미결 사항

- TC-AUTH-03, TC-AUTH-04, TC-AUTH-07, TC-AUTH-08 수동 검증 미완료
- `users.google_calendar_refresh_token` AES-256 암호화 미구현 (Calendar 스프린트에서 처리)
- CORS 허용 오리진 (`http://localhost:5173`) 하드코딩 → 환경 변수화 검토 (다음 스프린트)

---

## 다음 스프린트 제안

Sprint-02 주제 후보:
- **근태 관리** (출퇴근 기록, 근무 예정 일정)
- **프로필 관리** (내 프로필 조회/수정, 직원 목록)
