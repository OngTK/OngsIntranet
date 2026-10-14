# 프로젝트 개발 워크플로우

## 기본 원칙

- **대화형 Claude** → 생각·논의·결정
- **에이전트형 Claude Code** → 문서화·코드 작성·테스트 실행
- **파일이 곧 메모리** → 결정사항은 반드시 파일로 저장

---

## 1단계 — 전체 설계 (프로젝트 시작 시 1회)

> 목적: 에이전트가 흔들림 없이 코드를 작성할 수 있는 기반 문서 확보

### 수행 방식: 대화형 Claude

1. **목적 정의**
   - 해결하려는 문제
   - 핵심 사용자(페르소나)
   - 성공 기준

2. **요구사항 정의**
   - 기능 요구사항 (Functional)
   - 비기능 요구사항 (성능·보안·확장성)
   - 우선순위 (Must / Should / Could / Won't)

3. **ERD 설계**
   - 핵심 엔티티 및 관계 정의
   - → 확정 후 `docs/erd.md` 저장

4. **아키텍처 결정**
   - 기술 스택 확정 (언어·프레임워크·DB·클라우드)
   - 시스템 구성도
   - → 확정 후 `docs/architecture.md` 저장

5. **스프린트 큰 그림 분할**
   - 기능 단위로 스프린트 분류
   - 의존성 고려한 순서 배치
   - → `docs/sprints/` 하위에 저장

### 견고하게 유지해야 하는 것
| 항목 | 이유 |
|---|---|
| 목적·핵심 요구사항 | 바뀌면 모든 설계가 흔들림 |
| ERD | 데이터 구조 변경은 코드 전체에 영향 |
| 아키텍처·기술 스택 | 중간 변경 비용이 매우 높음 |

---

## 2단계 — 스프린트 반복 (매 스프린트마다)

> 목적: 확정된 큰 그림 안에서 기능을 구체화하고 구현

### 수행 방식: 대화형 → 에이전트형

```
대화형: 스프린트 상세 계획 논의
  ↓
대화형: API 설계 (이번 스프린트 범위만)
  ↓
대화형: 화면 설계 (이번 스프린트 범위만)
  ↓
에이전트: 설계 내용 docs/sprints/sprint_XX.md 저장
  ↓
에이전트: 코드 작성 (Backend → Frontend → API 연결)
  ↓
에이전트: 테스트 실행 → 결과 docs/sprints/sprint_XX_result.md 저장
  ↓
사람: 결과 검토 후 다음 스프린트 트리거
```

### 에이전트 실행 프롬프트 구조

```
[참조 문서]
- CLAUDE.md
- docs/sprints/sprint_XX.md
- docs/api_spec.md

[실행 순서]
1. sprint_XX.md의 Backend 항목 구현
2. 단위 테스트 실행 → 실패 시 수정 후 재실행 (최대 3회)
3. sprint_XX.md의 Frontend 항목 구현
4. Backend-Frontend API 연결
5. Whitelist 테스트 케이스 전체 실행
6. 결과를 docs/sprints/sprint_XX_result.md 저장
```

### API 설계와 화면 설계는 스프린트 단위로 구체화

- 처음부터 전체를 완벽하게 만들 필요 없음
- 해당 스프린트 범위만 구체화 → 구현 → 회고 → 다음 스프린트 반영
- 변경사항은 `docs/decisions/` 에 이유와 함께 기록

---

## 3단계 — 스프린트 결과 검토 (매 스프린트 종료 시)

### 결과 MD 확인 항목
- [ ] 완료 항목 검토
- [ ] FAIL 항목 원인 확인
- [ ] 다음 스프린트로 이월할 항목 결정
- [ ] CLAUDE.md 현재 상태 업데이트

---

## 도구별 역할 정리

| 도구 | 역할 |
|---|---|
| **claude.ai (대화형)** | 설계 논의, 방향 결정, 트레이드오프 검토 |
| **VSCode Claude Code** | 대화 + 에이전트 통합 환경 (단일화 권장) |
| **에이전트 (Claude Code)** | 문서 저장, 코드 작성, 테스트 실행 |
| **CLAUDE.md** | 세션 간 컨텍스트 유지 (파일 = 메모리) |
| **docs/** | 설계 문서, 스프린트 계획·결과 저장소 |

---

## 폴더 구조

```
/OngsIntranet
├── CLAUDE.md                      ← 전체 컨텍스트 (항상 최신 유지)
├── /docs
│   ├── erd.md                     ← ERD
│   ├── api_spec.yaml              ← API 명세
│   ├── architecture.md            ← 아키텍처
│   ├── /decisions                 ← 설계 결정 이유 기록
│   │   └── 001_tech_stack.md
│   └── /sprints                   ← 스프린트 계획·결과
│       ├── sprint_01.md
│       └── sprint_01_result.md
├── /frontend                      ← React
│   ├── CLAUDE.md                  ← Frontend 전용 규칙
│   └── /src
└── /backend                       ← Spring Boot
    ├── CLAUDE.md                  ← Backend 전용 규칙
    └── /src
```

---

## Local vs Cloud Session

| | Local Session | Cloud Session |
|---|---|---|
| 실행 위치 | 내 컴퓨터 | Anthropic 클라우드 |
| 노트북 닫으면 | 세션 종료 | 계속 실행 |
| 적합한 경우 | 단시간 작업, 즉각 검토 | 스프린트 전체 실행 등 장시간 작업 |
