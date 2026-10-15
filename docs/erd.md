# ERD (Entity Relationship Diagram)

> 최종 업데이트: 2026-10-15  
> **주의**: 핵심 엔티티 변경 시 반드시 `docs/decisions/`에 기록 후 논의

---

## 전체 엔티티 목록

| 영역 | 엔티티 |
|------|--------|
| 공통 | users, departments, refresh_tokens |
| 근태 | work_schedules, attendance_records, leave_types, leave_requests, leave_balances |
| 공지사항 | notices, notice_permissions |
| 문서 결재 | document_templates, documents, approval_lines |
| 캘린더 | calendar_events |

---

## ERD 다이어그램

```mermaid
erDiagram

  %% ─────────────────────────────
  %% 공통
  %% ─────────────────────────────

  departments {`
    BIGINT id PK
    VARCHAR name
    BIGINT parent_id FK "자기 참조 (상위 부서)"
    BIGINT manager_id FK "부서장 users.id"
    DATETIME created_at
    DATETIME updated_at
  }

  users {
    BIGINT id PK
    VARCHAR google_id "Google OAuth sub (unique)"
    VARCHAR email "unique"
    VARCHAR name
    BIGINT department_id FK
    ENUM role "EMPLOYEE|MANAGER|CEO|ADMIN"
    VARCHAR position "직책 (예: 과장)"
    VARCHAR phone
    VARCHAR profile_image_url
    TEXT google_calendar_refresh_token
    BOOLEAN is_active
    DATETIME created_at
    DATETIME updated_at
  }

  refresh_tokens {
    BIGINT id PK
    BIGINT user_id FK
    TEXT token "unique"
    DATETIME expires_at
    DATETIME created_at
  }

  %% ─────────────────────────────
  %% 근태 관리
  %% ─────────────────────────────

  work_schedules {
    BIGINT id PK
    BIGINT user_id FK
    DATE work_date
    TIME start_time
    TIME end_time
    ENUM work_type "OFFICE|BUSINESS_TRIP|REMOTE|OTHER"
    VARCHAR work_location "본사|공사|지사 등 자유 입력"
    TEXT note
    DATETIME created_at
    DATETIME updated_at
  }

  attendance_records {
    BIGINT id PK
    BIGINT user_id FK
    DATE work_date
    DATETIME check_in_at
    DATETIME check_out_at
    ENUM status "NORMAL|LATE|EARLY_LEAVE|ABSENT"
    TEXT note
    DATETIME created_at
    DATETIME updated_at
  }

  leave_types {
    BIGINT id PK
    VARCHAR name "연차|반차|병가|경조사 등"
    DECIMAL days_count "차감 일수 (0.5 반차 가능)"
    BOOLEAN is_active
    DATETIME created_at
  }

  leave_requests {
    BIGINT id PK
    BIGINT user_id FK
    BIGINT leave_type_id FK
    DATE start_date
    DATE end_date
    TEXT reason
    ENUM status "PENDING|APPROVED|REJECTED|CANCELED"
    BIGINT approved_by FK "승인자 users.id"
    TEXT approval_comment
    DATETIME approved_at
    DATETIME created_at
    DATETIME updated_at
  }

  leave_balances {
    BIGINT id PK
    BIGINT user_id FK
    INT year
    DECIMAL total_days
    DECIMAL used_days
    DATETIME updated_at
  }

  %% ─────────────────────────────
  %% 공지사항
  %% ─────────────────────────────

  notices {
    BIGINT id PK
    VARCHAR title
    TEXT content
    BIGINT author_id FK "작성자 users.id"
    BIGINT department_id FK "NULL = 전사 공지"
    BOOLEAN is_pinned
    INT view_count
    DATETIME created_at
    DATETIME updated_at
    DATETIME deleted_at "soft delete"
  }

  notice_permissions {
    BIGINT id PK
    BIGINT user_id FK "권한 부여 대상"
    BIGINT granted_by FK "부여자 users.id"
    DATETIME created_at
  }

  %% ─────────────────────────────
  %% 문서 결재
  %% ─────────────────────────────

  document_templates {
    BIGINT id PK
    VARCHAR name "물품구입|간식비|야근식대 등"
    JSON content_schema "양식 필드 정의"
    BIGINT created_by FK "users.id"
    BOOLEAN is_active
    DATETIME created_at
    DATETIME updated_at
  }

  documents {
    BIGINT id PK
    VARCHAR title
    BIGINT template_id FK "nullable"
    JSON content "기안 내용"
    BIGINT author_id FK "기안자 users.id"
    ENUM status "DRAFT|IN_PROGRESS|APPROVED|REJECTED|CANCELED"
    DATETIME created_at
    DATETIME updated_at
  }

  approval_lines {
    BIGINT id PK
    BIGINT document_id FK
    BIGINT approver_id FK "결재자 users.id"
    INT order_seq "결재 순서"
    ENUM status "PENDING|APPROVED|REJECTED|SKIPPED"
    TEXT comment
    DATETIME acted_at
    DATETIME created_at
  }

  %% ─────────────────────────────
  %% 캘린더
  %% ─────────────────────────────

  calendar_events {
    BIGINT id PK
    BIGINT user_id FK
    VARCHAR google_event_id "Google Calendar event ID (nullable)"
    VARCHAR title
    TEXT description
    DATETIME start_at
    DATETIME end_at
    BOOLEAN is_all_day
    VARCHAR location
    ENUM sync_status "LOCAL|SYNCED|SYNC_FAILED"
    DATETIME created_at
    DATETIME updated_at
  }

  %% ─────────────────────────────
  %% 관계 정의
  %% ─────────────────────────────

  departments ||--o{ users : "소속"
  departments ||--o| departments : "상위 부서"
  users ||--o| departments : "부서장"

  users ||--o{ refresh_tokens : "발급"

  users ||--o{ work_schedules : "등록"
  users ||--o{ attendance_records : "기록"
  users ||--o{ leave_requests : "신청"
  users ||--o{ leave_balances : "보유"
  leave_types ||--o{ leave_requests : "분류"
  users ||--o{ leave_requests : "승인"

  users ||--o{ notices : "작성"
  departments ||--o{ notices : "대상 부서"
  users ||--o{ notice_permissions : "권한 대상"
  users ||--o{ notice_permissions : "권한 부여자"

  document_templates ||--o{ documents : "기반"
  users ||--o{ document_templates : "생성"
  users ||--o{ documents : "기안"
  documents ||--o{ approval_lines : "결재선"
  users ||--o{ approval_lines : "결재자"

  users ||--o{ calendar_events : "소유"
```

---

## 엔티티 상세 설명

### users
- `google_id`: Google OAuth2 로그인 시 발급되는 고유 식별자 (sub 필드)
- `role`: 애플리케이션 내 권한 구분. Google Workspace 그룹과는 별도 관리
- `google_calendar_refresh_token`: Google Calendar API 연동을 위한 Refresh Token (암호화 저장 필수)

### departments
- `parent_id`: 자기 참조로 부서 계층 구조 표현 (예: 본사 > 개발팀)
- `manager_id`: 부서장은 users 테이블 참조. users.department_id와 순환 참조이므로 nullable 처리

### leave_balances
- `(user_id, year)` 복합 unique 제약
- 연차 발생 로직(입사일 기준 등)은 서비스 레이어에서 처리

### notices
- `department_id = NULL`: 전사 공지
- `deleted_at`: soft delete 방식으로 삭제 처리

### notice_permissions
- `(user_id)` unique 제약: 사용자당 하나의 권한 레코드
- 레코드 존재 여부로 권한 판단

### document_templates
- `content_schema`: JSON 형태로 양식 필드 정의 (필드명, 타입, 필수여부 등)

### approval_lines
- `order_seq`: 결재 순서. 낮은 순서부터 순차 결재
- 병렬 결재가 필요하면 동일 `order_seq` 부여 가능

### calendar_events
- `google_event_id`: Google Calendar 연동 이벤트 ID. NULL이면 로컬 전용 이벤트
- `sync_status`: 동기화 상태 추적. SYNC_FAILED 시 재시도 로직 필요

---

## 핵심 제약 조건 (변경 금지)

| 엔티티 | 제약 |
|--------|------|
| users | PK, google_id unique, email unique |
| leave_balances | (user_id, year) unique |
| notice_permissions | user_id unique |
| approval_lines | (document_id, order_seq, approver_id) unique |
| calendar_events | google_event_id unique (not null인 경우) |
