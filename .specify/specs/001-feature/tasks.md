---
description: "oct19 공통 관리 vertical slice 구현 작업"
---

# Tasks: oct19 공통 관리

**Input**: `.specify/specs/001-feature/` 설계 문서.
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/openapi.yaml, ui-design.md.
**Tests**: 필수 TDD. 테스트를 먼저 쓰고 요구 부재로 실패함을 본 뒤 구현한다. 아래 작업은 계획이며 실제 구현·시험이 끝났다는 표시가 아니다.
**Organization**: spec.md US 번호·제목을 그대로 사용한다. 모든 story task는 [USn], 공통 작업은 story tag 없음.

## Format: `[ID] [P?] [Story] Description`

[P]는 같은 단계에서 다른 파일을 쓰고 서로 선행 의존이 없는 경우만 쓴다. 구현 작업은 앞선 RED 시험에 의존하므로 [P]를 붙이지 않는다.

## Path Conventions

Java는 backend/src/main/java/org/example/oct19/, 테스트는 backend/src/test/java/org/example/oct19/, mapper·migration은 backend/src/main/resources/, React는 frontend/src/, 브라우저 시험은 frontend/tests/e2e/, Compose는 infra/ 아래에 둔다. 이 경로들은 후속 구현 계획이며 현재 생성된 소스로 오인하지 않는다.

## 공통 계약 참조

| 구분 | 확정 계약 |
|---|---|
| Frontend | React 18.x + TypeScript + Vite 5.x; Node 20.x |
| Backend | Java 17 + Spring Boot 3.3.x |
| Database | PostgreSQL 16.x |
| Infrastructure | Docker Compose |
| execution/persistence | imperative / mybatis / blocking; decision_status=resolved; conflicts=[]; 정본은 plan.md marker JSON |
| required_outputs.backend_dir | backend |
| required_outputs.frontend_dir | frontend |
| required_outputs.compose_file | infra/docker-compose.yml |
| required_outputs.health_endpoint | /api/health |

근거: REQ-032, REQ-033, REQ-034, REQ-035, REQ-036, REQ-037, REQ-038, REQ-039, REQ-040, REQ-041. `신규생성 실행 계약`과 `기술스택 고정`은 동일한 권위의 계약이다. 명확화의 `추정 — 사용자 미확인`은 이 명시 계약을 대체하지 않으며 사업 정책의 승인이 아니다. 패치 버전은 OQ-TECH-001이다.
## Phase 1: Setup (Shared Infrastructure)

**Purpose**: 고정 스택과 필수 산출물의 빌드 골격을 준비한다.

- [ ] T001 infra/tests/stack-contract.test.sh에 BOM·필수 디렉터리·Compose·health 경로 검사와 누락 시 실패 검사를 먼저 작성한다. (REQ-032, REQ-033, REQ-034, REQ-035, REQ-037, REQ-038, REQ-039, REQ-040, REQ-041)
- [ ] T002 backend/pom.xml, backend/mvnw, backend/.mvn/에 Java 17/Spring Boot 3.3.x/MyBatis 및 JUnit 빌드를 초기화하고 위 골격 검사를 통과시킨다. (REQ-033, REQ-036, REQ-038)
- [ ] T003 frontend/package.json, frontend/package-lock.json, frontend/vite.config.ts에 React 18.x/TypeScript/Vite 5.x, Node 20.x, test/build/test:e2e 스크립트를 고정한다. (REQ-032, REQ-037, REQ-039)
- [ ] T004 infra/docker-compose.yml, backend/Dockerfile, frontend/Dockerfile에 PostgreSQL 16.x·backend·frontend 서비스를 구성한다. backend:8080/frontend:5173 로컬 포트는 설계 제안으로 검토한다. (REQ-034, REQ-035, REQ-040)
- [ ] T005 backend/.dockerignore와 frontend/.dockerignore에 node_modules, .git, dist, build, .next, coverage를 제외하고 infra/tests/stack-contract.test.sh의 context별 검사로 검증한다. (REQ-050)

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: 공유 DB·envelope·보안 경계·UI shell을 갖춘다. OQ-AUTH-001과 승인자·기간 정책 미해결 시 해당 업무 완료 판정을 차단한다.

- [ ] T006 backend/src/test/java/org/example/oct19/PersistenceContractTest.java에 Entity Registry 전체 PK/FK·PostgreSQL 연결·rollback 실패 시험을 먼저 작성한다. (REQ-034, REQ-036, REQ-048)
- [ ] T007 backend/src/main/resources/db/migration/V001__foundation.sql, backend/src/main/resources/application.yml에 Entity Registry 스키마·키·명시 인덱스와 MyBatis JDBC 연결을 구현하여 persistence 시험을 green으로 만든다. migration 도구 버전은 승인 후 고정한다. (REQ-034, REQ-036, REQ-048)
- [ ] T008 backend/src/test/java/org/example/oct19/ApiFoundationTest.java에 ApiResponse/ApiError의 success/meta와 getHealth 200, 무인증 401·실효 불허 403·protected write 없음 시험을 먼저 작성한다. (REQ-018, REQ-041)
- [ ] T009 backend/src/main/java/org/example/oct19/api/, backend/src/main/java/org/example/oct19/security/에 envelope·getHealth·인증 adapter·실효 메뉴 guard를 구현한다. OQ-AUTH-001 승인 없이 하드코딩된 중앙관리자나 권한 우선순위를 넣지 않는다. (REQ-018, REQ-041)
- [ ] T010 frontend/tests/unit/shell.test.tsx에 sidebar/header·loading/empty/error/permission/success·실효 거부 메뉴 숨김 RED 시험을 먼저 작성한다. (REQ-017, REQ-042, REQ-046)
- [ ] T011 frontend/src/components/, frontend/src/api/client.ts, frontend/src/styles/tokens.css에 공통 상태·sidebar/header·envelope 처리를 구현한다. 구체 디자인 토큰은 OQ-UI-004 승인 후 고정한다. (REQ-017, REQ-042, REQ-046)
- [ ] T012 backend/src/test/java/org/example/oct19/AccessibleMenusTest.java에 메뉴 계층·순서·실행정보·실효 권한 조합의 RED 시험을 작성한다. (REQ-017, REQ-020, REQ-023)
- [ ] T013 backend/src/main/java/org/example/oct19/service/AccessibleMenusService.java, backend/src/main/resources/mapper/AccessibleMenusMapper.xml에 getAccessibleMenus를 구현하고 승인된 사용자 메뉴 읽기 scope와 관리 편집 scope를 분리한다. (REQ-017, REQ-020, REQ-023)

**Checkpoint**: 공유 기반과 공개 health 검사 통과. 미정 인증·권한·period 정책은 승인 대기 표시를 유지하며 green으로 위장하지 않는다.

## Phase 3: User Story 1 - 사용자 관리 (Priority: P1)

**Goal**: SCR-USERS `/admin/users`에서 사용자 조건 검색 / 사용자 인사정보 목록 표시 / 사용여부·업무 역할 변경 유지. 데이터 `users`, API `searchUsers`/`updateUserSettings`, fixture `FX-US1`를 연결한다.

**Independent Test**: 교번·성명·소속·직급·재직상태·역할·사용여부 중 하나 이상의 조건으로 검색하면 조건에 맞는 사용자만 표시된다. 사용자 목록에 보직·퇴직일자·최종 동기화일시를 포함한 명시 항목이 표시된다. 시스템 사용여부 또는 업무 역할을 변경하고 다시 조회하면 변경값이 유지된다. 다른 US 화면 없이 해당 fixture와 API로 검증한다.

### Tests for User Story 1

- [ ] T014 [US1] backend/src/test/java/org/example/oct19/UserContractTest.java에 searchUsers/updateUserSettings의 원문 AC·입력 검증·권한·비간섭·기간 또는 이력 시험을 먼저 작성하고 미구현 실패를 기록한다. x-required-tests의 미정 정책은 승인 후 assertion을 구체화한다. (REQ-001, REQ-002, REQ-003, REQ-004)
- [ ] T015 [US1] frontend/tests/e2e/us1-users.spec.ts에 SCR-USERS /admin/users의 원문 primary AC와 loading/empty/error/permission/success를 검증하는 RED 브라우저 시험을 먼저 작성한다. (REQ-001, REQ-002, REQ-003)

### Implementation for User Story 1

- [ ] T016 [US1] backend/src/main/resources/db/migration/V002__users_constraints.sql, backend/src/main/resources/db/test-fixtures/us1.sql에 users의 검증된 constraint·fixture FX-US1를 구현한다. source 미확인 필드·보관·삭제 정책을 추가하지 않는다. (REQ-001, REQ-002, REQ-003, REQ-004)
- [ ] T017 [US1] backend/src/main/java/org/example/oct19/service/UserService.java, backend/src/main/java/org/example/oct19/mapper/UserMapper.java, backend/src/main/resources/mapper/UserMapper.xml에 blocking MyBatis·transaction과 해당 원문 규칙을 구현한다. (REQ-001, REQ-002, REQ-003, REQ-004)
- [ ] T018 [US1] backend/src/main/java/org/example/oct19/api/UserController.java에 searchUsers/updateUserSettings를 연결하고 DTO·envelope·서버 권한 및 비간섭 검증으로 앞선 contract 시험을 green으로 만든다. (REQ-001, REQ-002, REQ-003, REQ-004)
- [ ] T019 [US1] frontend/src/pages/UserPage.tsx, frontend/src/api/users.ts에 SCR-USERS /admin/users의 고유 wireframe·데이터 binding·API와 local-only 동작을 구현한다. 선택 행의 숨김 user_id를 updateUserSettings 본문에 보존하고 업무 역할 projection을 직접 쓰지 않고 사용자 역할 화면으로 연결한다. OQ UI는 임의로 확정하지 않고 deferred 표시한다. (REQ-001, REQ-002, REQ-003)
- [ ] T020 [US1] frontend/tests/e2e/us1-users.spec.ts, backend/src/test/java/org/example/oct19/UserContractTest.java에 저장 후 재조회·해당 비간섭 snapshot 회귀 사례를 완성하고 실제 green 결과를 .specify/specs/001-feature/quickstart.md의 검증 기록에 남긴다. (REQ-001, REQ-002, REQ-003, REQ-004)

**Checkpoint**: US1 사용자 관리의 원문 AC 및 해당 slice API/UI/DB 시험이 독립 통과하고 OQ 차단 항목이 분리되어 있어야 한다.

## Phase 4: User Story 2 - 조직 관리 (Priority: P1)

**Goal**: SCR-ORGANIZATIONS `/admin/organizations`에서 조직 현재 관계 조회 / 상위조직 적용기간 저장 / 변경 전 조직관계 이력 조회. 데이터 `organizations`, API `getOrganizationRelations`/`saveOrganizationRelation`, fixture `FX-US2`를 연결한다.

**Independent Test**: 조직코드로 조회하면 해당 조직의 조직구분과 현재 상위·하위 관계가 표시된다. 상위조직 변경 시 적용 시작일·종료일과 변경된 상위조직이 함께 저장된다. 변경 이후에도 변경 전 조직관계와 적용기간을 이력으로 조회할 수 있다. 다른 US 화면 없이 해당 fixture와 API로 검증한다.

### Tests for User Story 2

- [ ] T021 [US2] backend/src/test/java/org/example/oct19/OrganizationContractTest.java에 getOrganizationRelations/saveOrganizationRelation의 원문 AC·입력 검증·권한·비간섭·기간 또는 이력 시험을 먼저 작성하고 미구현 실패를 기록한다. x-required-tests의 미정 정책은 승인 후 assertion을 구체화한다. (REQ-005, REQ-006, REQ-007, REQ-008)
- [ ] T022 [US2] frontend/tests/e2e/us2-organizations.spec.ts에 SCR-ORGANIZATIONS /admin/organizations의 원문 primary AC와 loading/empty/error/permission/success를 검증하는 RED 브라우저 시험을 먼저 작성한다. (REQ-005, REQ-006, REQ-007)

### Implementation for User Story 2

- [ ] T023 [US2] backend/src/main/resources/db/migration/V003__organizations_constraints.sql, backend/src/main/resources/db/test-fixtures/us2.sql에 organizations의 검증된 constraint·fixture FX-US2를 구현한다. source 미확인 필드·보관·삭제 정책을 추가하지 않는다. (REQ-005, REQ-006, REQ-007, REQ-008)
- [ ] T024 [US2] backend/src/main/java/org/example/oct19/service/OrganizationService.java, backend/src/main/java/org/example/oct19/mapper/OrganizationMapper.java, backend/src/main/resources/mapper/OrganizationMapper.xml에 blocking MyBatis·transaction과 해당 원문 규칙을 구현한다. (REQ-005, REQ-006, REQ-007, REQ-008)
- [ ] T025 [US2] backend/src/main/java/org/example/oct19/api/OrganizationController.java에 getOrganizationRelations/saveOrganizationRelation 및 listOrganizationRelationHistory를 연결하고 DTO·envelope·서버 권한 및 비간섭 검증으로 앞선 contract 시험을 green으로 만든다. (REQ-005, REQ-006, REQ-007, REQ-008)
- [ ] T026 [US2] frontend/src/pages/OrganizationPage.tsx, frontend/src/api/organizations.ts에 SCR-ORGANIZATIONS /admin/organizations의 고유 wireframe·데이터 binding·API와 local-only 동작을 구현한다. OQ UI는 임의로 확정하지 않고 deferred 표시한다. (REQ-005, REQ-006, REQ-007)
- [ ] T027 [US2] frontend/tests/e2e/us2-organizations.spec.ts, backend/src/test/java/org/example/oct19/OrganizationContractTest.java에 저장 후 재조회·해당 비간섭 snapshot 회귀 사례를 완성하고 실제 green 결과를 .specify/specs/001-feature/quickstart.md의 검증 기록에 남긴다. (REQ-005, REQ-006, REQ-007, REQ-008)

**Checkpoint**: US2 조직 관리의 원문 AC 및 해당 slice API/UI/DB 시험이 독립 통과하고 OQ 차단 항목이 분리되어 있어야 한다.

## Phase 5: User Story 3 - 역할 관리 (Priority: P1)

**Goal**: SCR-ROLES `/admin/roles`에서 역할 정의 저장·재조회. 데이터 `roles`, API `listRoles`/`saveRole`, fixture `FX-US3`를 연결한다.

**Independent Test**: 역할별 목적·부여 기준·데이터 범위 기본값을 저장하고 재조회할 수 있다. 다른 US 화면 없이 해당 fixture와 API로 검증한다.

### Tests for User Story 3

- [ ] T028 [US3] backend/src/test/java/org/example/oct19/RoleContractTest.java에 listRoles/saveRole의 원문 AC·입력 검증·권한·비간섭·기간 또는 이력 시험을 먼저 작성하고 미구현 실패를 기록한다. x-required-tests의 미정 정책은 승인 후 assertion을 구체화한다. (REQ-009, REQ-010, REQ-011)
- [ ] T029 [US3] frontend/tests/e2e/us3-roles.spec.ts에 SCR-ROLES /admin/roles의 원문 primary AC와 loading/empty/error/permission/success를 검증하는 RED 브라우저 시험을 먼저 작성한다. (REQ-009)

### Implementation for User Story 3

- [ ] T030 [US3] backend/src/main/resources/db/migration/V004__roles_constraints.sql, backend/src/main/resources/db/test-fixtures/us3.sql에 roles의 검증된 constraint·fixture FX-US3를 구현한다. source 미확인 필드·보관·삭제 정책을 추가하지 않는다. (REQ-009, REQ-010, REQ-011)
- [ ] T031 [US3] backend/src/main/java/org/example/oct19/service/RoleService.java, backend/src/main/java/org/example/oct19/mapper/RoleMapper.java, backend/src/main/resources/mapper/RoleMapper.xml에 blocking MyBatis·transaction과 해당 원문 규칙을 구현한다. (REQ-009, REQ-010, REQ-011)
- [ ] T032 [US3] backend/src/main/java/org/example/oct19/api/RoleController.java에 listRoles/saveRole를 연결하고 DTO·envelope·서버 권한 및 비간섭 검증으로 앞선 contract 시험을 green으로 만든다. (REQ-009, REQ-010, REQ-011)
- [ ] T033 [US3] frontend/src/pages/RolePage.tsx, frontend/src/api/roles.ts에 SCR-ROLES /admin/roles의 고유 wireframe·데이터 binding·API와 local-only 동작을 구현한다. OQ UI는 임의로 확정하지 않고 deferred 표시한다. (REQ-009)
- [ ] T034 [US3] frontend/tests/e2e/us3-roles.spec.ts, backend/src/test/java/org/example/oct19/RoleContractTest.java에 저장 후 재조회·해당 비간섭 snapshot 회귀 사례를 완성하고 실제 green 결과를 .specify/specs/001-feature/quickstart.md의 검증 기록에 남긴다. (REQ-009, REQ-010, REQ-011)

**Checkpoint**: US3 역할 관리의 원문 AC 및 해당 slice API/UI/DB 시험이 독립 통과하고 OQ 차단 항목이 분리되어 있어야 한다.

## Phase 6: User Story 4 - 사용자 역할 관리 (Priority: P1)

**Goal**: SCR-USER-ROLES `/admin/user-roles`에서 다중 역할·상이한 기간 부여 / 부여·변경·회수 승인자 기록 / 역할 발생 출처 구분 조회. 데이터 `user_role_assignments`, API `listUserRoleAssignments`/`saveUserRoleAssignment`, fixture `FX-US4`를 연결한다.

**Independent Test**: 한 사용자에게 둘 이상의 역할을 서로 다른 유효기간으로 부여할 수 있다. 역할을 부여·변경·회수하면 대상 사용자·역할·유효기간·승인자가 함께 저장된다. 보직 기반 역할과 수동 역할을 조회 결과에서 구분할 수 있다. 다른 US 화면 없이 해당 fixture와 API로 검증한다.

### Tests for User Story 4

- [ ] T035 [US4] backend/src/test/java/org/example/oct19/UserRoleAssignmentContractTest.java에 listUserRoleAssignments/saveUserRoleAssignment의 원문 AC·입력 검증·권한·비간섭·기간 또는 이력 시험을 먼저 작성하고 미구현 실패를 기록한다. x-required-tests의 미정 정책은 승인 후 assertion을 구체화한다. (REQ-012, REQ-013, REQ-014, REQ-015)
- [ ] T036 [US4] frontend/tests/e2e/us4-user-role-assignments.spec.ts에 SCR-USER-ROLES /admin/user-roles의 원문 primary AC와 loading/empty/error/permission/success를 검증하는 RED 브라우저 시험을 먼저 작성한다. (REQ-012, REQ-013, REQ-014)

### Implementation for User Story 4

- [ ] T037 [US4] backend/src/main/resources/db/migration/V005__user_role_assignments_constraints.sql, backend/src/main/resources/db/test-fixtures/us4.sql에 user_role_assignments의 검증된 constraint·fixture FX-US4를 구현한다. source 미확인 필드·보관·삭제 정책을 추가하지 않는다. (REQ-012, REQ-013, REQ-014, REQ-015)
- [ ] T038 [US4] backend/src/main/java/org/example/oct19/service/UserRoleAssignmentService.java, backend/src/main/java/org/example/oct19/mapper/UserRoleAssignmentMapper.java, backend/src/main/resources/mapper/UserRoleAssignmentMapper.xml에 blocking MyBatis·transaction과 해당 원문 규칙을 구현한다. (REQ-012, REQ-013, REQ-014, REQ-015)
- [ ] T039 [US4] backend/src/main/java/org/example/oct19/api/UserRoleAssignmentController.java에 listUserRoleAssignments/saveUserRoleAssignment를 연결하고 DTO·envelope·서버 권한 및 비간섭 검증으로 앞선 contract 시험을 green으로 만든다. (REQ-012, REQ-013, REQ-014, REQ-015)
- [ ] T040 [US4] frontend/src/pages/UserRoleAssignmentPage.tsx, frontend/src/api/user_role_assignments.ts에 SCR-USER-ROLES /admin/user-roles의 고유 wireframe·데이터 binding·API와 local-only 동작을 구현한다. 부여는 내부 assignment_id UUID 생성·재시도 보존, 변경·회수는 선택 행 ID 재사용, 각 버튼은 requestBody.action=부여/변경/회수로 연결하고 origin은 readonly로 유지한다. OQ UI는 임의로 확정하지 않고 deferred 표시한다. (REQ-012, REQ-013, REQ-014)
- [ ] T041 [US4] frontend/tests/e2e/us4-user-role-assignments.spec.ts, backend/src/test/java/org/example/oct19/UserRoleAssignmentContractTest.java에 저장 후 재조회·해당 비간섭 snapshot 회귀 사례를 완성하고 실제 green 결과를 .specify/specs/001-feature/quickstart.md의 검증 기록에 남긴다. (REQ-012, REQ-013, REQ-014, REQ-015)

**Checkpoint**: US4 사용자 역할 관리의 원문 AC 및 해당 slice API/UI/DB 시험이 독립 통과하고 OQ 차단 항목이 분리되어 있어야 한다.

## Phase 7: User Story 5 - 메뉴 권한 관리 (Priority: P1)

**Goal**: SCR-MENU-PERMISSIONS `/admin/menu-permissions`에서 메뉴 접근 허용 설정 저장. 데이터 `menu_permissions`, API `listMenuPermissions`/`saveMenuPermission`, fixture `FX-US5`를 연결한다.

**Independent Test**: 역할·조직·사용자와 메뉴를 선택해 접근 허용 여부를 저장하고 재조회할 수 있다. 다른 US 화면 없이 해당 fixture와 API로 검증한다.

### Tests for User Story 5

- [ ] T042 [US5] backend/src/test/java/org/example/oct19/MenuPermissionContractTest.java에 listMenuPermissions/saveMenuPermission의 원문 AC·입력 검증·권한·비간섭·기간 또는 이력 시험을 먼저 작성하고 미구현 실패를 기록한다. x-required-tests의 미정 정책은 승인 후 assertion을 구체화한다. (REQ-016, REQ-017, REQ-018)
- [ ] T043 [US5] frontend/tests/e2e/us5-menu-permissions.spec.ts에 SCR-MENU-PERMISSIONS /admin/menu-permissions의 원문 primary AC와 loading/empty/error/permission/success를 검증하는 RED 브라우저 시험을 먼저 작성한다. (REQ-016)

### Implementation for User Story 5

- [ ] T044 [US5] backend/src/main/resources/db/migration/V006__menu_permissions_constraints.sql, backend/src/main/resources/db/test-fixtures/us5.sql에 menu_permissions의 검증된 constraint·fixture FX-US5를 구현한다. source 미확인 필드·보관·삭제 정책을 추가하지 않는다. (REQ-016, REQ-017, REQ-018)
- [ ] T045 [US5] backend/src/main/java/org/example/oct19/service/MenuPermissionService.java, backend/src/main/java/org/example/oct19/mapper/MenuPermissionMapper.java, backend/src/main/resources/mapper/MenuPermissionMapper.xml에 blocking MyBatis·transaction과 해당 원문 규칙을 구현한다. (REQ-016, REQ-017, REQ-018)
- [ ] T046 [US5] backend/src/main/java/org/example/oct19/api/MenuPermissionController.java에 listMenuPermissions/saveMenuPermission를 연결하고 DTO·envelope·서버 권한 및 비간섭 검증으로 앞선 contract 시험을 green으로 만든다. (REQ-016, REQ-017, REQ-018)
- [ ] T047 [US5] frontend/src/pages/MenuPermissionPage.tsx, frontend/src/api/menu_permissions.ts에 SCR-MENU-PERMISSIONS /admin/menu-permissions의 고유 wireframe·데이터 binding·API와 local-only 동작을 구현한다. OQ UI는 임의로 확정하지 않고 deferred 표시한다. (REQ-016)
- [ ] T048 [US5] frontend/tests/e2e/us5-menu-permissions.spec.ts, backend/src/test/java/org/example/oct19/MenuPermissionContractTest.java에 저장 후 재조회·해당 비간섭 snapshot 회귀 사례를 완성하고 실제 green 결과를 .specify/specs/001-feature/quickstart.md의 검증 기록에 남긴다. (REQ-016, REQ-017, REQ-018)

**Checkpoint**: US5 메뉴 권한 관리의 원문 AC 및 해당 slice API/UI/DB 시험이 독립 통과하고 OQ 차단 항목이 분리되어 있어야 한다.

## Phase 8: User Story 6 - 메뉴 구조 관리 (Priority: P2)

**Goal**: SCR-MENU-STRUCTURE `/admin/menu-structure`에서 부모메뉴·계층 저장 / 동일 계층 표시순서 적용. 데이터 `menus`, API `getMenuStructure`/`saveMenuStructure`, fixture `FX-US6`를 연결한다.

**Independent Test**: 메뉴별 부모메뉴와 계층단계를 저장하고 계층구조로 재조회할 수 있다. 동일 계층의 표시순서를 변경하면 사용자 메뉴에 변경된 순서가 적용된다. 다른 US 화면 없이 해당 fixture와 API로 검증한다.

### Tests for User Story 6

- [ ] T049 [US6] backend/src/test/java/org/example/oct19/MenuStructureContractTest.java에 getMenuStructure/saveMenuStructure의 원문 AC·입력 검증·권한·비간섭·기간 또는 이력 시험을 먼저 작성하고 미구현 실패를 기록한다. x-required-tests의 미정 정책은 승인 후 assertion을 구체화한다. (REQ-019, REQ-020, REQ-021)
- [ ] T050 [US6] frontend/tests/e2e/us6-menus.spec.ts에 SCR-MENU-STRUCTURE /admin/menu-structure의 원문 primary AC와 loading/empty/error/permission/success를 검증하는 RED 브라우저 시험을 먼저 작성한다. (REQ-019, REQ-020)

### Implementation for User Story 6

- [ ] T051 [US6] backend/src/main/resources/db/migration/V007__menus_constraints.sql, backend/src/main/resources/db/test-fixtures/us6.sql에 menus의 검증된 constraint·fixture FX-US6를 구현한다. source 미확인 필드·보관·삭제 정책을 추가하지 않는다. (REQ-019, REQ-020, REQ-021)
- [ ] T052 [US6] backend/src/main/java/org/example/oct19/service/MenuStructureService.java, backend/src/main/java/org/example/oct19/mapper/MenuStructureMapper.java, backend/src/main/resources/mapper/MenuStructureMapper.xml에 blocking MyBatis·transaction과 해당 원문 규칙을 구현한다. (REQ-019, REQ-020, REQ-021)
- [ ] T053 [US6] backend/src/main/java/org/example/oct19/api/MenuStructureController.java에 getMenuStructure/saveMenuStructure를 연결하고 DTO·envelope·서버 권한 및 비간섭 검증으로 앞선 contract 시험을 green으로 만든다. (REQ-019, REQ-020, REQ-021)
- [ ] T054 [US6] frontend/src/pages/MenuStructurePage.tsx, frontend/src/api/menus.ts에 SCR-MENU-STRUCTURE /admin/menu-structure의 고유 wireframe·데이터 binding·API와 local-only 동작을 구현한다. OQ UI는 임의로 확정하지 않고 deferred 표시한다. (REQ-019, REQ-020)
- [ ] T055 [US6] frontend/tests/e2e/us6-menus.spec.ts, backend/src/test/java/org/example/oct19/MenuStructureContractTest.java에 저장 후 재조회·해당 비간섭 snapshot 회귀 사례를 완성하고 실제 green 결과를 .specify/specs/001-feature/quickstart.md의 검증 기록에 남긴다. (REQ-019, REQ-020, REQ-021)

**Checkpoint**: US6 메뉴 구조 관리의 원문 AC 및 해당 slice API/UI/DB 시험이 독립 통과하고 OQ 차단 항목이 분리되어 있어야 한다.

## Phase 9: User Story 7 - 메뉴 정보 관리 (Priority: P2)

**Goal**: SCR-MENU-INFO `/admin/menu-info`에서 메뉴 실행정보 저장 / 사용 가능한 메뉴 화면 실행. 데이터 `menu_execution_info`, API `listMenuExecutionInfo`/`saveMenuExecutionInfo`, fixture `FX-US7`를 연결한다.

**Independent Test**: 메뉴의 실행정보를 저장하고 동일한 값으로 재조회할 수 있다. 사용 가능한 메뉴를 선택하면 연결된 화면ID와 URL의 화면이 실행된다. 다른 US 화면 없이 해당 fixture와 API로 검증한다.

### Tests for User Story 7

- [ ] T056 [US7] backend/src/test/java/org/example/oct19/MenuExecutionInfoContractTest.java에 listMenuExecutionInfo/saveMenuExecutionInfo의 원문 AC·입력 검증·권한·비간섭·기간 또는 이력 시험을 먼저 작성하고 미구현 실패를 기록한다. x-required-tests의 미정 정책은 승인 후 assertion을 구체화한다. (REQ-022, REQ-023, REQ-024)
- [ ] T057 [US7] frontend/tests/e2e/us7-menu-execution-info.spec.ts에 SCR-MENU-INFO /admin/menu-info의 원문 primary AC와 loading/empty/error/permission/success를 검증하는 RED 브라우저 시험을 먼저 작성한다. (REQ-022, REQ-023)

### Implementation for User Story 7

- [ ] T058 [US7] backend/src/main/resources/db/migration/V008__menu_execution_info_constraints.sql, backend/src/main/resources/db/test-fixtures/us7.sql에 menu_execution_info의 검증된 constraint·fixture FX-US7를 구현한다. source 미확인 필드·보관·삭제 정책을 추가하지 않는다. (REQ-022, REQ-023, REQ-024)
- [ ] T059 [US7] backend/src/main/java/org/example/oct19/service/MenuExecutionInfoService.java, backend/src/main/java/org/example/oct19/mapper/MenuExecutionInfoMapper.java, backend/src/main/resources/mapper/MenuExecutionInfoMapper.xml에 blocking MyBatis·transaction과 해당 원문 규칙을 구현한다. (REQ-022, REQ-023, REQ-024)
- [ ] T060 [US7] backend/src/main/java/org/example/oct19/api/MenuExecutionInfoController.java에 listMenuExecutionInfo/saveMenuExecutionInfo를 연결하고 DTO·envelope·서버 권한 및 비간섭 검증으로 앞선 contract 시험을 green으로 만든다. (REQ-022, REQ-023, REQ-024)
- [ ] T061 [US7] frontend/src/pages/MenuExecutionInfoPage.tsx, frontend/src/api/menu_execution_info.ts에 SCR-MENU-INFO /admin/menu-info의 고유 wireframe·데이터 binding·API와 local-only 동작을 구현한다. OQ UI는 임의로 확정하지 않고 deferred 표시한다. (REQ-022, REQ-023)
- [ ] T062 [US7] frontend/tests/e2e/us7-menu-execution-info.spec.ts, backend/src/test/java/org/example/oct19/MenuExecutionInfoContractTest.java에 저장 후 재조회·해당 비간섭 snapshot 회귀 사례를 완성하고 실제 green 결과를 .specify/specs/001-feature/quickstart.md의 검증 기록에 남긴다. (REQ-022, REQ-023, REQ-024)

**Checkpoint**: US7 메뉴 정보 관리의 원문 AC 및 해당 slice API/UI/DB 시험이 독립 통과하고 OQ 차단 항목이 분리되어 있어야 한다.

## Phase 10: User Story 8 - 코드그룹 관리 (Priority: P2)

**Goal**: SCR-CODE-GROUPS `/admin/code-groups`에서 코드그룹 정의 저장 / 그룹ID별 독립 조회. 데이터 `code_groups`, API `listCodeGroups`/`saveCodeGroup`, fixture `FX-US8`를 연결한다.

**Independent Test**: 그룹ID·명칭·설명·관리부서를 저장하고 재조회할 수 있다. 서로 다른 그룹ID의 코드그룹이 독립적으로 조회된다. 다른 US 화면 없이 해당 fixture와 API로 검증한다.

### Tests for User Story 8

- [ ] T063 [US8] backend/src/test/java/org/example/oct19/CodeGroupContractTest.java에 listCodeGroups/saveCodeGroup의 원문 AC·입력 검증·권한·비간섭·기간 또는 이력 시험을 먼저 작성하고 미구현 실패를 기록한다. x-required-tests의 미정 정책은 승인 후 assertion을 구체화한다. (REQ-025, REQ-026, REQ-027)
- [ ] T064 [US8] frontend/tests/e2e/us8-code-groups.spec.ts에 SCR-CODE-GROUPS /admin/code-groups의 원문 primary AC와 loading/empty/error/permission/success를 검증하는 RED 브라우저 시험을 먼저 작성한다. (REQ-025, REQ-026)

### Implementation for User Story 8

- [ ] T065 [US8] backend/src/main/resources/db/migration/V009__code_groups_constraints.sql, backend/src/main/resources/db/test-fixtures/us8.sql에 code_groups의 검증된 constraint·fixture FX-US8를 구현한다. source 미확인 필드·보관·삭제 정책을 추가하지 않는다. (REQ-025, REQ-026, REQ-027)
- [ ] T066 [US8] backend/src/main/java/org/example/oct19/service/CodeGroupService.java, backend/src/main/java/org/example/oct19/mapper/CodeGroupMapper.java, backend/src/main/resources/mapper/CodeGroupMapper.xml에 blocking MyBatis·transaction과 해당 원문 규칙을 구현한다. (REQ-025, REQ-026, REQ-027)
- [ ] T067 [US8] backend/src/main/java/org/example/oct19/api/CodeGroupController.java에 listCodeGroups/saveCodeGroup를 연결하고 DTO·envelope·서버 권한 및 비간섭 검증으로 앞선 contract 시험을 green으로 만든다. (REQ-025, REQ-026, REQ-027)
- [ ] T068 [US8] frontend/src/pages/CodeGroupPage.tsx, frontend/src/api/code_groups.ts에 SCR-CODE-GROUPS /admin/code-groups의 고유 wireframe·데이터 binding·API와 local-only 동작을 구현한다. OQ UI는 임의로 확정하지 않고 deferred 표시한다. (REQ-025, REQ-026)
- [ ] T069 [US8] frontend/tests/e2e/us8-code-groups.spec.ts, backend/src/test/java/org/example/oct19/CodeGroupContractTest.java에 저장 후 재조회·해당 비간섭 snapshot 회귀 사례를 완성하고 실제 green 결과를 .specify/specs/001-feature/quickstart.md의 검증 기록에 남긴다. (REQ-025, REQ-026, REQ-027)

**Checkpoint**: US8 코드그룹 관리의 원문 AC 및 해당 slice API/UI/DB 시험이 독립 통과하고 OQ 차단 항목이 분리되어 있어야 한다.

## Phase 11: User Story 9 - 상세코드 관리 (Priority: P2)

**Goal**: SCR-DETAIL-CODES `/admin/detail-codes`에서 그룹별 상세코드 조회 / 상세코드 필드 저장 / 선택값 정렬순서 반영 / 상위코드 계층 식별. 데이터 `detail_codes`, API `listDetailCodes`/`saveDetailCode`, fixture `FX-US9`를 연결한다.

**Independent Test**: 코드그룹을 선택하면 해당 그룹의 상세코드만 조회된다. 상세코드의 명시 필드를 저장하고 재조회할 수 있다. 정렬순서를 변경하면 화면 선택값이 같은 순서로 제공된다. 상위코드를 지정한 코드는 계층관계로 식별할 수 있다. 다른 US 화면 없이 해당 fixture와 API로 검증한다.

### Tests for User Story 9

- [ ] T070 [US9] backend/src/test/java/org/example/oct19/DetailCodeContractTest.java에 listDetailCodes/saveDetailCode의 원문 AC·입력 검증·권한·비간섭·기간 또는 이력 시험을 먼저 작성하고 미구현 실패를 기록한다. x-required-tests의 미정 정책은 승인 후 assertion을 구체화한다. (REQ-028, REQ-029, REQ-030, REQ-031)
- [ ] T071 [US9] frontend/tests/e2e/us9-detail-codes.spec.ts에 SCR-DETAIL-CODES /admin/detail-codes의 원문 primary AC와 loading/empty/error/permission/success를 검증하는 RED 브라우저 시험을 먼저 작성한다. (REQ-028, REQ-029, REQ-030, REQ-031)

### Implementation for User Story 9

- [ ] T072 [US9] backend/src/main/resources/db/migration/V010__detail_codes_constraints.sql, backend/src/main/resources/db/test-fixtures/us9.sql에 detail_codes의 검증된 constraint·fixture FX-US9를 구현한다. source 미확인 필드·보관·삭제 정책을 추가하지 않는다. (REQ-028, REQ-029, REQ-030, REQ-031)
- [ ] T073 [US9] backend/src/main/java/org/example/oct19/service/DetailCodeService.java, backend/src/main/java/org/example/oct19/mapper/DetailCodeMapper.java, backend/src/main/resources/mapper/DetailCodeMapper.xml에 blocking MyBatis·transaction과 해당 원문 규칙을 구현한다. (REQ-028, REQ-029, REQ-030, REQ-031)
- [ ] T074 [US9] backend/src/main/java/org/example/oct19/api/DetailCodeController.java에 listDetailCodes/saveDetailCode를 연결하고 DTO·envelope·서버 권한 및 비간섭 검증으로 앞선 contract 시험을 green으로 만든다. (REQ-028, REQ-029, REQ-030, REQ-031)
- [ ] T075 [US9] frontend/src/pages/DetailCodePage.tsx, frontend/src/api/detail_codes.ts에 SCR-DETAIL-CODES /admin/detail-codes의 고유 wireframe·데이터 binding·API와 local-only 동작을 구현한다. OQ UI는 임의로 확정하지 않고 deferred 표시한다. (REQ-028, REQ-029, REQ-030, REQ-031)
- [ ] T076 [US9] frontend/tests/e2e/us9-detail-codes.spec.ts, backend/src/test/java/org/example/oct19/DetailCodeContractTest.java에 저장 후 재조회·해당 비간섭 snapshot 회귀 사례를 완성하고 실제 green 결과를 .specify/specs/001-feature/quickstart.md의 검증 기록에 남긴다. (REQ-028, REQ-029, REQ-030, REQ-031)

**Checkpoint**: US9 상세코드 관리의 원문 AC 및 해당 slice API/UI/DB 시험이 독립 통과하고 OQ 차단 항목이 분리되어 있어야 한다.

## Phase 12: Polish & Cross-Cutting Concerns

**Purpose**: 공통 실행과 문서 동기화·산출물 회귀검증.

- [ ] T077 infra/tests/compose-smoke.test.sh에 Compose build·PostgreSQL migration·backend health·frontend 응답·API smoke·context별 dockerignore 검사 RED를 먼저 작성한다. (REQ-035, REQ-040, REQ-041, REQ-049, REQ-050)
- [ ] T078 infra/docker-compose.yml, backend/Dockerfile, frontend/Dockerfile에 실제 build/readiness·port 연결을 완성하고 compose-smoke 시험을 green으로 만든다. in-memory DB 대체는 허용하지 않는다. (REQ-034, REQ-035, REQ-040, REQ-049)
- [ ] T079 .specify/specs/001-feature/spec.md, .specify/specs/001-feature/plan.md, .specify/specs/001-feature/tasks.md, .specify/specs/001-feature/ui-design.md, .specify/specs/001-feature/quickstart.md에 실제 승인된 정책·디자인과 실행 결과를 동기화한다. plan exact marker를 보존하고 원장 의미를 재번호로 바꾸지 않는다. (REQ-043, REQ-044, REQ-045, REQ-046, REQ-047, REQ-049)

## Dependencies & Execution Order

### Phase Dependencies

Setup → Foundational → 각 US의 RED → schema/fixture → mapper/service → API → UI → green 회귀 → Polish. Database는 같은 slice의 backend보다 먼저 준비한다. frontend는 해당 API 계약과 공통 shell에 의존한다. infra build context는 backend/frontend 골격을 참조하고 최종 실행은 앱 빌드 후 수행한다.

### User Story Dependencies

US1·US2는 foundation의 사용자/조직 fixture로 각각 검증한다. US3 정의 → US4 부여라는 제품 데이터 의존은 있으나 US4는 승인된 역할 fixture로 독립 검증 가능하다. US5는 사용자·조직·역할·메뉴 후보 조회가 필요하고 OQ-AUTH-002를 선행 해결한다. US6·US7은 공통 메뉴 키를 공유하므로 migration 충돌을 조정한다. US8·US9는 코드그룹 키를 공유하되 US9는 fixture 그룹으로 독립 검증한다. 동일 파일을 쓰는 schema·mapper 수정은 병렬 처리하지 않는다.

### Within Each User Story

실패 시험의 이유를 확인 → 최소 구현 → 개별 green → 전체 회귀 → 리팩터 순서다. 마감 checkpoint는 미확인 정책 pending을 감추지 않는다.

### Parallel Opportunities

이 계획은 보수적으로 [P]를 사용하지 않았다. 별도 US 테스트 파일 작성은 shared schema와 승인 작업에 의존하지 않는 경우에만 병렬로 배정할 수 있다. 테스트를 green으로 만드는 구현은 같은 테스트의 선행 완료에 의존한다.

## Parallel Example: User Story 1

US1 backend ContractTest와 브라우저 spec의 작성은 파일이 다르지만 둘 다 foundation 준비와 정책 결정을 선행 조건으로 가진다. 해당 조건을 충족한 시점의 두 RED 작성만 병렬 가능하며 mapper/API/UI의 순서는 유지한다.

## Implementation Strategy

### MVP First (User Story 1 Only)

Setup·Foundation → US1의 사용자 검색·표시·사용여부·역할 변경 연결 → DB 재조회와 원천 변경 거부 검증 → 결과 승인. 다른 여정의 기능을 US1에 합치지 않는다.

### Incremental Delivery

각 US를 독립 fixture로 검증하고 기능마다 원문 AC를 통과시킨 뒤 다음 여정을 추가한다. 최종 Compose 실행에서 API와 frontend를 동시에 확인한다.

### Parallel Team Strategy

공통 스키마·보안·메뉴 helper를 먼저 담당자가 조정한다. 이후 별도 화면·service 파일은 분담하되 shared table migration과 menu permission 판단을 단일 책임자가 통합한다.

## Notes

모든 작업은 아직 미완료다. 코드 작성 전 failing test를 관찰하며 commit/push는 이 작업에서 요청되지 않았다. 정의되지 않은 삭제·bulk·export·승인대기 상태를 임의 구현하지 않는다.


## 요구 회계

| canonical_id | status | evidence |
| --- | --- | --- |
| REQ-001 | covered | T014, T015, T016, T017, T018, T019, T020 · 사용자 조건 검색 |
| REQ-002 | covered | T014, T015, T016, T017, T018, T019, T020 · 사용자 인사정보 목록 표시 |
| REQ-003 | covered | T014, T015, T016, T017, T018, T019, T020 · 사용여부·업무 역할 변경 유지 |
| REQ-004 | covered | T014, T016, T017, T018, T020 · 원천 인사정보 변경 거부 |
| REQ-005 | covered | T021, T022, T023, T024, T025, T026, T027 · 조직 현재 관계 조회 |
| REQ-006 | covered | T021, T022, T023, T024, T025, T026, T027 · 상위조직 적용기간 저장 |
| REQ-007 | covered | T021, T022, T023, T024, T025, T026, T027 · 변경 전 조직관계 이력 조회 |
| REQ-008 | covered | T021, T023, T024, T025, T027 · 조직변경의 사용자정보 비간섭 |
| REQ-009 | covered | T028, T029, T030, T031, T032, T033, T034 · 역할 정의 저장·재조회 |
| REQ-010 | covered | T028, T030, T031, T032, T034 · 역할 식별정보 구분 |
| REQ-011 | covered | T028, T030, T031, T032, T034 · 역할 정의와 부여내역 분리 |
| REQ-012 | covered | T035, T036, T037, T038, T039, T040, T041 · 다중 역할·상이한 기간 부여 |
| REQ-013 | covered | T035, T036, T037, T038, T039, T040, T041 · 부여·변경·회수 승인자 기록 |
| REQ-014 | covered | T035, T036, T037, T038, T039, T040, T041 · 역할 발생 출처 구분 조회 |
| REQ-015 | covered | T035, T037, T038, T039, T041 · 종료된 역할 현재 적용 제외 |
| REQ-016 | covered | T042, T043, T044, T045, T046, T047, T048 · 메뉴 접근 허용 설정 저장 |
| REQ-017 | covered | T010, T011, T012, T013, T042, T044, T045, T046, T048 · 허용되지 않은 메뉴 숨김 |
| REQ-018 | covered | T008, T009, T042, T044, T045, T046, T048 · 비허용 URL 서버 차단 |
| REQ-019 | covered | T049, T050, T051, T052, T053, T054, T055 · 부모메뉴·계층 저장 |
| REQ-020 | covered | T012, T013, T049, T050, T051, T052, T053, T054, T055 · 동일 계층 표시순서 적용 |
| REQ-021 | covered | T049, T051, T052, T053, T055 · 다른 부모 아래 순서 보존 |
| REQ-022 | covered | T056, T057, T058, T059, T060, T061, T062 · 메뉴 실행정보 저장 |
| REQ-023 | covered | T012, T013, T056, T057, T058, T059, T060, T061, T062 · 사용 가능한 메뉴 화면 실행 |
| REQ-024 | covered | T056, T058, T059, T060, T062 · 실행정보 변경의 권한 비간섭 |
| REQ-025 | covered | T063, T064, T065, T066, T067, T068, T069 · 코드그룹 정의 저장 |
| REQ-026 | covered | T063, T064, T065, T066, T067, T068, T069 · 그룹ID별 독립 조회 |
| REQ-027 | covered | T063, T065, T066, T067, T069 · 그룹 수정의 상세코드 비간섭 |
| REQ-028 | covered | T070, T071, T072, T073, T074, T075, T076 · 그룹별 상세코드 조회 |
| REQ-029 | covered | T070, T071, T072, T073, T074, T075, T076 · 상세코드 필드 저장 |
| REQ-030 | covered | T070, T071, T072, T073, T074, T075, T076 · 선택값 정렬순서 반영 |
| REQ-031 | covered | T070, T071, T072, T073, T074, T075, T076 · 상위코드 계층 식별 |
| REQ-032 | covered | T001, T003 · Frontend 기술스택 |
| REQ-033 | covered | T001, T002 · Backend 기술스택 |
| REQ-034 | covered | T001, T004, T006, T007, T078 · Database 기술스택 |
| REQ-035 | covered | T001, T004, T077, T078 · Infrastructure 기술스택 |
| REQ-036 | covered | T002, T006, T007 · 동기·blocking MyBatis 실행·영속성 |
| REQ-037 | covered | T001, T003 · Version BOM 호환성 |
| REQ-038 | covered | T001, T002 · backend_dir 필수 산출물 |
| REQ-039 | covered | T001, T003 · frontend_dir 필수 산출물 |
| REQ-040 | covered | T001, T004, T077, T078 · compose_file 필수 산출물 |
| REQ-041 | covered | T001, T008, T009, T077 · health_endpoint 필수 산출물 |
| REQ-042 | covered | T010, T011 · 레퍼런스 UI/UX 적용 |
| REQ-043 | covered | T079 · 명세의 스택·산출물·범위 명시 |
| REQ-044 | covered | T079 · 계획의 계층 구조·생성순서 명시 |
| REQ-045 | covered | T079 · 작업의 계층별 구현 포함 |
| REQ-046 | covered | T010, T011, T079 · UI 레퍼런스 설계 기준 명시 |
| REQ-047 | covered | T079 · plan 실행·영속성 정본 보존 |
| REQ-048 | covered | T006, T007 · DB 스키마·migration 기준 |
| REQ-049 | covered | T077, T078, T079 · Backend·Frontend 공동 검증 |
| REQ-050 | covered | T005, T077 · build context 제외 파일 |
