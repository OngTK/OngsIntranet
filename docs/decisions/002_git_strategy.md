# 002 — Git 브랜치 전략

> 작성일: 2026-10-15  
> 상태: 확정

---

## 결정 사항

OngsIntranet 프로젝트의 Git 브랜치 전략을 아래와 같이 정의한다.

---

## 브랜치 구조

```
main
└── develop
    ├── docs/{스프린트명}-{내용}      # 설계·기획 문서 작업
    ├── feature/{스프린트명}-{기능명} # 기능 코드 구현
    ├── fix/{내용}                   # 버그 수정
    └── refactor/{내용}              # 코드 개선 (기능 변경 없음)
```

### 각 브랜치 역할

| 브랜치 | 직접 커밋 | 역할 |
|--------|-----------|------|
| `main` | 금지 | 배포 브랜치. develop PR 머지만 허용 |
| `develop` | 금지 | 개발 통합 브랜치. 모든 작업 브랜치의 시작점 |
| `docs/*` | 허용 | 설계 문서 전용 |
| `feature/*` | 허용 | 기능 구현 전용 |
| `fix/*` | 허용 | 버그 수정 전용 |
| `refactor/*` | 허용 | 코드 개선 전용 |

---

## 브랜치 네이밍 규칙

```
docs/sprint-01-erd
docs/sprint-01-api-spec
feature/sprint-01-auth
feature/sprint-01-attendance
fix/jwt-expiry-error
refactor/attendance-service
```

---

## 스프린트 단위 작업 흐름

```
develop
  │
  ├─ docs/sprint-XX-design    (설계 문서 작업)
  │       └─ PR → develop 머지
  │
  ├─ feature/sprint-XX-{기능} (코드 구현)
  │       └─ PR → develop 머지
  │
  └─ (스프린트 완료) PR → main 머지 (배포)
```

1. develop에서 `docs/{sprint-XX}-design` 브랜치 생성
2. ERD, API 스펙, 화면 설계 등 문서 작성 완료
3. 검토 후 develop으로 PR 머지
4. develop에서 `feature/{sprint-XX}-{기능명}` 브랜치 생성
5. 코드 구현 및 테스트
6. 완료 후 develop으로 PR 머지
7. 스프린트 전체 완료 시 develop → main PR 머지 (배포)

---

## 커밋 메시지 컨벤션

형식: `{type}: {설명}`

| type | 설명 |
|------|------|
| `feat` | 새 기능 추가 |
| `fix` | 버그 수정 |
| `docs` | 문서 작업 (설계, 주석 등) |
| `refactor` | 코드 개선 (기능 변경 없음) |
| `test` | 테스트 추가·수정 |
| `chore` | 빌드·설정·의존성 변경 |

### 예시

```
feat: 사용자 Google OAuth2 로그인 구현
fix: JWT Access Token 만료 시 갱신 처리 오류 수정
docs: sprint-01 ERD 초안 작성
docs: sprint-01 API 스펙 작성
refactor: AttendanceService 트랜잭션 범위 축소
test: LeaveService 연차 잔여일수 계산 단위 테스트 추가
chore: Flyway 의존성 추가
```

---

## 에이전트(Claude) 작업 시 필수 규칙

1. **작업 시작 전 브랜치 확인**: `git branch` 실행 후 올바른 브랜치인지 확인
2. **항상 develop에서 분기**: `git checkout -b {브랜치명} develop`
3. **설계 문서는 `docs/` 브랜치에서만** 작업
4. **코드 구현은 `feature/` 브랜치에서만** 작업
5. **`main`에 직접 커밋 절대 금지**
6. **develop에 직접 커밋 금지** — 반드시 작업 브랜치에서 PR

---

## 이 전략을 선택한 이유

- **설계 문서와 코드를 브랜치 레벨에서 분리**: `docs/*`와 `feature/*`를 명확히 구분하여 설계 완료 후 코드 작업 시작 흐름을 강제
- **main 보호**: 미검증 코드가 운영에 배포되는 것을 방지
- **스프린트 단위 추적 가능**: 브랜치명에 스프린트 번호를 포함하여 git log만으로 진행 상황 파악 가능
- **단순함**: GitHub Flow보다 약간 엄격하지만, Git Flow보다 단순하여 소규모 팀에 적합
