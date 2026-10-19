# Frontend 정적 인계 영수증

## 실제 수행

- 요구/계획/UI 계약/OpenAPI를 범위별로 읽고 frontend-design 및 spec-ui-ascii-design을 확인했다.
- `tests/unit/app.test.tsx`와 `tests/e2e/admin.spec.ts`를 production 파일보다 먼저 작성했다.
  이어 공통 client/상태 회귀 시험을 보강했다. 실행된 RED/GREEN 증거는 없다.
- 아래 아홉 전용 page와 기능별 client, 서버 접근 판정 shell, 상태/후보/이력 처리 및 CSS를 작성했다.
- `UI-CONTRACT.md`에 screen별 화면 영역·action·payload·정책 미정을 정적 체크리스트로 남겼다.
- `README.md`에 lockfile/설치/외부 실제 환경 검증 선행조건을 남겼다.
- 읽기 전용 `git status --short --untracked-files=all -- frontend`가 각 frontend 파일을 출력했다.
- `git diff --check -- frontend`의 실제 반환은 exit_code=0, 오류 출력 없음이었다.
  **모든 frontend 파일이 untracked라 tracked diff 검사가 새 소스의 문법·형식을 검증한 것은 아니다.**

## 실제 경로

### 아홉 page

- `frontend/src/pages/UserPage.tsx`
- `frontend/src/pages/OrganizationPage.tsx`
- `frontend/src/pages/RolePage.tsx`
- `frontend/src/pages/UserRoleAssignmentPage.tsx`
- `frontend/src/pages/MenuPermissionPage.tsx`
- `frontend/src/pages/MenuStructurePage.tsx`
- `frontend/src/pages/MenuExecutionInfoPage.tsx`
- `frontend/src/pages/CodeGroupPage.tsx`
- `frontend/src/pages/DetailCodePage.tsx`

### API

`frontend/src/api/`의 `client.ts`, `types.ts`, `users.ts`, `organizations.ts`, `roles.ts`,
`user_role_assignments.ts`, `menu_permissions.ts`, `menus.ts`, `menu_execution_info.ts`,
`code_groups.ts`, `detail_codes.ts`.

### shell / 상태 / 시각

`frontend/src/App.tsx`, `src/main.tsx`, `src/styles/tokens.css`,
`src/components/{ResourcePage,OrganizationHistory}.tsx`,
`src/components/{access,lookups,codeCandidates}.ts`, `src/components/primitives.tsx`.
위 `src/...` 상대 경로는 모두 `frontend/` 아래다.

### 테스트 / 설정 / 문서

- `frontend/tests/unit/{app,states}.test.tsx`
- `frontend/tests/unit/client.test.ts`, `frontend/tests/unit/setup.ts`
- `frontend/tests/e2e/admin.spec.ts`
- `frontend/package.json`, `frontend/tsconfig.json`, `frontend/vite.config.ts`
- `frontend/playwright.config.ts`, `frontend/index.html`, `frontend/.gitignore`
- `frontend/UI-CONTRACT.md`, `frontend/README.md`, 이 영수증

## 실행하지 않은 항목 / 도구 한계

- 이 agent는 npm/다른 패키지 관리자, compiler, test, build, dev server, network 명령을 시작하지 않았다.
- write/patch의 자동 validator는 `.tsx`에 `No linter for .tsx files`, 일부 `.ts`에는
  `npx not usable: This is not the tsc command you are looking for`를 반환했다.
  이것은 허가된 typecheck/test 실행이나 성공 결과가 아니며, 사용 가능한 compiler 검증도 아니다.
- `package-lock.json`은 생성/조작하지 않았다. runner의 실제 dependency materialization이 필요하다.
- Playwright는 자체 webServer 없이 외부 실제 UI/API/DB와 승인된 storage state·합성 fixture를 요구한다.
- 미승인 정책 쓰기 scenario는 환경 승인이 없으면 명시 skip이며 합격 증거가 아니다.

## 소유 범위와 동시 변경

이 agent의 변경은 frontend 아래로 제한했다. infra 소유 Dockerfile/.dockerignore/nginx.conf는
작성/수정하지 않았다. 최종 파일 목록에 이 파일들이 보이는 것은 다른 담당자의 산출물이다.
작업 후반 App.tsx·tokens.css·admin.spec.ts 등에 sibling agent 변경 알림이 있었다.
읽은 최신 내용 위에 국소 patch를 적용해 해당 변경을 보존했다. runner는 동시 변경이 정리된
최종 상태에 대해 다시 typecheck·unit·build·E2E를 수행해야 한다.

## 미해결 의존성

OQ-AUTH-001/002 인증·실효 권한·후보 scope, OQ-ORG-001 기간/순환/소유권,
OQ-ROLE-001 승인자·보직 매핑·회수, OQ-DATA-001/002 동기화·삭제/접속,
OQ-UI-001/002/003/004 IA·추가 필드·실행 안전·시각 승인, OQ-TECH-001 실행 BOM 검증은
미해결이다. frontend의 conservative internal-URL 가드는 server URL 정책을 발명하지 않는다.
