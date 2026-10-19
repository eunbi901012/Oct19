# UI Contract 체크리스트 — oct19 공통 관리

## 근거와 검증 상태

- 정본: `codegen-context/ui-design.md`의 화면 목록·ASCII·UI Contract·공통 상태·시각 제안·IA,
  `codegen-context/contracts/openapi.yaml`, `tasks.md`, `spec.md`, `plan.md`, 루트 `AGENTS.md`.
- 아래 `[x]`는 **소스에 구현/테스트를 작성했다는 정적 체크**이며 실행 합격 표시가 아니다.
- 테스트 파일을 먼저 작성한 다음 production 파일을 작성했다. 설치·RED/GREEN 실행·컴파일·빌드·브라우저
  검증은 지시된 실행 제한 때문에 수행하지 않았다. 자동 파일 도구가 제공한 `.tsx` lint 결과도
  `skipped`이며 성공으로 해석하지 않는다.
- `package-lock.json`을 만들어내지 않았다. runner가 Node 20/npm 10 환경에서 실제 의존성을
  해결하고 lockfile을 물질화해야 한다. infra 소유 Dockerfile의 `npm ci` 선행 조건이다.
- `frontend/Dockerfile`, `.dockerignore`, `nginx.conf` 및 frontend 외 파일은 이 작업 소유가 아니다.

## 공통 shell / 상태

- [x] 컴팩트 네이비 sidebar, 헤더, breadcrumb, 본문 제목·설명, task별 고유 편집 영역.
- [x] 실제 `/api/accessible-menus` 결과만 navigation/direct route gate에 사용한다.
  역할 문자열, 관리자 우회, 하드코딩 접근 메뉴, `/api/menu-structure` 탐색 우회는 없다.
- [x] 접근 응답 누락/실패/형식 오류는 fail-closed. 허용 목록에 없는 route는 본문을 만들지 않으며
  해당 primary API를 호출하지 않는다. 권한 가드는 서버 가드를 대체하지 않는다.
- [x] 별도 dashboard·로그인·토큰 입력·통계·사용자/기관 seed·검색결과 placeholder를 넣지 않는다.
  루트는 업무 선택 안내만 표시한다. 헤더의 개인 이름은 발명하지 않는다.
- [x] 모든 목록 요청은 `page`와 `size`를 명시하며 서버의 pagination.total을 표시한다.
  목록형 화면의 이전/다음 pager와 크기 표시, 단일 대상형의 전체 건수를 구분한다.
- [x] 사용자·조직·역할·메뉴·그룹·상위코드 후보는 API에서 읽는다. 후보 조회는 실제 total을
  확인하며 전체 페이지를 읽는다. 상세코드 후보는 선택 그룹으로 제한하고 서버 순서를 보존한다.
- [x] loading / empty / error / permission / success를 구분한다. primary 401/403은 이전 목록과
  편집을 숨긴다. 보조 후보 403은 해당 의존성 오류로 표시하고 저장을 막는다.
- [x] 조회/route 전환에는 AbortController와 generation 가드가 있다. 늦은 응답은 무시한다.
  조직 이력·후보도 언마운트/맥락 변경 시 취소한다.
- [x] 저장 실패는 draft·식별자를 유지한다. 취소는 선택행 baseline으로 draft를 복원하며,
  serializer는 그 draft와 저장 식별자만 사용한다. readonly 필드의 DOM 변조도 payload에 안 넣는다.
- [x] 저장 후 실제 재조회 성공 이후에만 success를 표시한다. 저장 성공 후 읽기 실패는
  쓰기를 재전송하지 않고 읽기 재시도를 제공한다. 쓰기 실패 재시도는 같은 draft/UUID를 사용한다.
- [x] 저장 중 중복 버튼 입력을 차단한다. UI 계약에 없는 일반 저장 confirm은 추가하지 않는다.
- [x] label, heading, table caption, live status/alert, keyboard-focus, horizontal overflow,
  모바일 single-column, reduced-motion을 적용한다. 스크린샷 기반 시각 검증은 미실행이다.

## SCR-USERS / `/admin/users` / UserPage.tsx

- [x] 상단 조건 검색 / 넓은 인사 목록 / 선택 사용자 readonly 요약 / 로컬 사용여부 편집.
- [x] 교번·성명·소속·직급·재직상태·업무 역할·사용여부 조건을 각각 실제 query로 전달한다.
- [x] 보직·퇴직일자·최종 동기화일시를 포함한 원천 정보를 목록과 요약에 표시한다.
- [x] 사용여부만 `PUT /api/users`의 `{ user_id, enabled }`로 보낸다.
- [x] 업무 역할은 허용된 경우에만 별도 사용자 역할 화면으로 연결한다. 기간·승인자를 생략한
  role 배열을 `/api/users`에 보내지 않는다. target user_id는 route context와 함께 전달한다.
- [x] 개별 사용여부 저장이 KORUS 원천 필드나 역할 정의를 변경하지 않도록 payload를 분리한다.
- 작성 테스트: 전체 route/state matrix, 원천 payload 제외, 취소 후 저장, protected write denial.

## SCR-ORGANIZATIONS / `/admin/organizations` / OrganizationPage.tsx

- [x] 조직코드 선택 조회 / readonly 조직구분·현재 상하위 관계 / 상위관계·기간 묶음 편집 /
  별도 기간 이력. 공통 목록 pager나 임의 조직 트리를 복제하지 않는다.
- [x] `GET /api/organizations?page&size&organization_code`로 단일 조직 현재 관계를 읽는다.
- [x] 상위코드·시작일·종료일은 한 `PUT /api/organizations` payload에 묶인다.
- [x] `이력 조회`는 `GET /api/organization-relation-history?page&size&organization_code`를
  실제 호출한다. 현재 응답을 이력처럼 재사용하지 않는다. 이력 자체 total/pager가 있다.
- [x] 사용자 소속·보직·역할 입력이나 관련 endpoint 쓰기를 추가하지 않는다.
- 작성 테스트: 단일 조회, 실제 이력 요청, 실제환경의 승인된 상위관계 변경/복구.
- 미정: OQ-ORG-001 조직 소유권·기간 경계·중복·순환·과거 정정. 클라이언트가 확정하지 않는다.

## SCR-ROLES / `/admin/roles` / RolePage.tsx

- [x] 역할 식별 조회 / 역할 정의 목록 / 독립 정의 편집.
- [x] 기존 role_id는 readonly, 목적·부여 기준·기본 데이터 범위만 편집한다.
- [x] `GET/PUT /api/roles`, 명시 paging, 선택행 identity와 취소 baseline 보존.
- [x] 사용자 부여내역 쓰기와 역할 정의 쓰기를 섞지 않는다.
- 작성 테스트: 역할별 렌더, protected API denial, 취소 payload, 실제 목적 변경/복구.

## SCR-USER-ROLES / `/admin/user-roles` / UserRoleAssignmentPage.tsx

- [x] 대상 사용자 context / 다중 부여행 / 발생 출처·서버 현재 적용 판정 / 명령별 편집.
- [x] user_id, role_id, valid_from, valid_to, 별도 approver_id를 실제 후보에서 지정한다.
- [x] 버튼은 `부여`·`변경`·`회수`이며 승인자 자동 지정·임의 confirm은 없다.
- [x] 부여는 클라이언트 UUID, 변경/회수는 선택 assignment_id를 사용한다.
  실패 재시도는 같은 assignment_id를 보존한다.
- [x] origin과 is_current는 readonly 응답 projection이며 쓰기 DTO에서 제외한다.
  origin은 수동/보직 기반을 구분하고 기간의 실효 판정은 서버 응답만 표시한다.
- [x] 사용자 역할 연결의 query/back/forward가 바뀌면 keyed context로 이전 선택·draft를 리셋한다.
- [x] `GET/PUT /api/user-role-assignments`; 역할 정의와 인사정보 쓰기를 추가하지 않는다.
- 작성 테스트: UUID retry, 세 action payload, 별도 승인자, 빈 승인자 차단, 실제 승인자 변경/복구.
- 미정: OQ-ROLE-001 승인자 신원/단계·보직 매핑·회수 효력, OQ-ORG-001 경계값.

## SCR-MENU-PERMISSIONS / `/admin/menu-permissions` / MenuPermissionPage.tsx

- [x] 메뉴 접근 후보 / 역할·조직·사용자 선택 / 현재 설정 / 허용 변경 / 재판정 설명.
- [x] 미설정은 빈 select와 `미설정`으로 표시한다. 저장값 false와 동일시하지 않는다.
- [x] 대상 조건은 optional query. 쓰기는 최소 한 대상과 메뉴·명시 boolean을 요구한다.
- [x] 기존 permission_id를 보존하고 최초 부여 UUID는 실패 재시도에도 유지한다.
- [x] 저장 후 `/api/accessible-menus`를 재조회하여 sidebar와 direct route gate를 갱신한다.
- [x] `GET/PUT /api/menu-permissions`; 역할·메뉴의 허용 조합 우선순위를 발명하지 않는다.
- 작성 테스트: 저장 뒤 access 재조회, 미설정/false 구분, UUID retry, 실제 허용 toggle/복구.
- 미정: OQ-AUTH-001/002 실효 조합·관리 범위·후보 읽기 권한. 실서버 쓰기 시험은 별도 승인 필요.

## SCR-MENU-STRUCTURE / `/admin/menu-structure` / MenuStructurePage.tsx

- [x] 부모 후보 / menu_id·부모·level·display_order를 가진 계층 목록 / 구조 편집.
- [x] 기존 menu_id readonly. 순서·계층은 정수 검증하며 실제 서버 순서를 표시한다.
- [x] `GET/PUT /api/menu-structure`만 구조를 수정한다. 실행정보·permission은 쓰지 않는다.
- [x] 저장 후 accessible-menus를 새로 읽어 동일 부모의 실제 표시순서를 반영한다.
- 작성 테스트: 정수 순서·선택 identity payload, 실제 순서 변경/복구.
- 미정: 메뉴 명칭 필드와 순환·레벨 상한. 없는 name을 seed로 채우지 않는다.

## SCR-MENU-INFO / `/admin/menu-info` / MenuExecutionInfoPage.tsx

- [x] 메뉴 선택 조회 / 단일 실행정보 폼 / 서버 허용 메뉴 실행 영역. 공통 목록 pager를 쓰지 않는다.
- [x] `GET/PUT /api/menu-execution-info`; menu_id readonly, screen_id·url만 편집한다.
- [x] 저장 후 accessible-menus refresh. 실행은 저장된 서버 접근결과의 screen_id/url만 사용한다.
- [x] safeInternalUrl은 same-origin 절대 내부 경로만 허용하고 protocol-relative, backslash,
  제어문자와 중첩 인코딩 우회는 거부한다. 위험한 저장 URL을 실행 링크로 직접 사용하지 않는다.
- [x] 외부/위험 URL은 비실행 안내로 남긴다. 이를 서버 URL 정책의 승인으로 주장하지 않는다.
- 작성 테스트: 정확한 screen_id/url payload, 위험 URL 비실행, 실제 URL 변경·안전 링크 이동/복구.
- 미정: OQ-UI-003 실행정보 추가 필드·사용 가능 판정, URL 저장/실행 안전정책 승인.

## SCR-CODE-GROUPS / `/admin/code-groups` / CodeGroupPage.tsx

- [x] 그룹 식별 조회 / 그룹 목록 / 독립 그룹 정의 편집 / 허용된 상세코드 화면 연결.
- [x] 기존 group_id readonly, 명칭·설명·관리부서 편집. 관리부서 후보도 실제 조직 API 데이터다.
- [x] `GET/PUT /api/code-groups`; 상세코드 값을 같이 보내거나 자동 수정하지 않는다.
- 작성 테스트: group identity 보존, 실제 설명 변경·재조회·복구.

## SCR-DETAIL-CODES / `/admin/detail-codes` / DetailCodePage.tsx

- [x] 그룹 선택 없이는 상세코드를 요청하지 않는다. 선택 그룹 context를 모든 조회에 전달한다.
- [x] 그룹 전환 시 이전 선택·draft·success를 비우고 이전 요청을 취소한다.
- [x] 확인된 네 필드 group_id/code/sort_order/parent_code만 쓰며 기존 복합 키는 readonly.
- [x] 같은 그룹 전체 페이지의 실제 코드 후보를 상위코드 select에 쓰고 서버 순서를 보존한다.
  self는 후보에서 제외하되 서버의 미정 순환 정책까지 대신 결정하지 않는다.
- [x] 계층은 상위코드 연결과 들여쓰기로 식별한다. 저장 후 부모 후보·선택값 순서를 다시 읽는다.
- [x] `GET/PUT /api/detail-codes`; 그룹 정의나 임의 추가 코드 필드는 수정하지 않는다.
- 작성 테스트: 필수 group query, 복합 키 잠금, sort payload, 실제 순서 변경/복구.
- 미정: OQ-UI-002 추가 필드·필수성, 계층 순환/최대 깊이 정책.

## 남은 정책·통합 선행조건

1. OQ-AUTH-001/002: 인증 및 server-side 판정/차단, 토큰 발급 방식과 후보 API 읽기 scope.
   프론트엔드는 기관 인증된 same-origin 요청만 하며 인증 UI나 토큰 발급을 만들지 않는다.
2. OQ-ORG-001/OQ-ROLE-001/OQ-DATA-001/002: 소유권·기간·승인·동기화·삭제/보관/접속 정책.
3. OQ-UI-001/003/004: 제안 route/screen ID·연결·메뉴 라벨·시각 토큰/반응형 최종 승인.
4. OQ-TECH-001: 정확한 patch BOM의 실행 호환성 검증과 규모/입력 크기. 여기의 exact 의존성은
   Node20/React18/Vite5용 구현 선택이며 실행 검증 완료 버전이라는 주장은 아니다.
5. runner: 실제 lockfile 생성 → 설치 → typecheck/unit/build → 외부 서버/실DB/승인 fixture → E2E.
   E2E는 자체 webServer나 route interception을 사용하지 않는다. 미승인 정책 scenario는 명시 skip이며
   제품 동작의 합격으로 집계하면 안 된다.
