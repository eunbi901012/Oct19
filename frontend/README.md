# oct19 공통 관리 — frontend

React 18.3.1 / TypeScript 5.6.3 / Vite 5.4.11 / Node 20 / npm 10.
아홉 전용 페이지·snake_case API DTO·서버 접근 판정 shell과 행동 테스트를 작성했습니다.
화면별 정적 계약 추적은 [UI-CONTRACT.md](UI-CONTRACT.md)를 참조하세요.

## 실행하지 않은 검증과 runner 순서

이 작업에서는 패키지 관리자·테스트·빌드·서버·네트워크를 실행하지 않았습니다.
작성된 테스트는 합격 테스트가 아닙니다. lockfile도 조작하지 않았습니다.
파일 도구가 반환한 자동 lint는 skip/가용성 실패이므로 TypeScript 검증 근거가 아닙니다.

아래는 **runner가 승인 후 실행할 명령**이며 이 작업의 실행 로그가 아닙니다.

```sh
cd frontend
# Node 20.x / npm 10.x 환경에서 실제 의존성 해결 및 lockfile 생성
npm install --package-lock-only
npm ci
npm run typecheck
npm test
npm run build
```

`package-lock.json`이 없으면 위 materialization이 `npm ci` 및 infra Docker build의 선행 조건입니다.
의존성은 package.json에 exact pin이며 optional한 install fallback을 Docker에 추가하지 않았습니다.
Dockerfile / .dockerignore / nginx.conf는 infra 담당입니다.

개발 서버는 `npm run dev`, API proxy의 기본 target은 `http://localhost:8080`이며 필요 시
`API_PROXY_TARGET`을 runner에서 지정합니다. 브라우저 API는 항상 상대 `/api/...` 요청입니다.
기존 same-origin 인증 컨텍스트를 사용하며 bearer 토큰 입력·인증 제공자·로그인을 발명하지 않습니다.

## E2E — 외부에서 시작한 실제 환경

`playwright.config.ts`에는 webServer가 없습니다. UI·API·DB 및 승인된 fixture를 외부 runner가
먼저 시작하고 인증 storage state를 제공해야 합니다. 테스트는 API/브라우저를 가로채지 않습니다.

```sh
E2E_BASE_URL=http://localhost:5173 \
E2E_STORAGE_STATE=/absolute/path/to/approved-storage-state.json \
npm run test:e2e
```

Playwright browser 설치도 runner 선행 조건입니다. storage state나 credential은 저장소에 넣지 마세요.
전체 아홉 화면을 접근할 수 있는 승인된 관리 주체와 비어 있지 않은 각 화면 fixture가 필요합니다.
미허용 화면은 명시 skip이며 허용/미허용 직접 진입 자체는 별도 test에서 검증합니다.

정책 민감 쓰기 시험은 다음 **기존 DB의 승인된 값/승인 표시**가 추가로 필요합니다.

- `E2E_MUTATION_APPROVED=true`: 폐기 가능한 통합 DB·합성 fixture에 대한 전체 쓰기/복구 승인.
- `E2E_ORG_PARENT_CODE`: 비순환·기간 정책 검토를 마친 대체 상위조직.
- `E2E_APPROVER_ID`: 테스트용 역할 변경의 승인자로 검증된 사용자; 행위자와의 동일/상이 정책은 미확정.
- `E2E_PERMISSION_WRITE_APPROVED=true`: 자기 console 접근을 회수하지 않는 격리 fixture 승인.

위 값이 없는 경우 해당 정책 쓰기만 명시 skip입니다. 인증/정책 차단이 있는 서버에 대해 통과를
주장하지 않습니다. 테스트는 변경 후 API 재조회로 검증하고 `finally`에서 원래 payload로 복원합니다.
실행 중 강제 종료/서버 정책 거부는 복원을 막을 수 있으므로 폐기 가능한 통합 DB를 사용하세요.
조직 이력처럼 정당한 append-only 부수 효과는 테스트가 삭제하지 않습니다.

## 파일 구성

- `src/App.tsx`: 허용 메뉴 응답 기반 sidebar/header/direct route gate, 아홉 전용 page 등록.
- `src/pages/*Page.tsx`: 각 screen의 필드·고유 레이아웃 종류·typed serializer·전용 client 연결.
- `src/components/ResourcePage.tsx`: 읽기/저장/재조회/취소/UUID/경합 처리; kind별 영역 구성.
- `src/components/OrganizationHistory.tsx`: 현재 조직과 독립된 실제 이력 API와 paging.
- `src/components/lookups.ts`, `codeCandidates.ts`: 서버 후보 전체 페이지, scope/abort/error 처리.
- `src/components/access.ts`: 접근 context와 보수적 내부 URL 실행 가드.
- `src/api/{users,organizations,roles,user_role_assignments,menu_permissions,menus,
  menu_execution_info,code_groups,detail_codes}.ts`: 아홉 전용 기능 client.
- `src/api/client.ts`, `types.ts`: ApiResponse/PageMeta/ApiError와 명시된 DTO.
- `src/styles/tokens.css`: 컴팩트 enterprise shell과 화면별 편집 구조·반응형·상태 표현.
- `tests/unit/{app,states}.test.tsx`, `tests/unit/client.test.ts`: RTL/Vitest 행동/상태/보안 검사.
- `tests/e2e/admin.spec.ts`: 실제 환경 탐색/직접 진입, 아홉 화면 변경·재조회·복구.

## 중요한 한계

클라이언트의 fail-closed route gate는 서버 URL/API 가드를 대체하지 않습니다.
OQ-AUTH-001/002, OQ-ORG-001, OQ-ROLE-001, OQ-UI-001/002/003/004, OQ-DATA-001/002와
OQ-TECH-001은 이 작업으로 해결되지 않았습니다. 위험 URL의 비실행은 frontend 최소 안전장치이며
server allow-list 정책의 확정이 아닙니다. 상세 내용은 UI-CONTRACT.md에 남겼습니다.
