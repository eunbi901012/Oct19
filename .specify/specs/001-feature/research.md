# Phase 0 조사·결정 — oct19

## 결정 요약

저장소의 SPEC-REQUEST.md, 세 템플릿, 미작성 constitution을 읽고 원장과 문서를 설계했다. 외부 제품 조사나 서비스 동작 검증을 실행한 것으로 주장하지 않는다.

| 결정 | 선택·근거 | 대안·기각 이유 | canonical_id |
| --- | --- | --- | --- |
| Frontend | React 18.x/TypeScript/Vite 5.x/Node 20.x 명시 계약 | Angular/Vue/Next.js는 요청 stack이 아님 | REQ-032, REQ-037, REQ-039 |
| Backend | Java 17/Spring Boot 3.3.x | 다른 언어·framework는 계약 위반 | REQ-033, REQ-037, REQ-038 |
| 실행·영속성 | resolved imperative/mybatis/blocking; plan exact marker가 정본 | JPA/spring-jdbc는 blocking_priority의 후순위이며 이번 선택 아님. R2DBC/reactive는 execution model 불일치 | REQ-036, REQ-047 |
| DB | PostgreSQL 16.x·versioned migration 제안 | 인메모리 대체로 persistence 검증을 생략할 수 없음 | REQ-034, REQ-037, REQ-048 |
| 배포 | Compose와 backend/frontend 두 build context 제안 | 단일 frontend-only 실행은 필수 backend 산출물 누락 | REQ-035, REQ-040, REQ-050 |
| API | 도메인 리소스별 GET/PUT; 동일 operation에 복수 요구 연결 | 요구 ID 기반 숫자 endpoint나 요구당 API는 사용 안 함 | REQ-001, REQ-016, REQ-029 |
| 메뉴·디자인 | 아홉 여정·3단계 IA·도메인별 다른 레이아웃 제안 | dashboard card 언급만으로 새 대시보드를 만들지 않음 | REQ-042, REQ-046 |
| 검증 | 원장 근거·구조 스키마 검사와 후속 runtime smoke 구분 | 문서 검증 성공을 실제 Compose/DB/browser 성공으로 대체하지 않음 | REQ-041, REQ-049 |
| 숨김 기술 식별자 | users.user_id는 선택 응답에서 전달. 신규 역할 부여 assignment_id와 최초 메뉴 설정 permission_id는 frontend 내부 UUID를 생성·재시도 보존하는 기술 제안 | 식별자를 추가 사용자 입력으로 요구하지 않는다. 기존 행 변경·회수는 응답 식별자를 재사용하며 PK rename은 제공하지 않는다 | REQ-003, REQ-012, REQ-013, REQ-016 |
| projection·선택 대상 표현 | business_roles/is_current는 계산 결과이며 직접 저장하지 않는다. 메뉴 설정의 미선택 대상은 입력 생략/null, DB NULL, 응답 null로 표현한다 | 모든 대상에 가짜 문자열을 채우지 않는다. 복합 대상의 허용 조합·우선순위·동일성 키는 OQ-AUTH-001 승인 전 확정하지 않는다 | REQ-003, REQ-015, REQ-016 |

## 정책 판단별 조사 결과

| canonical_id | 확정 근거 | 해결된 경계 / 미해결 |
| --- | --- | --- |
| REQ-001 | SPEC-REQUEST.md:15 · 사용자 조건 검색 | 교번·성명·소속·직급·재직상태·역할·사용여부 중 하나 이상의 조건으로 검색하면 조건에 맞는 사용자만 표시된다.; 원문 이상의 정책은 승인 대기 |
| REQ-002 | SPEC-REQUEST.md:16 · 사용자 인사정보 목록 표시 | 사용자 목록에 보직·퇴직일자·최종 동기화일시를 포함한 명시 항목이 표시된다.; 원문 이상의 정책은 승인 대기 |
| REQ-003 | SPEC-REQUEST.md:17 · 사용여부·업무 역할 변경 유지 | 시스템 사용여부 또는 업무 역할을 변경하고 다시 조회하면 변경값이 유지된다.; 원문 이상의 정책은 승인 대기 |
| REQ-004 | SPEC-REQUEST.md:18 · 원천 인사정보 변경 거부 | KORUS 원천 인사정보는 화면과 서버 처리에서 변경할 수 없다.; 원문 이상의 정책은 승인 대기 |
| REQ-005 | SPEC-REQUEST.md:22 · 조직 현재 관계 조회 | 조직코드로 조회하면 해당 조직의 조직구분과 현재 상위·하위 관계가 표시된다.; 원문 이상의 정책은 승인 대기 |
| REQ-006 | SPEC-REQUEST.md:23 · 상위조직 적용기간 저장 | 상위조직 변경 시 적용 시작일·종료일과 변경된 상위조직이 함께 저장된다.; 원문 이상의 정책은 승인 대기 |
| REQ-007 | SPEC-REQUEST.md:24 · 변경 전 조직관계 이력 조회 | 변경 이후에도 변경 전 조직관계와 적용기간을 이력으로 조회할 수 있다.; 원문 이상의 정책은 승인 대기 |
| REQ-008 | SPEC-REQUEST.md:25 · 조직변경의 사용자정보 비간섭 | 조직구조 변경이 사용자의 소속·보직 또는 역할정보를 직접 변경하지 않는다.; 원문 이상의 정책은 승인 대기 |
| REQ-009 | SPEC-REQUEST.md:29 · 역할 정의 저장·재조회 | 역할별 목적·부여 기준·데이터 범위 기본값을 저장하고 재조회할 수 있다.; 원문 이상의 정책은 승인 대기 |
| REQ-010 | SPEC-REQUEST.md:30 · 역할 식별정보 구분 | 정의된 역할은 서로 다른 역할 식별정보로 구분된다.; 원문 이상의 정책은 승인 대기 |
| REQ-011 | SPEC-REQUEST.md:31 · 역할 정의와 부여내역 분리 | 역할 정의 변경만으로 특정 사용자의 역할 부여내역이 직접 변경되지 않는다.; 원문 이상의 정책은 승인 대기 |
| REQ-012 | SPEC-REQUEST.md:35 · 다중 역할·상이한 기간 부여 | 한 사용자에게 둘 이상의 역할을 서로 다른 유효기간으로 부여할 수 있다.; 원문 이상의 정책은 승인 대기 |
| REQ-013 | SPEC-REQUEST.md:36 · 부여·변경·회수 승인자 기록 | 역할을 부여·변경·회수하면 대상 사용자·역할·유효기간·승인자가 함께 저장된다.; 원문 이상의 정책은 승인 대기 |
| REQ-014 | SPEC-REQUEST.md:37 · 역할 발생 출처 구분 조회 | 보직 기반 역할과 수동 역할을 조회 결과에서 구분할 수 있다.; 원문 이상의 정책은 승인 대기 |
| REQ-015 | SPEC-REQUEST.md:38 · 종료된 역할 현재 적용 제외 | 유효기간이 종료된 역할은 현재 적용 역할로 판정되지 않는다.; 원문 이상의 정책은 승인 대기 |
| REQ-016 | SPEC-REQUEST.md:43 · 메뉴 접근 허용 설정 저장 | 역할·조직·사용자와 메뉴를 선택해 접근 허용 여부를 저장하고 재조회할 수 있다.; 원문 이상의 정책은 승인 대기 |
| REQ-017 | SPEC-REQUEST.md:44 · 허용되지 않은 메뉴 숨김 | 접근이 허용되지 않은 메뉴는 사용자 메뉴에 표시되지 않는다.; 원문 이상의 정책은 승인 대기 |
| REQ-018 | SPEC-REQUEST.md:45 · 비허용 URL 서버 차단 | 접근이 허용되지 않은 사용자가 URL로 직접 요청해도 서버에서 차단된다.; 원문 이상의 정책은 승인 대기 |
| REQ-019 | SPEC-REQUEST.md:49 · 부모메뉴·계층 저장 | 메뉴별 부모메뉴와 계층단계를 저장하고 계층구조로 재조회할 수 있다.; 원문 이상의 정책은 승인 대기 |
| REQ-020 | SPEC-REQUEST.md:50 · 동일 계층 표시순서 적용 | 동일 계층의 표시순서를 변경하면 사용자 메뉴에 변경된 순서가 적용된다.; 원문 이상의 정책은 승인 대기 |
| REQ-021 | SPEC-REQUEST.md:51 · 다른 부모 아래 순서 보존 | 한 계층의 순서 변경이 다른 부모메뉴 아래 순서를 변경하지 않는다.; 원문 이상의 정책은 승인 대기 |
| REQ-022 | SPEC-REQUEST.md:55 · 메뉴 실행정보 저장 | 메뉴의 실행정보를 저장하고 동일한 값으로 재조회할 수 있다.; 원문 이상의 정책은 승인 대기 |
| REQ-023 | SPEC-REQUEST.md:56 · 사용 가능한 메뉴 화면 실행 | 사용 가능한 메뉴를 선택하면 연결된 화면ID와 URL의 화면이 실행된다.; 원문 이상의 정책은 승인 대기 |
| REQ-024 | SPEC-REQUEST.md:57 · 실행정보 변경의 권한 비간섭 | 메뉴 실행정보 변경이 메뉴 권한 설정을 변경하지 않는다.; 원문 이상의 정책은 승인 대기 |
| REQ-025 | SPEC-REQUEST.md:61 · 코드그룹 정의 저장 | 그룹ID·명칭·설명·관리부서를 저장하고 재조회할 수 있다.; 원문 이상의 정책은 승인 대기 |
| REQ-026 | SPEC-REQUEST.md:62 · 그룹ID별 독립 조회 | 서로 다른 그룹ID의 코드그룹이 독립적으로 조회된다.; 원문 이상의 정책은 승인 대기 |
| REQ-027 | SPEC-REQUEST.md:63 · 그룹 수정의 상세코드 비간섭 | 코드그룹 수정이 해당 그룹의 상세코드 값을 임의로 변경하지 않는다.; 원문 이상의 정책은 승인 대기 |
| REQ-028 | SPEC-REQUEST.md:67 · 그룹별 상세코드 조회 | 코드그룹을 선택하면 해당 그룹의 상세코드만 조회된다.; 원문 이상의 정책은 승인 대기 |
| REQ-029 | SPEC-REQUEST.md:68 · 상세코드 필드 저장 | 상세코드의 명시 필드를 저장하고 재조회할 수 있다.; 원문 이상의 정책은 승인 대기 |
| REQ-030 | SPEC-REQUEST.md:69 · 선택값 정렬순서 반영 | 정렬순서를 변경하면 화면 선택값이 같은 순서로 제공된다.; 원문 이상의 정책은 승인 대기 |
| REQ-031 | SPEC-REQUEST.md:70 · 상위코드 계층 식별 | 상위코드를 지정한 코드는 계층관계로 식별할 수 있다.; 원문 이상의 정책은 승인 대기 |
| REQ-032 | SPEC-REQUEST.md:78 · Frontend 기술스택 | - Frontend: React + TypeScript + Vite; 원문 이상의 정책은 승인 대기 |
| REQ-033 | SPEC-REQUEST.md:79 · Backend 기술스택 | - Backend: Java 17 + Spring Boot; 원문 이상의 정책은 승인 대기 |
| REQ-034 | SPEC-REQUEST.md:80 · Database 기술스택 | - Database: PostgreSQL; 원문 이상의 정책은 승인 대기 |
| REQ-035 | SPEC-REQUEST.md:81 · Infrastructure 기술스택 | - Infrastructure: Docker Compose; 원문 이상의 정책은 승인 대기 |
| REQ-036 | SPEC-REQUEST.md:89 · 동기·blocking MyBatis 실행·영속성 | imperative 실행, mybatis 영속성, blocking 접근을 사용한다. decision_status=resolved이며 conflicts는 비어 있다.; 원문 이상의 정책은 승인 대기 |
| REQ-037 | SPEC-REQUEST.md:104 · Version BOM 호환성 | ### Version BOM<br>- spring_boot: 3.3.x<br>- node: 20.x<br>- vite: 5.x<br>- react: 18.x<br>- postgres: 16.x; 원문 이상의 정책은 승인 대기 |
| REQ-038 | SPEC-REQUEST.md:112 · backend_dir 필수 산출물 | - backend_dir: backend; 원문 이상의 정책은 승인 대기 |
| REQ-039 | SPEC-REQUEST.md:113 · frontend_dir 필수 산출물 | - frontend_dir: frontend; 원문 이상의 정책은 승인 대기 |
| REQ-040 | SPEC-REQUEST.md:114 · compose_file 필수 산출물 | - compose_file: infra/docker-compose.yml; 원문 이상의 정책은 승인 대기 |
| REQ-041 | SPEC-REQUEST.md:115 · health_endpoint 필수 산출물 | - health_endpoint: /api/health; 원문 이상의 정책은 승인 대기 |
| REQ-042 | SPEC-REQUEST.md:118 · 레퍼런스 UI/UX 적용 | - 적용 방식: 레퍼런스는 복제 대상이 아니라 UI/UX 구현 계약이다. sidebar, header, dashboard card, table/form density, spacing, color token, responsive behavior를 대상 도메인에 맞게 적용해야 한다.; 원문 이상의 정책은 승인 대기 |
| REQ-043 | SPEC-REQUEST.md:121 · 명세의 스택·산출물·범위 명시 | - spec.md에는 기술스택 고정, 필수 산출물, 백엔드/프론트엔드 범위를 명시한다.; 원문 이상의 정책은 승인 대기 |
| REQ-044 | SPEC-REQUEST.md:122 · 계획의 계층 구조·생성순서 명시 | - plan.md에는 backend/frontend/database/infra 구조와 생성 순서를 명시한다.; 원문 이상의 정책은 승인 대기 |
| REQ-045 | SPEC-REQUEST.md:123 · 작업의 계층별 구현 포함 | - tasks.md에는 backend/, frontend/, infra/ 구현 작업을 각각 포함한다.; 원문 이상의 정책은 승인 대기 |
| REQ-046 | SPEC-REQUEST.md:124 · UI 레퍼런스 설계 기준 명시 | - ui-design.md에는 레퍼런스 디자인의 레이아웃, 컴포넌트, 시각 규칙, 검증 기준을 명시한다.; 원문 이상의 정책은 승인 대기 |
| REQ-047 | SPEC-REQUEST.md:125 · plan 실행·영속성 정본 보존 | - plan.md는 실행·영속성 계약의 exact marker+JSON block을 보존하고, 생성된 9개 문서의 semantic SSOT(의미상 정본)로 확정한다.; 원문 이상의 정책은 승인 대기 |
| REQ-048 | SPEC-REQUEST.md:130 · DB 스키마·migration 기준 | - DB 스키마 또는 migration 기준이 없으면 신규생성 구현은 불완전한 것으로 간주한다.; 원문 이상의 정책은 승인 대기 |
| REQ-049 | SPEC-REQUEST.md:131 · Backend·Frontend 공동 검증 | - preview 또는 quickstart 검증에서 backend API와 frontend 화면을 함께 확인한다.; 원문 이상의 정책은 승인 대기 |
| REQ-050 | SPEC-REQUEST.md:132 · build context 제외 파일 | - compose 의 각 build context 디렉터리(예: context: .. 는 저장소 루트)에 .dockerignore 를 두고 node_modules, .git, dist, build, .next, coverage 를 제외한다.; 원문 이상의 정책은 승인 대기 |

## Phase 0에서 해결한 기술 경계

- 명확화 추정 응답이 아니라 명시 기술스택 계약이 우선한다.
- 실행·영속성은 resolved이므로 MyBatis 동기 구현으로 일관시킨다.
- source_id는 AC 번호가 아닌 선언된 CMN-FR-*이며 AC는 child proof로 보존한다.
- 명확화가 unconfirmed인 경우 실제 KORUS 제외·중앙 관리자·즉시 적용·권한 우선순위·기간 중복 금지·삭제 제외를 확정하지 않는다.

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
| REQ-001 | covered | 정책 판단별 조사 결과 · users · searchUsers/updateUserSettings · 사용자 조건 검색 |
| REQ-002 | covered | 정책 판단별 조사 결과 · users · searchUsers/updateUserSettings · 사용자 인사정보 목록 표시 |
| REQ-003 | covered | 정책 판단별 조사 결과 · users · searchUsers/updateUserSettings · 사용여부·업무 역할 변경 유지 |
| REQ-004 | covered | 정책 판단별 조사 결과 · users · searchUsers/updateUserSettings · 원천 인사정보 변경 거부 |
| REQ-005 | covered | 정책 판단별 조사 결과 · organizations · getOrganizationRelations/saveOrganizationRelation · 조직 현재 관계 조회 |
| REQ-006 | covered | 정책 판단별 조사 결과 · organizations · getOrganizationRelations/saveOrganizationRelation · 상위조직 적용기간 저장 |
| REQ-007 | covered | 정책 판단별 조사 결과 · organizations · getOrganizationRelations/saveOrganizationRelation · 변경 전 조직관계 이력 조회 |
| REQ-008 | data-constraint | 정책 판단별 조사 결과 · organizations · getOrganizationRelations/saveOrganizationRelation · 조직변경의 사용자정보 비간섭 |
| REQ-009 | covered | 정책 판단별 조사 결과 · roles · listRoles/saveRole · 역할 정의 저장·재조회 |
| REQ-010 | covered | 정책 판단별 조사 결과 · roles · listRoles/saveRole · 역할 식별정보 구분 |
| REQ-011 | data-constraint | 정책 판단별 조사 결과 · roles · listRoles/saveRole · 역할 정의와 부여내역 분리 |
| REQ-012 | covered | 정책 판단별 조사 결과 · user_role_assignments · listUserRoleAssignments/saveUserRoleAssignment · 다중 역할·상이한 기간 부여 |
| REQ-013 | covered | 정책 판단별 조사 결과 · user_role_assignments · listUserRoleAssignments/saveUserRoleAssignment · 부여·변경·회수 승인자 기록 |
| REQ-014 | covered | 정책 판단별 조사 결과 · user_role_assignments · listUserRoleAssignments/saveUserRoleAssignment · 역할 발생 출처 구분 조회 |
| REQ-015 | covered | 정책 판단별 조사 결과 · user_role_assignments · listUserRoleAssignments/saveUserRoleAssignment · 종료된 역할 현재 적용 제외 |
| REQ-016 | covered | 정책 판단별 조사 결과 · menu_permissions · listMenuPermissions/saveMenuPermission · 메뉴 접근 허용 설정 저장 |
| REQ-017 | screen-only | 정책 판단별 조사 결과 · menu_permissions · listMenuPermissions/saveMenuPermission · 허용되지 않은 메뉴 숨김 |
| REQ-018 | covered | 정책 판단별 조사 결과 · menu_permissions · listMenuPermissions/saveMenuPermission · 비허용 URL 서버 차단 |
| REQ-019 | covered | 정책 판단별 조사 결과 · menus · getMenuStructure/saveMenuStructure · 부모메뉴·계층 저장 |
| REQ-020 | covered | 정책 판단별 조사 결과 · menus · getMenuStructure/saveMenuStructure · 동일 계층 표시순서 적용 |
| REQ-021 | data-constraint | 정책 판단별 조사 결과 · menus · getMenuStructure/saveMenuStructure · 다른 부모 아래 순서 보존 |
| REQ-022 | covered | 정책 판단별 조사 결과 · menu_execution_info · listMenuExecutionInfo/saveMenuExecutionInfo · 메뉴 실행정보 저장 |
| REQ-023 | covered | 정책 판단별 조사 결과 · menu_execution_info · listMenuExecutionInfo/saveMenuExecutionInfo · 사용 가능한 메뉴 화면 실행 |
| REQ-024 | data-constraint | 정책 판단별 조사 결과 · menu_execution_info · listMenuExecutionInfo/saveMenuExecutionInfo · 실행정보 변경의 권한 비간섭 |
| REQ-025 | covered | 정책 판단별 조사 결과 · code_groups · listCodeGroups/saveCodeGroup · 코드그룹 정의 저장 |
| REQ-026 | covered | 정책 판단별 조사 결과 · code_groups · listCodeGroups/saveCodeGroup · 그룹ID별 독립 조회 |
| REQ-027 | data-constraint | 정책 판단별 조사 결과 · code_groups · listCodeGroups/saveCodeGroup · 그룹 수정의 상세코드 비간섭 |
| REQ-028 | covered | 정책 판단별 조사 결과 · detail_codes · listDetailCodes/saveDetailCode · 그룹별 상세코드 조회 |
| REQ-029 | covered | 정책 판단별 조사 결과 · detail_codes · listDetailCodes/saveDetailCode · 상세코드 필드 저장 |
| REQ-030 | covered | 정책 판단별 조사 결과 · detail_codes · listDetailCodes/saveDetailCode · 선택값 정렬순서 반영 |
| REQ-031 | covered | 정책 판단별 조사 결과 · detail_codes · listDetailCodes/saveDetailCode · 상위코드 계층 식별 |
| REQ-032 | non-functional | 정책 판단별 조사 결과 · React/TypeScript/Vite 번들 선택은 frontend/package.json의 빌드 의존성 계약이며 REST 입출력이 아니다. |
| REQ-033 | non-functional | 정책 판단별 조사 결과 · Java 17/Spring Boot 선택은 backend/pom.xml과 런타임의 컴파일 계약이다. |
| REQ-034 | non-functional | 정책 판단별 조사 결과 · PostgreSQL 선택은 data-model.md의 매핑·DB 접속 대상이지 신규 업무 API가 아니다. |
| REQ-035 | non-functional | 정책 판단별 조사 결과 · Docker Compose 실행 방식은 infra/docker-compose.yml의 서비스 연결 책임이다. |
| REQ-036 | non-functional | 정책 판단별 조사 결과 · 동기 blocking MyBatis는 plan.md marker의 실행 결정으로 모든 요청 처리 내부에 적용된다. |
| REQ-037 | non-functional | 정책 판단별 조사 결과 · BOM은 frontend/package.json/backend/pom.xml/DB 이미지 버전 검증으로 충족한다. |
| REQ-038 | non-functional | 정책 판단별 조사 결과 · backend 디렉터리 산출 여부는 저장소 파일 검사 대상이다. |
| REQ-039 | non-functional | 정책 판단별 조사 결과 · frontend 디렉터리 산출 여부는 저장소 파일 검사 대상이다. |
| REQ-040 | non-functional | 정책 판단별 조사 결과 · Compose 파일 위치는 infra/docker-compose.yml 파일 존재와 config 검사로 확인한다. |
| REQ-041 | covered | 정책 판단별 조사 결과 · getHealth /api/health |
| REQ-042 | non-functional | 정책 판단별 조사 결과 · 레퍼런스의 sidebar/header/밀도/토큰/반응형은 ui-design.md 공통 시각 설계이며 API를 만들지 않는다. |
| REQ-043 | non-functional | 정책 판단별 조사 결과 · spec.md 기술스택 계약에서 백엔드·프론트엔드 범위를 문서화한다. |
| REQ-044 | non-functional | 정책 판단별 조사 결과 · plan.md Project Structure와 생성 순서가 소유하는 구현 배치 계약이다. |
| REQ-045 | non-functional | 정책 판단별 조사 결과 · tasks.md Setup/Foundational/US 단계가 backend/frontend/infra 작업을 배정한다. |
| REQ-046 | non-functional | 정책 판단별 조사 결과 · ui-design.md 레퍼런스 구현 검수 기준은 화면 공통 스타일 책임이다. |
| REQ-047 | non-functional | 정책 판단별 조사 결과 · plan.md의 exact marker JSON 보존은 문서 정본 검증 대상이다. |
| REQ-048 | data-constraint | 정책 판단별 조사 결과 · data-model.md Entity Registry와 tasks.md migration 작업이 스키마 기준을 충족한다. |
| REQ-049 | non-functional | 정책 판단별 조사 결과 · quickstart.md API/UI 동시 smoke 절차와 seed fixture로 검증한다. |
| REQ-050 | non-functional | 정책 판단별 조사 결과 · 각 build context의 .dockerignore 제외 집합을 infra 검사에서 확인한다. |

## Tradeoffs

- REST/OpenAPI는 검증과 도구 연동이 명확하지만 문서와 구현 schema의 동기화가 필요하다.
