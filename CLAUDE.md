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

- **최종 업데이트**: 2026-10-15
- **완료된 스프린트**: 없음
- **진행 중**: 초기 설계
- **다음 작업**: 프로젝트 초기 세팅 (Spring Boot, React)
- **미결 사항**: 없음

## 알려진 이슈

- 없음
