# 구현 계획: oct19 공통 관리

**Branch**: `master` | **Date**: 2026-10-19 | **Spec**: [spec.md](spec.md)

**Input**: `.specify/specs/001-feature/spec.md`

## Summary

아홉 독립 관리 여정을 React/TypeScript와 동기 Spring Boot/MyBatis API로 구현하고 PostgreSQL에 저장한다. 원장 행위 분해와 feature-local AC를 기준으로 vertical slice를 수행한다. REQ-001부터 각 실제 원장 항목을 tasks.md에서 개별 연결한다. 정책 OQ가 남아 있어 전체 구현 준비 완료 승인은 보류한다.

## Technical Context

**Language/Version**: Java 17, TypeScript(버전 미지정), Node 20.x.
**Primary Dependencies**: Spring Boot 3.3.x, MyBatis(starter 버전 OQ-TECH-001), React 18.x, Vite 5.x.
**Storage**: PostgreSQL 16.x; blocking MyBatis mapper와 imperative service transaction.
**Testing**: Maven wrapper + JUnit/Spring 통합시험 및 PostgreSQL 컨테이너, Vitest/Testing Library와 Playwright를 기술 제안한다. 미정 정책 시험은 승인 전 pending으로 관리한다.
**Target Platform**: Docker Compose 로컬 실행; 실제 운영 환경 OQ-TECH-001.
**Project Type**: frontend/backend 분리 웹 앱.
**Performance Goals**: 원문 수치 없음; OQ-TECH-001. 임의 SLA를 추가하지 않는다.
**Constraints**: KORUS 인사정보 readonly, 실효 불허 URL 서버 차단, 비간섭 제약.
**Scale/Scope**: 아홉 관리 화면; 목록 필터·규모·페이지 기본값은 원문 근거와 OQ로 분리한다.

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
## 실행·영속성 결정 정본

아래는 SPEC-REQUEST.md에서 복사한 exact marker+JSON이며 이 plan.md가 의미 정본이다. decision_status=resolved, imperative/mybatis/blocking이다. clarification의 추정 응답과 달리 명시 실행 계약으로 확정되었다. 사업 승인 OQ까지 해결되었다는 뜻은 아니다. REQ-036, REQ-047.

<!-- aiops:execution-persistence-contract:start -->
```json
{
  "decision_status": "resolved",
  "execution_model": "imperative",
  "persistence_technology": "mybatis",
  "access_mode": "blocking",
  "selection_source": "policy-default",
  "blocking_priority": [
    "mybatis",
    "jpa",
    "spring-jdbc"
  ],
  "conflicts": []
}
```
<!-- aiops:execution-persistence-contract:end -->

## Constitution Check

`.specify/memory/constitution.md`는 미작성 placeholder 템플릿이므로 조직 헌장 통과를 주장할 수 없다. 명시 요청 계약, TDD, 원장 의미 보존, 출력 범위를 적용한다. 문서 게이트: 출처 인용·행위 분해·AC·원장 회계·OpenAPI 스키마 검사. 실행 게이트: Java/Node BOM, DB migration, 백엔드·프론트엔드 실행, 권한 시험. 정책 미확인은 OQ로 차단한다.

## Project Structure

### Documentation (this feature)

`.specify/specs/001-feature/`에는 spec.md, plan.md, tasks.md, data-model.md, contracts/openapi.yaml, research.md, quickstart.md, architecture.md, ui-design.md의 아홉 관리 문서와 requirement-registry.json/.md 추적 sidecar만 둔다.

### Source Code (repository root)

아래 경로는 후속 구현 산출물 계획이다. 이번 문서 생성에서 실행 코드를 만들지 않는다.

```text
backend/
  pom.xml, mvnw, .mvn/, Dockerfile, .dockerignore
  src/main/java/org/example/oct19/{api,service,mapper,security}/
  src/main/resources/{application.yml,mapper/,db/migration/,db/test-fixtures/}
  src/test/java/org/example/oct19/
frontend/
  package.json, package-lock.json, vite.config.ts, Dockerfile, .dockerignore
  src/{pages,components,api,styles}/
  tests/{unit,e2e}/
infra/
  docker-compose.yml
  tests/
```

**Structure Decision**: frontend는 Vite UI·공통 상태·메뉴 shell, backend는 API/service/mapper/security, database는 backend migration·fixture, infrastructure는 infra Compose와 앱별 build context로 분리한다. REQ-038, REQ-039, REQ-040, REQ-048, REQ-050.

## 아키텍처

브라우저 React → HTTP REST `/api/*` → Spring Boot imperative controller/service → blocking MyBatis mapper → PostgreSQL. 역할·메뉴의 실효 권한은 서버와 UI에 같은 판정 결과를 전달한다. frontend 개발 프록시 및 배포 origin은 승인된 포트/인증 설정과 함께 정한다. 각 저장은 소유 table 단위 트랜잭션이며 조직 관계 이력은 같은 transaction에서 보존한다. 상세 흐름은 architecture.md의 operationId 참조 표를 따른다. REQ-004, REQ-007, REQ-015, REQ-018, REQ-036.

## 계층별 구현 구조와 생성 순서

1. Setup: backend/pom.xml·Maven wrapper, frontend/package.json·Vite, infra/docker-compose.yml 및 각 build context의 .dockerignore 골격을 준비한다.
2. Database: Entity Registry를 기준으로 migration·키·FK와 검증 fixture를 작성하고 실패하는 mapping/transaction 시험을 실행한다. Spring Boot+MyBatis blocking mapper의 기본 연결을 만든다.
3. Backend foundation: ApiResponse/ApiError, 보안 adapter 인터페이스, health, 서버 접근 가드를 TDD로 구현한다. 미승인 인증·권한 정책은 구성 경계로 격리하고 기능 통과를 선언하지 않는다.
4. Frontend foundation: sidebar/header·상태 컴포넌트·ApiResponse 해석·접근 실패 처리를 작성한다. 디자인 reference의 실제 자산과 토큰은 OQ-UI-004 승인 대상이다.
5. US1–US9 vertical slice: 각각 failing backend/API·UI 시험 → 소유 schema/fixture → mapper/service/API → 해당 React 화면 → green 및 비간섭 회귀검증 순서다.
6. Infrastructure integration: 앱별 Dockerfile build, PostgreSQL readiness·앱 의존관계, backend/frontend 포트 연결을 실행 검증하고 quickstart smoke를 기록한다.

## 범위별 책임

| canonical_id | 구현 소유 경계 | 검증 |
| --- | --- | --- |
| REQ-001 | US1 / users / updateUserSettings | CMN-FR-001/AC-01: 교번·성명·소속·직급·재직상태·역할·사용여부 중 하나 이상의 조건으로 검색하면 조건에 맞는 사용자만 표시된다. |
| REQ-002 | US1 / users / updateUserSettings | CMN-FR-001/AC-02: 사용자 목록에 보직·퇴직일자·최종 동기화일시를 포함한 명시 항목이 표시된다. |
| REQ-003 | US1 / users / updateUserSettings | CMN-FR-001/AC-03: 시스템 사용여부 또는 업무 역할을 변경하고 다시 조회하면 변경값이 유지된다. |
| REQ-004 | US1 / users / updateUserSettings | CMN-FR-001/AC-04: KORUS 원천 인사정보는 화면과 서버 처리에서 변경할 수 없다. |
| REQ-005 | US2 / organizations / saveOrganizationRelation | CMN-FR-002/AC-01: 조직코드로 조회하면 해당 조직의 조직구분과 현재 상위·하위 관계가 표시된다. |
| REQ-006 | US2 / organizations / saveOrganizationRelation | CMN-FR-002/AC-02: 상위조직 변경 시 적용 시작일·종료일과 변경된 상위조직이 함께 저장된다. |
| REQ-007 | US2 / organizations / saveOrganizationRelation | CMN-FR-002/AC-03: 변경 이후에도 변경 전 조직관계와 적용기간을 이력으로 조회할 수 있다. |
| REQ-008 | US2 / organizations / saveOrganizationRelation | CMN-FR-002/AC-04: 조직구조 변경이 사용자의 소속·보직 또는 역할정보를 직접 변경하지 않는다. |
| REQ-009 | US3 / roles / saveRole | CMN-FR-005/AC-01: 역할별 목적·부여 기준·데이터 범위 기본값을 저장하고 재조회할 수 있다. |
| REQ-010 | US3 / roles / saveRole | CMN-FR-005/AC-02: 정의된 역할은 서로 다른 역할 식별정보로 구분된다. |
| REQ-011 | US3 / roles / saveRole | CMN-FR-005/AC-03: 역할 정의 변경만으로 특정 사용자의 역할 부여내역이 직접 변경되지 않는다. |
| REQ-012 | US4 / user_role_assignments / saveUserRoleAssignment | CMN-FR-006/AC-01: 한 사용자에게 둘 이상의 역할을 서로 다른 유효기간으로 부여할 수 있다. |
| REQ-013 | US4 / user_role_assignments / saveUserRoleAssignment | CMN-FR-006/AC-02: 역할을 부여·변경·회수하면 대상 사용자·역할·유효기간·승인자가 함께 저장된다. |
| REQ-014 | US4 / user_role_assignments / saveUserRoleAssignment | CMN-FR-006/AC-03: 보직 기반 역할과 수동 역할을 조회 결과에서 구분할 수 있다. |
| REQ-015 | US4 / user_role_assignments / saveUserRoleAssignment | CMN-FR-006/AC-04: 유효기간이 종료된 역할은 현재 적용 역할로 판정되지 않는다. |
| REQ-016 | US5 / menu_permissions / saveMenuPermission | CMN-FR-007/AC-01: 역할·조직·사용자와 메뉴를 선택해 접근 허용 여부를 저장하고 재조회할 수 있다. |
| REQ-017 | US5 / menu_permissions / saveMenuPermission | CMN-FR-007/AC-02: 접근이 허용되지 않은 메뉴는 사용자 메뉴에 표시되지 않는다. |
| REQ-018 | US5 / menu_permissions / saveMenuPermission | CMN-FR-007/AC-03: 접근이 허용되지 않은 사용자가 URL로 직접 요청해도 서버에서 차단된다. |
| REQ-019 | US6 / menus / saveMenuStructure | CMN-FR-013/AC-01: 메뉴별 부모메뉴와 계층단계를 저장하고 계층구조로 재조회할 수 있다. |
| REQ-020 | US6 / menus / saveMenuStructure | CMN-FR-013/AC-02: 동일 계층의 표시순서를 변경하면 사용자 메뉴에 변경된 순서가 적용된다. |
| REQ-021 | US6 / menus / saveMenuStructure | CMN-FR-013/AC-03: 한 계층의 순서 변경이 다른 부모메뉴 아래 순서를 변경하지 않는다. |
| REQ-022 | US7 / menu_execution_info / saveMenuExecutionInfo | CMN-FR-014/AC-01: 메뉴의 실행정보를 저장하고 동일한 값으로 재조회할 수 있다. |
| REQ-023 | US7 / menu_execution_info / saveMenuExecutionInfo | CMN-FR-014/AC-02: 사용 가능한 메뉴를 선택하면 연결된 화면ID와 URL의 화면이 실행된다. |
| REQ-024 | US7 / menu_execution_info / saveMenuExecutionInfo | CMN-FR-014/AC-03: 메뉴 실행정보 변경이 메뉴 권한 설정을 변경하지 않는다. |
| REQ-025 | US8 / code_groups / saveCodeGroup | CMN-FR-016/AC-01: 그룹ID·명칭·설명·관리부서를 저장하고 재조회할 수 있다. |
| REQ-026 | US8 / code_groups / saveCodeGroup | CMN-FR-016/AC-02: 서로 다른 그룹ID의 코드그룹이 독립적으로 조회된다. |
| REQ-027 | US8 / code_groups / saveCodeGroup | CMN-FR-016/AC-03: 코드그룹 수정이 해당 그룹의 상세코드 값을 임의로 변경하지 않는다. |
| REQ-028 | US9 / detail_codes / saveDetailCode | CMN-FR-017/AC-01: 코드그룹을 선택하면 해당 그룹의 상세코드만 조회된다. |
| REQ-029 | US9 / detail_codes / saveDetailCode | CMN-FR-017/AC-02: 상세코드의 명시 필드를 저장하고 재조회할 수 있다. |
| REQ-030 | US9 / detail_codes / saveDetailCode | CMN-FR-017/AC-03: 정렬순서를 변경하면 화면 선택값이 같은 순서로 제공된다. |
| REQ-031 | US9 / detail_codes / saveDetailCode | CMN-FR-017/AC-04: 상위코드를 지정한 코드는 계층관계로 식별할 수 있다. |
| REQ-032 | non-functional | SPEC-REQUEST.md:78의 Frontend 기술스택를 산출물과 구현 검증에서 확인한다. |
| REQ-033 | non-functional | SPEC-REQUEST.md:79의 Backend 기술스택를 산출물과 구현 검증에서 확인한다. |
| REQ-034 | non-functional | SPEC-REQUEST.md:80의 Database 기술스택를 산출물과 구현 검증에서 확인한다. |
| REQ-035 | non-functional | SPEC-REQUEST.md:81의 Infrastructure 기술스택를 산출물과 구현 검증에서 확인한다. |
| REQ-036 | architecture-runtime | SPEC-REQUEST.md:89의 동기·blocking MyBatis 실행·영속성를 산출물과 구현 검증에서 확인한다. |
| REQ-037 | non-functional | SPEC-REQUEST.md:104의 Version BOM 호환성를 산출물과 구현 검증에서 확인한다. |
| REQ-038 | architecture-runtime | SPEC-REQUEST.md:112의 backend_dir 필수 산출물를 산출물과 구현 검증에서 확인한다. |
| REQ-039 | architecture-runtime | SPEC-REQUEST.md:113의 frontend_dir 필수 산출물를 산출물과 구현 검증에서 확인한다. |
| REQ-040 | architecture-runtime | SPEC-REQUEST.md:114의 compose_file 필수 산출물를 산출물과 구현 검증에서 확인한다. |
| REQ-041 | api-contract | SPEC-REQUEST.md:115의 health_endpoint 필수 산출물를 산출물과 구현 검증에서 확인한다. |
| REQ-042 | ui-supporting | SPEC-REQUEST.md:118의 레퍼런스 UI/UX 적용를 산출물과 구현 검증에서 확인한다. |
| REQ-043 | non-functional | SPEC-REQUEST.md:121의 명세의 스택·산출물·범위 명시를 산출물과 구현 검증에서 확인한다. |
| REQ-044 | non-functional | SPEC-REQUEST.md:122의 계획의 계층 구조·생성순서 명시를 산출물과 구현 검증에서 확인한다. |
| REQ-045 | non-functional | SPEC-REQUEST.md:123의 작업의 계층별 구현 포함를 산출물과 구현 검증에서 확인한다. |
| REQ-046 | ui-supporting | SPEC-REQUEST.md:124의 UI 레퍼런스 설계 기준 명시를 산출물과 구현 검증에서 확인한다. |
| REQ-047 | architecture-runtime | SPEC-REQUEST.md:125의 plan 실행·영속성 정본 보존를 산출물과 구현 검증에서 확인한다. |
| REQ-048 | data-contract | SPEC-REQUEST.md:130의 DB 스키마·migration 기준를 산출물과 구현 검증에서 확인한다. |
| REQ-049 | seed-test-fixture | SPEC-REQUEST.md:131의 Backend·Frontend 공동 검증를 산출물과 구현 검증에서 확인한다. |
| REQ-050 | architecture-runtime | SPEC-REQUEST.md:132의 build context 제외 파일를 산출물과 구현 검증에서 확인한다. |


## Complexity Tracking

기술 요구 위반을 합리화하지 않는다. 인증/권한·조직 소유권·승인자·상세코드 필드와 디자인의 미정은 OQ이며 별도 시스템을 추가해 해결한 것으로 간주하지 않는다. 단일 backend와 PostgreSQL을 유지하고 불필요한 worker·queue·audit 서비스를 만들지 않는다.

## 확인 필요 사항

| OQ | 미정 결정·차단 범위 | 원문 행 |
| --- | --- | --- |
| OQ-AUTH-001 | 인증 방식·역할 코드·관리 범위·권한 조합 우선순위는 미확인이다. bearerAuth는 API 설계 제안이며 실제 토큰 발급·기관 연계·부서 위임 범위를 승인받아야 한다. | 167–174 |
| OQ-ORG-001 | 조직정보의 KORUS/로컬 소유권, 기간 경계 포함 여부, 중복·순환 판정, 과거 정정·미래 예약 정책이 미확인이다. | 165–166,175–176 |
| OQ-ROLE-001 | 승인자 신원 검증·승인 단계·보직 매핑·회수의 효력 시점이 미확인이다. 현재 행위자를 승인자로 자동 지정하지 않는다. | 169–172 |
| OQ-DATA-001 | KORUS 실제 연결 규격, 초기 적재·동기화 경계, 사용여부·수동 역할 보존 방식은 미확인이다. 원천 인사정보 수정 금지는 확정이다. | 165–166 |
| OQ-UI-001 | 원문에 메뉴 경로·route·화면ID가 없어 제안한 3단계 메뉴·명명·레이아웃의 승인이 필요하다. | 117–118,177–188 |
| OQ-UI-002 | 상세코드의 명시 필드 전체 목록은 없다. code/group_id/sort_order/parent_code 외 저장 필드와 필수성은 확인 필요다. | 64–70 |
| OQ-UI-003 | 메뉴의 사용 가능 판정 근거와 실행정보의 추가 필드, 사용자 역할 변경 UI와 기간·승인자 입력 통합 방식은 확인 필요다. | 17,55–57 |
| OQ-DATA-002 | 삭제·보관·참조 보호 및 퇴직·사용중지 접속 차단의 구체 정책은 미확인이다. 삭제 API는 이번 설계에서 추가하지 않되 제외 승인으로 간주하지 않는다. | 171–186 |
| OQ-TECH-001 | 패치 버전·MyBatis starter·migration 도구 버전, 성능 규모·필드 길이·최대 입력 크기는 미지정이다. 기술 선택 제안은 테스트로 고정하기 전 검토한다. | 78–109 |
| OQ-UI-004 | 구체 레퍼런스 자산·색상 토큰·반응형 임계값은 제공되지 않았다. dashboard card 언급은 대시보드 기능 승인이 아니다. | 117–118,177–188 |
| OQ-AUTH-002 | 권한 편집 화면의 메뉴·조직·사용자·역할 후보 조회와 일반 사용자의 메뉴 읽기 범위는 승인 필요다. 기본 차단·우선순위를 미리 확정하지 않는다. | 43–45,173–174 |


## 요구 회계

| canonical_id | status | evidence |
| --- | --- | --- |
| REQ-001 | covered | 범위별 책임 · users · searchUsers/updateUserSettings · 사용자 조건 검색 |
| REQ-002 | covered | 범위별 책임 · users · searchUsers/updateUserSettings · 사용자 인사정보 목록 표시 |
| REQ-003 | covered | 범위별 책임 · users · searchUsers/updateUserSettings · 사용여부·업무 역할 변경 유지 |
| REQ-004 | covered | 범위별 책임 · users · searchUsers/updateUserSettings · 원천 인사정보 변경 거부 |
| REQ-005 | covered | 범위별 책임 · organizations · getOrganizationRelations/saveOrganizationRelation · 조직 현재 관계 조회 |
| REQ-006 | covered | 범위별 책임 · organizations · getOrganizationRelations/saveOrganizationRelation · 상위조직 적용기간 저장 |
| REQ-007 | covered | 범위별 책임 · organizations · getOrganizationRelations/saveOrganizationRelation · 변경 전 조직관계 이력 조회 |
| REQ-008 | data-constraint | 범위별 책임 · organizations · getOrganizationRelations/saveOrganizationRelation · 조직변경의 사용자정보 비간섭 |
| REQ-009 | covered | 범위별 책임 · roles · listRoles/saveRole · 역할 정의 저장·재조회 |
| REQ-010 | covered | 범위별 책임 · roles · listRoles/saveRole · 역할 식별정보 구분 |
| REQ-011 | data-constraint | 범위별 책임 · roles · listRoles/saveRole · 역할 정의와 부여내역 분리 |
| REQ-012 | covered | 범위별 책임 · user_role_assignments · listUserRoleAssignments/saveUserRoleAssignment · 다중 역할·상이한 기간 부여 |
| REQ-013 | covered | 범위별 책임 · user_role_assignments · listUserRoleAssignments/saveUserRoleAssignment · 부여·변경·회수 승인자 기록 |
| REQ-014 | covered | 범위별 책임 · user_role_assignments · listUserRoleAssignments/saveUserRoleAssignment · 역할 발생 출처 구분 조회 |
| REQ-015 | covered | 범위별 책임 · user_role_assignments · listUserRoleAssignments/saveUserRoleAssignment · 종료된 역할 현재 적용 제외 |
| REQ-016 | covered | 범위별 책임 · menu_permissions · listMenuPermissions/saveMenuPermission · 메뉴 접근 허용 설정 저장 |
| REQ-017 | screen-only | 범위별 책임 · menu_permissions · listMenuPermissions/saveMenuPermission · 허용되지 않은 메뉴 숨김 |
| REQ-018 | covered | 범위별 책임 · menu_permissions · listMenuPermissions/saveMenuPermission · 비허용 URL 서버 차단 |
| REQ-019 | covered | 범위별 책임 · menus · getMenuStructure/saveMenuStructure · 부모메뉴·계층 저장 |
| REQ-020 | covered | 범위별 책임 · menus · getMenuStructure/saveMenuStructure · 동일 계층 표시순서 적용 |
| REQ-021 | data-constraint | 범위별 책임 · menus · getMenuStructure/saveMenuStructure · 다른 부모 아래 순서 보존 |
| REQ-022 | covered | 범위별 책임 · menu_execution_info · listMenuExecutionInfo/saveMenuExecutionInfo · 메뉴 실행정보 저장 |
| REQ-023 | covered | 범위별 책임 · menu_execution_info · listMenuExecutionInfo/saveMenuExecutionInfo · 사용 가능한 메뉴 화면 실행 |
| REQ-024 | data-constraint | 범위별 책임 · menu_execution_info · listMenuExecutionInfo/saveMenuExecutionInfo · 실행정보 변경의 권한 비간섭 |
| REQ-025 | covered | 범위별 책임 · code_groups · listCodeGroups/saveCodeGroup · 코드그룹 정의 저장 |
| REQ-026 | covered | 범위별 책임 · code_groups · listCodeGroups/saveCodeGroup · 그룹ID별 독립 조회 |
| REQ-027 | data-constraint | 범위별 책임 · code_groups · listCodeGroups/saveCodeGroup · 그룹 수정의 상세코드 비간섭 |
| REQ-028 | covered | 범위별 책임 · detail_codes · listDetailCodes/saveDetailCode · 그룹별 상세코드 조회 |
| REQ-029 | covered | 범위별 책임 · detail_codes · listDetailCodes/saveDetailCode · 상세코드 필드 저장 |
| REQ-030 | covered | 범위별 책임 · detail_codes · listDetailCodes/saveDetailCode · 선택값 정렬순서 반영 |
| REQ-031 | covered | 범위별 책임 · detail_codes · listDetailCodes/saveDetailCode · 상위코드 계층 식별 |
| REQ-032 | non-functional | 범위별 책임 · React/TypeScript/Vite 번들 선택은 frontend/package.json의 빌드 의존성 계약이며 REST 입출력이 아니다. |
| REQ-033 | non-functional | 범위별 책임 · Java 17/Spring Boot 선택은 backend/pom.xml과 런타임의 컴파일 계약이다. |
| REQ-034 | non-functional | 범위별 책임 · PostgreSQL 선택은 data-model.md의 매핑·DB 접속 대상이지 신규 업무 API가 아니다. |
| REQ-035 | non-functional | 범위별 책임 · Docker Compose 실행 방식은 infra/docker-compose.yml의 서비스 연결 책임이다. |
| REQ-036 | non-functional | 범위별 책임 · 동기 blocking MyBatis는 plan.md marker의 실행 결정으로 모든 요청 처리 내부에 적용된다. |
| REQ-037 | non-functional | 범위별 책임 · BOM은 frontend/package.json/backend/pom.xml/DB 이미지 버전 검증으로 충족한다. |
| REQ-038 | non-functional | 범위별 책임 · backend 디렉터리 산출 여부는 저장소 파일 검사 대상이다. |
| REQ-039 | non-functional | 범위별 책임 · frontend 디렉터리 산출 여부는 저장소 파일 검사 대상이다. |
| REQ-040 | non-functional | 범위별 책임 · Compose 파일 위치는 infra/docker-compose.yml 파일 존재와 config 검사로 확인한다. |
| REQ-041 | covered | 범위별 책임 · getHealth /api/health |
| REQ-042 | non-functional | 범위별 책임 · 레퍼런스의 sidebar/header/밀도/토큰/반응형은 ui-design.md 공통 시각 설계이며 API를 만들지 않는다. |
| REQ-043 | non-functional | 범위별 책임 · spec.md 기술스택 계약에서 백엔드·프론트엔드 범위를 문서화한다. |
| REQ-044 | non-functional | 범위별 책임 · plan.md Project Structure와 생성 순서가 소유하는 구현 배치 계약이다. |
| REQ-045 | non-functional | 범위별 책임 · tasks.md Setup/Foundational/US 단계가 backend/frontend/infra 작업을 배정한다. |
| REQ-046 | non-functional | 범위별 책임 · ui-design.md 레퍼런스 구현 검수 기준은 화면 공통 스타일 책임이다. |
| REQ-047 | non-functional | 범위별 책임 · plan.md의 exact marker JSON 보존은 문서 정본 검증 대상이다. |
| REQ-048 | data-constraint | 범위별 책임 · data-model.md Entity Registry와 tasks.md migration 작업이 스키마 기준을 충족한다. |
| REQ-049 | non-functional | 범위별 책임 · quickstart.md API/UI 동시 smoke 절차와 seed fixture로 검증한다. |
| REQ-050 | non-functional | 범위별 책임 · 각 build context의 .dockerignore 제외 집합을 infra 검사에서 확인한다. |
