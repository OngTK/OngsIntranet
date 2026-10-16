# OngsIntranet CLAUDE.md

## 프로젝트 목적

사내 직원의 근태 관리, 공지사항 공유, 문서 결재 등을 지원하는 사내 인트라넷 시스템.
Google Workspace 계정 기반으로 운영되며, Google Calendar와 연동하여 일정 관리까지 통합 제공한다.

## 핵심 사용자

| 역할 | 설명 |
|------|------|
| 일반 직원 | 출퇴근 기록, 휴가 신청, 문서 기안, 캘린더, 공지 조회 |
| 부서장 | 팀원 근태 확인, 결재 승인/반려, 공지 작성 (권한 부여 시) |
| 대표 | 전사 관리 화면, 결재 최종 승인, 전사 공지 |
| 시스템 관리자 | 계정/권한 관리, 전사 설정, 공지 CUD 권한 위임 |

## 핵심 기능 (MVP)

### 근태 관리
- 근무 예정 일정 등록 (출퇴근 시간, 출장 여부, 근무 장소: 본사/공사/지사 등)
- 출퇴근 기록
- 휴가·연차 신청 및 승인

### 공지사항
- 공지 CRUD
- 부서별 공지 분리
- 공지 CUD 권한을 대표/시스템 관리자가 일반 직원·부서장에게 위임 가능

### 문서 결재
- 기안 작성 및 결재선 지정
- 승인 / 반려 처리
- 자주 쓰는 양식 템플릿 (예: 물품구입, 간식비, 야근식대 등)

### 캘린더
- 개인 일정 관리
- Google Calendar 연동

### 프로필 관리
- 내 프로필 조회 및 수정
- 다른 직원 프로필 조회

### 화면 분리
- **일반 사용자 화면**: 출퇴근, 캘린더, 문서 결재, 공지 조회, 프로필
- **관리자 화면** (대표, 시스템 관리자): 전사 직원 출퇴근 조회, 전사 문서 관리, 권한 관리

## 기술 스택

- **Frontend**: React + Vite + TypeScript
- **Backend**: Java 17 + Spring Boot 3
- **DB**: MySQL
- **인증**: Google OAuth2 (Google Workspace) + JWT (Access Token + Refresh Token)
- **외부 연동**: Google Calendar API
- **배포**: AWS (EC2, RDS 등)
- **다국어**: 한국어 / 영어 / 일본어 (i18n)
- **지원 환경**: Web + Mobile 브라우저 (반응형)

## 설계 문서 위치

- ERD: `docs/erd.md`
- API 스펙: `docs/api_spec.yaml`
- 아키텍처: `docs/architecture.md`
- 설계 결정: `docs/decisions/`
- 스프린트: `docs/sprints/`

## 코딩 규칙

- **Frontend**
  - 컴포넌트 단위 분리
  - TypeScript strict mode
  - 반응형 레이아웃 (Mobile-first)
  - i18n 키 기반 다국어 처리 (한국어/영어/일본어)
- **Backend**
  - 레이어드 아키텍처: Controller → Service → Repository
  - Spring Security + JWT 필터 기반 인증
  - 역할(Role) 기반 접근 제어 (RBAC): EMPLOYEE / MANAGER / CEO / ADMIN
- **절대 변경 금지**: ERD 핵심 엔티티, API 응답 구조 (변경 시 반드시 논의 후 `docs/decisions/`에 기록)

## 현재 상태

- **최종 업데이트**: 2026-10-16
- **완료된 스프린트**: 없음 (Sprint-01 구현 및 부분 검증 완료, develop 머지 대기)
- **진행 중**: Sprint-01 마무리 단계
- **완료된 작업**:
  - `2026-10-15` 프로젝트 초기 세팅 (Spring Boot 4.1.1 + React 19 + Vite, PR #1 → develop 머지)
  - `2026-10-16` Sprint-01 설계 문서 작성 및 develop 머지
    - `docs/sprints/sprint-01.md`: 스프린트 계획, DB 스키마, API 스펙, TC 8건
    - `docs/decisions/003_auth_flow.md`: 인증 흐름 설계 결정 5건
  - `2026-10-16` Sprint-01 인증 기능 전체 구현 (`feature/sprint-01-auth`)
    - Backend: Flyway V1~V3, JwtProvider, SecurityConfig, OAuth2UserService/SuccessHandler, AuthController, UserController, GlobalExceptionHandler
    - Frontend: AuthContext, PrivateRoute, LoginPage, OAuthCallbackPage, HomePage, Layout, axios.ts 인터셉터
    - 빌드 검증: Gradle compileJava ✅, TypeScript tsc --noEmit ✅
  - `2026-10-16` 통합 테스트 수행 및 발견 버그 6건 수정 (상세: `docs/sprints/sprint-01-result.md`)
    - TC-AUTH-01 ✅ TC-AUTH-02 ✅ TC-AUTH-05 ✅ TC-AUTH-06 ✅
- **다음 작업**:
  1. TC-AUTH-03, 04, 07, 08 수동 검증 (선택)
  2. `feature/sprint-01-auth` → `develop` 머지
  3. Sprint-02 시작 (근태 관리 또는 프로필 관리)
- **미결 사항**:
  - TC-AUTH-03 (AT 만료 자동 재발급): `jwt.access-token-expiry: 5` 임시 설정 후 검증 필요
  - TC-AUTH-04 (RT 만료 로그인 이동): RT 만료 대기 필요
  - TC-AUTH-07 (공개 엔드포인트): `/actuator/health` 미인증 접근 확인 필요
  - TC-AUTH-08 (비활성 계정 차단): DB에서 `is_active=false` 설정 후 검증 필요
  - `feature/sprint-01-auth` → `develop` 머지 대기 중

## 브랜치 전략

### 브랜치 구조
- `main`: 배포용 — 직접 커밋 금지, develop에서만 머지
- `develop`: 개발 통합 브랜치 — 모든 작업의 시작점
- `docs/{스프린트명}-{내용}`: 설계·기획 문서 작업
- `feature/{스프린트명}-{기능명}`: 기능 코드 구현
- `fix/{내용}`: 버그 수정
- `refactor/{내용}`: 코드 개선 (기능 변경 없음)

### 브랜치 prefix 규칙
| prefix | 용도 |
|--------|------|
| docs/ | ERD, API 스펙, 스프린트 계획 등 설계 문서 작업 |
| feature/ | 기능 코드 구현 |
| fix/ | 버그 수정 |
| refactor/ | 코드 개선 |

### 스프린트 단위 작업 순서
1. develop에서 `docs/{sprint-XX}-design` 브랜치 생성
2. 설계 문서 작성 (ERD, API 스펙, 화면 설계)
3. 검토 완료 후 develop으로 머지
4. develop에서 `feature/{sprint-XX}-{기능명}` 브랜치 생성
5. 코드 구현 및 테스트
6. 완료 후 develop으로 머지

### 커밋 메시지 규칙
- `feat`: 새 기능 추가
- `fix`: 버그 수정
- `docs`: 문서 작업 (설계, 주석 등)
- `refactor`: 코드 개선
- `test`: 테스트 추가·수정
- `chore`: 빌드·설정 변경

예시:
- `feat: 사용자 로그인 API 구현`
- `docs: sprint-01 ERD 초안 작성`
- `fix: JWT 토큰 만료 처리 오류 수정`

### 에이전트 작업 시 필수 확인
- 작업 시작 전 반드시 현재 브랜치 확인 (`git branch`)
- 항상 develop에서 새 브랜치 분기
- 설계 문서 작업은 `docs/` 브랜치에서만
- 코드 작업은 `feature/` 브랜치에서만
- `main`에 직접 커밋 금지

## 알려진 이슈

- IntelliJ에서 Lombok `@RequiredArgsConstructor` 사용 클래스에 "Flexible Constructor Bodies (Java 25)" 경고 표시 — Gradle 실제 빌드에는 무관, IDE 인식 문제로 판단
