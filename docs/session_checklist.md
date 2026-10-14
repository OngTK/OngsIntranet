# 세션 체크리스트

## 세션 시작 전

- [ ] `git pull` — 최신 상태 동기화
- [ ] CLAUDE.md 현재 상태 섹션 확인
- [ ] 진행할 스프린트 문서 확인 (`docs/sprints/sprint_XX.md`)
- [ ] 미결 사항 확인 (`CLAUDE.md` → 미결 사항 섹션)

### Claude Code 첫 프롬프트 (새 세션 시작 시)

```
CLAUDE.md와 docs/ 전체를 읽고 현재 프로젝트 상태를 파악한 뒤,
오늘 진행할 작업을 제안해줘.
```

---

## 작업 중

- [ ] 설계 결정이 생기면 즉시 `docs/decisions/` 에 저장 요청
- [ ] API 스펙 변경 시 `docs/api_spec.yaml` 즉시 업데이트 요청
- [ ] ERD 변경 시 `docs/erd.md` 즉시 업데이트 요청

---

## 세션 종료 전 (필수)

### 1. CLAUDE.md 현재 상태 업데이트

에이전트에게 요청:
```
오늘 작업한 내용 요약해서 CLAUDE.md 현재 상태 섹션 업데이트해줘.
완료 항목, 진행 중 항목, 다음 작업, 미결 사항을 포함해줘.
```

CLAUDE.md 현재 상태 섹션 형식:
```markdown
## 현재 상태
- 최종 업데이트: YYYY-MM-DD
- 완료된 스프린트: Sprint 01, 02
- 진행 중: Sprint 03 — [구체적인 작업]
- 다음 작업: [다음에 할 것]
- 미결 사항: [결정 안 된 것]

## 알려진 이슈
- [이슈 내용 및 원인]
```

### 2. 스프린트 결과 저장 (스프린트 완료 시)

에이전트에게 요청:
```
이번 스프린트 결과를 docs/sprints/sprint_XX_result.md 로 저장해줘.
완료 항목, 테스트 결과, 미완료 항목, 다음 스프린트 전 확인 필요 사항을 포함해줘.
```

### 3. Git Commit & Push

```bash
git add .
git commit -m "docs: sprint XX 완료 및 CLAUDE.md 업데이트"
git push
```

- [ ] CLAUDE.md 업데이트 확인
- [ ] 스프린트 결과 MD 저장 확인
- [ ] git push 완료 확인

---

## 기기 변경 후 재시작 시

```bash
git pull
```

이후 Claude Code에서:
```
CLAUDE.md와 docs/ 전체를 읽고 현재 프로젝트 상태를 파악해줘.
이어서 진행해야 할 작업을 알려줘.
```

---

## 체크 기준 요약

| 상황 | 반드시 할 것 |
|---|---|
| 설계 결정 발생 | `docs/decisions/` 저장 |
| API 변경 | `docs/api_spec.yaml` 업데이트 |
| 스프린트 완료 | `sprint_XX_result.md` 저장 |
| 세션 종료 | `CLAUDE.md` 현재 상태 업데이트 + git push |
| 기기 변경 | `git pull` + CLAUDE.md 읽기 프롬프트 |
