# 시스템 아키텍처 — oct19

## 시스템 맥락과 컴포넌트·데이터 흐름

```text
[권한 승인된 관리 사용자 / 허용 메뉴 사용자]
                 |
          [React frontend]
     sidebar / header / 9개 관리 화면
                 | HTTP /api/*
       [Spring Boot backend]
 인증 adapter -> 실효 메뉴 접근 guard -> controller
                 | 동기 service transaction
       [blocking MyBatis mapper]
                 | JDBC
        [PostgreSQL 16.x]

[Docker Compose] -> frontend / backend / database
[KORUS 인사 원천] -- 연계 경계 승인 필요 --> [원천 readonly projection]
```

KORUS 실제 호출 규격과 적재 경로는 OQ-DATA-001이며 이미 연계된 것으로 그리지 않는다. health는 운영 probe이며 사용자 화면이 아니다. 신규 queue·worker·search·storage 시스템은 요청되지 않았다.

## 요청과 트랜잭션 경계

GET은 read-only 조회, PUT은 동기 처리 완료 후 저장된 결과를 반환한다. 202/polling/callback 계약은 필요하지 않다. Java imperative service가 transaction을 열고 mapper가 같은 JDBC transaction에 참여한다. 조직관계 변경은 이전 관계·기간이 조회 가능하도록 보존하는 단일 transaction이며 users/역할 부여를 변경하지 않는다. 역할 정의와 부여내역, 메뉴 구조와 다른 부모 순서, 메뉴 실행정보와 권한, 코드그룹과 상세코드의 비간섭을 검증한다. REQ-006, REQ-007, REQ-008, REQ-011, REQ-021, REQ-024, REQ-027, REQ-036.

## 컴포넌트 API 의존성

| Component | OpenAPI operationId 또는 path | Data Entity | canonical_id |
| --- | --- | --- | --- |
| SCR-USERS → UserService / Mapper | searchUsers / updateUserSettings | users | REQ-001, REQ-002, REQ-003, REQ-004 |
| SCR-ORGANIZATIONS → OrganizationService / Mapper | getOrganizationRelations / saveOrganizationRelation | organizations | REQ-005, REQ-006, REQ-007, REQ-008 |
| SCR-ROLES → RoleService / Mapper | listRoles / saveRole | roles | REQ-009, REQ-010, REQ-011 |
| SCR-USER-ROLES → UserRoleAssignmentService / Mapper | listUserRoleAssignments / saveUserRoleAssignment | user_role_assignments | REQ-012, REQ-013, REQ-014, REQ-015 |
| SCR-MENU-PERMISSIONS → MenuPermissionService / Mapper | listMenuPermissions / saveMenuPermission | menu_permissions | REQ-016, REQ-017, REQ-018 |
| SCR-MENU-STRUCTURE → MenuStructureService / Mapper | getMenuStructure / saveMenuStructure | menus | REQ-019, REQ-020, REQ-021 |
| SCR-MENU-INFO → MenuExecutionInfoService / Mapper | listMenuExecutionInfo / saveMenuExecutionInfo | menu_execution_info | REQ-022, REQ-023, REQ-024 |
| SCR-CODE-GROUPS → CodeGroupService / Mapper | listCodeGroups / saveCodeGroup | code_groups | REQ-025, REQ-026, REQ-027 |
| SCR-DETAIL-CODES → DetailCodeService / Mapper | listDetailCodes / saveDetailCode | detail_codes | REQ-028, REQ-029, REQ-030, REQ-031 |
| 조직관계 이력 조회 | listOrganizationRelationHistory | organization_relations | REQ-007 |
| 공통 sidebar / 서버 접근 guard | getAccessibleMenus | menus, menu_execution_info, menu_permissions, user_role_assignments | REQ-017, REQ-018, REQ-020, REQ-023 |
| 서비스 probe | getHealth | 업무 table 없음 | REQ-041 |

## 보안·원천 경계

- bearerAuth는 기술 설계 제안이다. 기관 인증·검증용 인증·역할 코드·승인 주체를 OQ-AUTH-001/OQ-ROLE-001로 승인해야 한다. 계정 seed가 관리 권한 고정 정책을 만들지 않는다.
- 사용자 메뉴 숨김만으로 보안을 충족하지 않는다. 실효 불허 URL은 서버에서 거부하고 protected write가 없어야 한다. 역할·조직·사용자 권한 우선순위는 추정하지 않는다. REQ-017, REQ-018.
- 인사 원천 필드 변경을 UI에서 차단하고 backend allowlist DTO/mapper에서도 거부한다. 업무 역할은 user_role_assignments를 통해 변경한다. REQ-003, REQ-004.
- 보직 기반/수동 역할 origin은 조회에 표시한다. 기간이 종료된 역할을 현재로 판정하지 않으며 그 밖의 lifecycle 정책은 OQ로 남긴다. REQ-014, REQ-015.

## 계층별 소유 책임

| canonical_id | 설계 경계와 검증 |
| --- | --- |
| REQ-001 | users / updateUserSettings — 교번·성명·소속·직급·재직상태·역할·사용여부 중 하나 이상의 조건으로 검색하면 조건에 맞는 사용자만 표시된다. |
| REQ-002 | users / updateUserSettings — 사용자 목록에 보직·퇴직일자·최종 동기화일시를 포함한 명시 항목이 표시된다. |
| REQ-003 | users / updateUserSettings — 시스템 사용여부 또는 업무 역할을 변경하고 다시 조회하면 변경값이 유지된다. |
| REQ-004 | users / updateUserSettings — KORUS 원천 인사정보는 화면과 서버 처리에서 변경할 수 없다. |
| REQ-005 | organizations / saveOrganizationRelation — 조직코드로 조회하면 해당 조직의 조직구분과 현재 상위·하위 관계가 표시된다. |
| REQ-006 | organizations / saveOrganizationRelation — 상위조직 변경 시 적용 시작일·종료일과 변경된 상위조직이 함께 저장된다. |
| REQ-007 | organizations / saveOrganizationRelation — 변경 이후에도 변경 전 조직관계와 적용기간을 이력으로 조회할 수 있다. |
| REQ-008 | organizations / saveOrganizationRelation — 조직구조 변경이 사용자의 소속·보직 또는 역할정보를 직접 변경하지 않는다. |
| REQ-009 | roles / saveRole — 역할별 목적·부여 기준·데이터 범위 기본값을 저장하고 재조회할 수 있다. |
| REQ-010 | roles / saveRole — 정의된 역할은 서로 다른 역할 식별정보로 구분된다. |
| REQ-011 | roles / saveRole — 역할 정의 변경만으로 특정 사용자의 역할 부여내역이 직접 변경되지 않는다. |
| REQ-012 | user_role_assignments / saveUserRoleAssignment — 한 사용자에게 둘 이상의 역할을 서로 다른 유효기간으로 부여할 수 있다. |
| REQ-013 | user_role_assignments / saveUserRoleAssignment — 역할을 부여·변경·회수하면 대상 사용자·역할·유효기간·승인자가 함께 저장된다. |
| REQ-014 | user_role_assignments / saveUserRoleAssignment — 보직 기반 역할과 수동 역할을 조회 결과에서 구분할 수 있다. |
| REQ-015 | user_role_assignments / saveUserRoleAssignment — 유효기간이 종료된 역할은 현재 적용 역할로 판정되지 않는다. |
| REQ-016 | menu_permissions / saveMenuPermission — 역할·조직·사용자와 메뉴를 선택해 접근 허용 여부를 저장하고 재조회할 수 있다. |
| REQ-017 | menu_permissions / saveMenuPermission — 접근이 허용되지 않은 메뉴는 사용자 메뉴에 표시되지 않는다. |
| REQ-018 | menu_permissions / saveMenuPermission — 접근이 허용되지 않은 사용자가 URL로 직접 요청해도 서버에서 차단된다. |
| REQ-019 | menus / saveMenuStructure — 메뉴별 부모메뉴와 계층단계를 저장하고 계층구조로 재조회할 수 있다. |
| REQ-020 | menus / saveMenuStructure — 동일 계층의 표시순서를 변경하면 사용자 메뉴에 변경된 순서가 적용된다. |
| REQ-021 | menus / saveMenuStructure — 한 계층의 순서 변경이 다른 부모메뉴 아래 순서를 변경하지 않는다. |
| REQ-022 | menu_execution_info / saveMenuExecutionInfo — 메뉴의 실행정보를 저장하고 동일한 값으로 재조회할 수 있다. |
| REQ-023 | menu_execution_info / saveMenuExecutionInfo — 사용 가능한 메뉴를 선택하면 연결된 화면ID와 URL의 화면이 실행된다. |
| REQ-024 | menu_execution_info / saveMenuExecutionInfo — 메뉴 실행정보 변경이 메뉴 권한 설정을 변경하지 않는다. |
| REQ-025 | code_groups / saveCodeGroup — 그룹ID·명칭·설명·관리부서를 저장하고 재조회할 수 있다. |
| REQ-026 | code_groups / saveCodeGroup — 서로 다른 그룹ID의 코드그룹이 독립적으로 조회된다. |
| REQ-027 | code_groups / saveCodeGroup — 코드그룹 수정이 해당 그룹의 상세코드 값을 임의로 변경하지 않는다. |
| REQ-028 | detail_codes / saveDetailCode — 코드그룹을 선택하면 해당 그룹의 상세코드만 조회된다. |
| REQ-029 | detail_codes / saveDetailCode — 상세코드의 명시 필드를 저장하고 재조회할 수 있다. |
| REQ-030 | detail_codes / saveDetailCode — 정렬순서를 변경하면 화면 선택값이 같은 순서로 제공된다. |
| REQ-031 | detail_codes / saveDetailCode — 상위코드를 지정한 코드는 계층관계로 식별할 수 있다. |
| REQ-032 | React/TypeScript/Vite 번들 선택은 frontend/package.json의 빌드 의존성 계약이며 REST 입출력이 아니다. |
| REQ-033 | Java 17/Spring Boot 선택은 backend/pom.xml과 런타임의 컴파일 계약이다. |
| REQ-034 | PostgreSQL 선택은 data-model.md의 매핑·DB 접속 대상이지 신규 업무 API가 아니다. |
| REQ-035 | Docker Compose 실행 방식은 infra/docker-compose.yml의 서비스 연결 책임이다. |
| REQ-036 | 동기 blocking MyBatis는 plan.md marker의 실행 결정으로 모든 요청 처리 내부에 적용된다. |
| REQ-037 | BOM은 frontend/package.json/backend/pom.xml/DB 이미지 버전 검증으로 충족한다. |
| REQ-038 | backend 디렉터리 산출 여부는 저장소 파일 검사 대상이다. |
| REQ-039 | frontend 디렉터리 산출 여부는 저장소 파일 검사 대상이다. |
| REQ-040 | Compose 파일 위치는 infra/docker-compose.yml 파일 존재와 config 검사로 확인한다. |
| REQ-041 | getHealth 정상 결과를 반환한다. |
| REQ-042 | 레퍼런스의 sidebar/header/밀도/토큰/반응형은 ui-design.md 공통 시각 설계이며 API를 만들지 않는다. |
| REQ-043 | spec.md 기술스택 계약에서 백엔드·프론트엔드 범위를 문서화한다. |
| REQ-044 | plan.md Project Structure와 생성 순서가 소유하는 구현 배치 계약이다. |
| REQ-045 | tasks.md Setup/Foundational/US 단계가 backend/frontend/infra 작업을 배정한다. |
| REQ-046 | ui-design.md 레퍼런스 구현 검수 기준은 화면 공통 스타일 책임이다. |
| REQ-047 | plan.md의 exact marker JSON 보존은 문서 정본 검증 대상이다. |
| REQ-048 | data-model.md Entity Registry와 tasks.md migration 작업이 스키마 기준을 충족한다. |
| REQ-049 | quickstart.md API/UI 동시 smoke 절차와 seed fixture로 검증한다. |
| REQ-050 | 각 build context의 .dockerignore 제외 집합을 infra 검사에서 확인한다. |

## 주요 결정과 미해결 위험

명시 stack·BOM·required_outputs는 아래 공통 참조를 따른다. API wire shape를 재정의하지 않고 contracts/openapi.yaml을 참조한다. organization snapshot의 현재성·동시성, 권한 후보 API의 read scope·계층 완전성, 역할 승인자 신원은 OQ 해결 전 운영 준비가 아니다. migration 자동 실행 도구는 Flyway를 기술 제안하며 버전과 잠금·충돌 처리는 OQ-TECH-001이다.

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

## 요구 회계

| canonical_id | status | evidence |
| --- | --- | --- |
| REQ-001 | covered | 계층별 소유 책임 · users · searchUsers/updateUserSettings · 사용자 조건 검색 |
| REQ-002 | covered | 계층별 소유 책임 · users · searchUsers/updateUserSettings · 사용자 인사정보 목록 표시 |
| REQ-003 | covered | 계층별 소유 책임 · users · searchUsers/updateUserSettings · 사용여부·업무 역할 변경 유지 |
| REQ-004 | covered | 계층별 소유 책임 · users · searchUsers/updateUserSettings · 원천 인사정보 변경 거부 |
| REQ-005 | covered | 계층별 소유 책임 · organizations · getOrganizationRelations/saveOrganizationRelation · 조직 현재 관계 조회 |
| REQ-006 | covered | 계층별 소유 책임 · organizations · getOrganizationRelations/saveOrganizationRelation · 상위조직 적용기간 저장 |
| REQ-007 | covered | 계층별 소유 책임 · organizations · getOrganizationRelations/saveOrganizationRelation · 변경 전 조직관계 이력 조회 |
| REQ-008 | data-constraint | 계층별 소유 책임 · organizations · getOrganizationRelations/saveOrganizationRelation · 조직변경의 사용자정보 비간섭 |
| REQ-009 | covered | 계층별 소유 책임 · roles · listRoles/saveRole · 역할 정의 저장·재조회 |
| REQ-010 | covered | 계층별 소유 책임 · roles · listRoles/saveRole · 역할 식별정보 구분 |
| REQ-011 | data-constraint | 계층별 소유 책임 · roles · listRoles/saveRole · 역할 정의와 부여내역 분리 |
| REQ-012 | covered | 계층별 소유 책임 · user_role_assignments · listUserRoleAssignments/saveUserRoleAssignment · 다중 역할·상이한 기간 부여 |
| REQ-013 | covered | 계층별 소유 책임 · user_role_assignments · listUserRoleAssignments/saveUserRoleAssignment · 부여·변경·회수 승인자 기록 |
| REQ-014 | covered | 계층별 소유 책임 · user_role_assignments · listUserRoleAssignments/saveUserRoleAssignment · 역할 발생 출처 구분 조회 |
| REQ-015 | covered | 계층별 소유 책임 · user_role_assignments · listUserRoleAssignments/saveUserRoleAssignment · 종료된 역할 현재 적용 제외 |
| REQ-016 | covered | 계층별 소유 책임 · menu_permissions · listMenuPermissions/saveMenuPermission · 메뉴 접근 허용 설정 저장 |
| REQ-017 | screen-only | 계층별 소유 책임 · menu_permissions · listMenuPermissions/saveMenuPermission · 허용되지 않은 메뉴 숨김 |
| REQ-018 | covered | 계층별 소유 책임 · menu_permissions · listMenuPermissions/saveMenuPermission · 비허용 URL 서버 차단 |
| REQ-019 | covered | 계층별 소유 책임 · menus · getMenuStructure/saveMenuStructure · 부모메뉴·계층 저장 |
| REQ-020 | covered | 계층별 소유 책임 · menus · getMenuStructure/saveMenuStructure · 동일 계층 표시순서 적용 |
| REQ-021 | data-constraint | 계층별 소유 책임 · menus · getMenuStructure/saveMenuStructure · 다른 부모 아래 순서 보존 |
| REQ-022 | covered | 계층별 소유 책임 · menu_execution_info · listMenuExecutionInfo/saveMenuExecutionInfo · 메뉴 실행정보 저장 |
| REQ-023 | covered | 계층별 소유 책임 · menu_execution_info · listMenuExecutionInfo/saveMenuExecutionInfo · 사용 가능한 메뉴 화면 실행 |
| REQ-024 | data-constraint | 계층별 소유 책임 · menu_execution_info · listMenuExecutionInfo/saveMenuExecutionInfo · 실행정보 변경의 권한 비간섭 |
| REQ-025 | covered | 계층별 소유 책임 · code_groups · listCodeGroups/saveCodeGroup · 코드그룹 정의 저장 |
| REQ-026 | covered | 계층별 소유 책임 · code_groups · listCodeGroups/saveCodeGroup · 그룹ID별 독립 조회 |
| REQ-027 | data-constraint | 계층별 소유 책임 · code_groups · listCodeGroups/saveCodeGroup · 그룹 수정의 상세코드 비간섭 |
| REQ-028 | covered | 계층별 소유 책임 · detail_codes · listDetailCodes/saveDetailCode · 그룹별 상세코드 조회 |
| REQ-029 | covered | 계층별 소유 책임 · detail_codes · listDetailCodes/saveDetailCode · 상세코드 필드 저장 |
| REQ-030 | covered | 계층별 소유 책임 · detail_codes · listDetailCodes/saveDetailCode · 선택값 정렬순서 반영 |
| REQ-031 | covered | 계층별 소유 책임 · detail_codes · listDetailCodes/saveDetailCode · 상위코드 계층 식별 |
| REQ-032 | non-functional | 계층별 소유 책임 · React/TypeScript/Vite 번들 선택은 frontend/package.json의 빌드 의존성 계약이며 REST 입출력이 아니다. |
| REQ-033 | non-functional | 계층별 소유 책임 · Java 17/Spring Boot 선택은 backend/pom.xml과 런타임의 컴파일 계약이다. |
| REQ-034 | non-functional | 계층별 소유 책임 · PostgreSQL 선택은 data-model.md의 매핑·DB 접속 대상이지 신규 업무 API가 아니다. |
| REQ-035 | non-functional | 계층별 소유 책임 · Docker Compose 실행 방식은 infra/docker-compose.yml의 서비스 연결 책임이다. |
| REQ-036 | non-functional | 계층별 소유 책임 · 동기 blocking MyBatis는 plan.md marker의 실행 결정으로 모든 요청 처리 내부에 적용된다. |
| REQ-037 | non-functional | 계층별 소유 책임 · BOM은 frontend/package.json/backend/pom.xml/DB 이미지 버전 검증으로 충족한다. |
| REQ-038 | non-functional | 계층별 소유 책임 · backend 디렉터리 산출 여부는 저장소 파일 검사 대상이다. |
| REQ-039 | non-functional | 계층별 소유 책임 · frontend 디렉터리 산출 여부는 저장소 파일 검사 대상이다. |
| REQ-040 | non-functional | 계층별 소유 책임 · Compose 파일 위치는 infra/docker-compose.yml 파일 존재와 config 검사로 확인한다. |
| REQ-041 | covered | 계층별 소유 책임 · getHealth /api/health |
| REQ-042 | non-functional | 계층별 소유 책임 · 레퍼런스의 sidebar/header/밀도/토큰/반응형은 ui-design.md 공통 시각 설계이며 API를 만들지 않는다. |
| REQ-043 | non-functional | 계층별 소유 책임 · spec.md 기술스택 계약에서 백엔드·프론트엔드 범위를 문서화한다. |
| REQ-044 | non-functional | 계층별 소유 책임 · plan.md Project Structure와 생성 순서가 소유하는 구현 배치 계약이다. |
| REQ-045 | non-functional | 계층별 소유 책임 · tasks.md Setup/Foundational/US 단계가 backend/frontend/infra 작업을 배정한다. |
| REQ-046 | non-functional | 계층별 소유 책임 · ui-design.md 레퍼런스 구현 검수 기준은 화면 공통 스타일 책임이다. |
| REQ-047 | non-functional | 계층별 소유 책임 · plan.md의 exact marker JSON 보존은 문서 정본 검증 대상이다. |
| REQ-048 | data-constraint | 계층별 소유 책임 · data-model.md Entity Registry와 tasks.md migration 작업이 스키마 기준을 충족한다. |
| REQ-049 | non-functional | 계층별 소유 책임 · quickstart.md API/UI 동시 smoke 절차와 seed fixture로 검증한다. |
| REQ-050 | non-functional | 계층별 소유 책임 · 각 build context의 .dockerignore 제외 집합을 infra 검사에서 확인한다. |
