# 실행·검증 안내 — oct19

## 검증 워크플로와 실행 전제

저장소 루트에서 아래 순서로 실행한다. 현재 작업은 명세 생성이며 backend/, frontend/, infra/ 실행물을 생성하지 않았다. 아래 명령은 tasks.md가 구현해야 할 실행 인터페이스와 예상 관찰 결과다. 아직 실행하지 않았고 Compose·DB·API·브라우저 통과를 주장하지 않는다. 인증/관리 scope·승인자·기간·상세코드 미정 정책이 남으면 해당 시험은 차단으로 보고한다.

검증 환경 제안: backend 포트 8080, frontend 포트 5173, Compose DB 서비스명 database, 로컬 DB 이름·사용자 oct19. 이는 원문 제공 운영 값이 아니라 비밀이 없는 제안이며 구현 시 실제 Compose와 일치시킨다. 비밀번호·토큰은 문서나 seed에 쓰지 않는다. ACCESS_TOKEN은 승인된 인증 경로에서 주입하고 값을 출력하지 않는다. Playwright 인증 fixture도 그 경로를 사용하며 임의 로그인 API를 추가하지 않는다. 매 단계는 저장소 루트에서 시작한다.

### QS-01 · 필수 출력·build context 확인

```sh
test -d backend && test -d frontend && test -f infra/docker-compose.yml && test -f backend/.dockerignore && test -f frontend/.dockerignore
```

예상 결과: exit=0; backend_dir/frontend_dir/compose_file와 앱별 .dockerignore가 존재한다
연결: REQ-038, REQ-039, REQ-040, REQ-050.

### QS-02 · BOM·stack 계약 검사

```sh
bash infra/tests/stack-contract.test.sh
```

예상 결과: exit=0; Java 17, Spring Boot 3.3.x, Node 20.x, React 18.x, Vite 5.x, PostgreSQL 16.x 및 제외 목록 검증 통과
연결: REQ-032, REQ-033, REQ-034, REQ-035, REQ-037, REQ-050.

### QS-03 · Compose 정적 해석

```sh
docker compose -f infra/docker-compose.yml config --quiet
```

예상 결과: exit=0; backend/frontend/database build context·접속 설정 해석 성공
연결: REQ-035, REQ-040.

### QS-04 · PostgreSQL 통합·MyBatis 시험

```sh
cd backend && ./mvnw verify
```

예상 결과: exit=0; 실제 PostgreSQL mapping·transaction·원천 readonly·권한 거부·조직 이력 및 비간섭 시험 통과. 승인 미정 테스트는 pending 표시
연결: REQ-004, REQ-007, REQ-008, REQ-015, REQ-018, REQ-036, REQ-048.

### QS-05 · Frontend 단위시험·빌드

```sh
cd frontend && npm ci && npm run test -- --run && npm run build
```

예상 결과: exit=0; React/Vite production build와 UI 공통 상태 시험 통과
연결: REQ-032, REQ-039, REQ-042, REQ-046.

### QS-06 · Compose 앱·DB 실행

```sh
docker compose -f infra/docker-compose.yml up -d --build --wait
```

예상 결과: exit=0; database readiness 뒤 backend/frontend 서비스가 준비된다. 동기 MyBatis가 PostgreSQL에 연결한다
연결: REQ-034, REQ-035, REQ-036, REQ-040.

### QS-07 · 필수 health_endpoint

```sh
curl --fail --silent http://localhost:8080/api/health
```

예상 결과: HTTP 200; success=true, meta object, data.status=UP
연결: REQ-041.

### QS-08 · 합성 fixture 확인

```sh
docker compose -f infra/docker-compose.yml exec -T database psql -U oct19 -d oct19 -c 'SELECT COUNT(*) FROM users;'
```

예상 결과: 쿼리 성공·FX-US1 합성 사용자 수가 양수. seed 적재 방식과 실제 계정 값은 승인된 로컬 설정으로 맞춘다
연결: REQ-049.

### QS-09 · 사용자 검색 API smoke

```sh
curl --fail --silent --get -H "Authorization: Bearer ${ACCESS_TOKEN:?}" --data-urlencode "page=1" --data-urlencode "size=20" --data-urlencode "employee_number=${EMPLOYEE_NUMBER:?}" http://localhost:8080/api/users
```

예상 결과: 200; ApiResponse.data에 EMPLOYEE_NUMBER 환경변수로 지정한 합성 fixture 교번과 일치하는 사용자만 포함하고 보직·퇴직일자·최종 동기화일시를 포함한다. 두 환경변수는 승인된 검증 환경에서 사전 주입한다.
연결: REQ-001, REQ-002.

### QS-10 · Frontend 응답 smoke

```sh
curl --fail --silent http://localhost:5173/admin/users
```

예상 결과: HTTP 200 HTML; 화면 기능 성공은 다음 브라우저 시험에서 별도 검증한다
연결: REQ-039, REQ-049.

### QS-11 · 아홉 primary 여정 브라우저 검증

```sh
cd frontend && npm run test:e2e
```

예상 결과: exit=0; US1–US9의 primary AC·고유 화면·UI 상태·저장 재조회가 검증됨. OQ 차단은 skip/pending을 따로 보고하며 전체 통과로 세지 않는다
연결: REQ-001, REQ-003, REQ-005, REQ-006, REQ-009, REQ-012, REQ-013, REQ-014, REQ-016, REQ-019, REQ-020, REQ-022, REQ-023, REQ-025, REQ-026, REQ-028, REQ-029, REQ-030, REQ-031, REQ-049.

### QS-12 · 필수 계층 통합 실행

```sh
bash infra/tests/compose-smoke.test.sh
```

예상 결과: exit=0; migration, health, API 저장 재조회와 frontend 응답을 같은 Compose 실행에서 확인
연결: REQ-040, REQ-041, REQ-048, REQ-049.

## 행위별 수동/자동 관찰 기준

| canonical_id | 실행 여정·시험 | 관찰 결과 |
| --- | --- | --- |
| REQ-001 | US1 searchUsers / updateUserSettings | 교번·성명·소속·직급·재직상태·역할·사용여부 중 하나 이상의 조건으로 검색하면 조건에 맞는 사용자만 표시된다. |
| REQ-002 | US1 searchUsers / updateUserSettings | 사용자 목록에 보직·퇴직일자·최종 동기화일시를 포함한 명시 항목이 표시된다. |
| REQ-003 | US1 searchUsers / updateUserSettings | 시스템 사용여부 또는 업무 역할을 변경하고 다시 조회하면 변경값이 유지된다. |
| REQ-004 | US1 searchUsers / updateUserSettings | KORUS 원천 인사정보는 화면과 서버 처리에서 변경할 수 없다. |
| REQ-005 | US2 getOrganizationRelations / saveOrganizationRelation | 조직코드로 조회하면 해당 조직의 조직구분과 현재 상위·하위 관계가 표시된다. |
| REQ-006 | US2 getOrganizationRelations / saveOrganizationRelation | 상위조직 변경 시 적용 시작일·종료일과 변경된 상위조직이 함께 저장된다. |
| REQ-007 | US2 getOrganizationRelations / saveOrganizationRelation | 변경 이후에도 변경 전 조직관계와 적용기간을 이력으로 조회할 수 있다. |
| REQ-008 | US2 getOrganizationRelations / saveOrganizationRelation | 조직구조 변경이 사용자의 소속·보직 또는 역할정보를 직접 변경하지 않는다. |
| REQ-009 | US3 listRoles / saveRole | 역할별 목적·부여 기준·데이터 범위 기본값을 저장하고 재조회할 수 있다. |
| REQ-010 | US3 listRoles / saveRole | 정의된 역할은 서로 다른 역할 식별정보로 구분된다. |
| REQ-011 | US3 listRoles / saveRole | 역할 정의 변경만으로 특정 사용자의 역할 부여내역이 직접 변경되지 않는다. |
| REQ-012 | US4 listUserRoleAssignments / saveUserRoleAssignment | 한 사용자에게 둘 이상의 역할을 서로 다른 유효기간으로 부여할 수 있다. |
| REQ-013 | US4 listUserRoleAssignments / saveUserRoleAssignment | 역할을 부여·변경·회수하면 대상 사용자·역할·유효기간·승인자가 함께 저장된다. |
| REQ-014 | US4 listUserRoleAssignments / saveUserRoleAssignment | 보직 기반 역할과 수동 역할을 조회 결과에서 구분할 수 있다. |
| REQ-015 | US4 listUserRoleAssignments / saveUserRoleAssignment | 유효기간이 종료된 역할은 현재 적용 역할로 판정되지 않는다. |
| REQ-016 | US5 listMenuPermissions / saveMenuPermission | 역할·조직·사용자와 메뉴를 선택해 접근 허용 여부를 저장하고 재조회할 수 있다. |
| REQ-017 | US5 listMenuPermissions / saveMenuPermission | 접근이 허용되지 않은 메뉴는 사용자 메뉴에 표시되지 않는다. |
| REQ-018 | US5 listMenuPermissions / saveMenuPermission | 접근이 허용되지 않은 사용자가 URL로 직접 요청해도 서버에서 차단된다. |
| REQ-019 | US6 getMenuStructure / saveMenuStructure | 메뉴별 부모메뉴와 계층단계를 저장하고 계층구조로 재조회할 수 있다. |
| REQ-020 | US6 getMenuStructure / saveMenuStructure | 동일 계층의 표시순서를 변경하면 사용자 메뉴에 변경된 순서가 적용된다. |
| REQ-021 | US6 getMenuStructure / saveMenuStructure | 한 계층의 순서 변경이 다른 부모메뉴 아래 순서를 변경하지 않는다. |
| REQ-022 | US7 listMenuExecutionInfo / saveMenuExecutionInfo | 메뉴의 실행정보를 저장하고 동일한 값으로 재조회할 수 있다. |
| REQ-023 | US7 listMenuExecutionInfo / saveMenuExecutionInfo | 사용 가능한 메뉴를 선택하면 연결된 화면ID와 URL의 화면이 실행된다. |
| REQ-024 | US7 listMenuExecutionInfo / saveMenuExecutionInfo | 메뉴 실행정보 변경이 메뉴 권한 설정을 변경하지 않는다. |
| REQ-025 | US8 listCodeGroups / saveCodeGroup | 그룹ID·명칭·설명·관리부서를 저장하고 재조회할 수 있다. |
| REQ-026 | US8 listCodeGroups / saveCodeGroup | 서로 다른 그룹ID의 코드그룹이 독립적으로 조회된다. |
| REQ-027 | US8 listCodeGroups / saveCodeGroup | 코드그룹 수정이 해당 그룹의 상세코드 값을 임의로 변경하지 않는다. |
| REQ-028 | US9 listDetailCodes / saveDetailCode | 코드그룹을 선택하면 해당 그룹의 상세코드만 조회된다. |
| REQ-029 | US9 listDetailCodes / saveDetailCode | 상세코드의 명시 필드를 저장하고 재조회할 수 있다. |
| REQ-030 | US9 listDetailCodes / saveDetailCode | 정렬순서를 변경하면 화면 선택값이 같은 순서로 제공된다. |
| REQ-031 | US9 listDetailCodes / saveDetailCode | 상위코드를 지정한 코드는 계층관계로 식별할 수 있다. |
| REQ-032 | QS-01–QS-12의 소유 계층 검사 | - Frontend: React + TypeScript + Vite |
| REQ-033 | QS-01–QS-12의 소유 계층 검사 | - Backend: Java 17 + Spring Boot |
| REQ-034 | QS-01–QS-12의 소유 계층 검사 | - Database: PostgreSQL |
| REQ-035 | QS-01–QS-12의 소유 계층 검사 | - Infrastructure: Docker Compose |
| REQ-036 | QS-01–QS-12의 소유 계층 검사 | imperative 실행, mybatis 영속성, blocking 접근을 사용한다. decision_status=resolved이며 conflicts는 비어 있다. |
| REQ-037 | QS-01–QS-12의 소유 계층 검사 | ### Version BOM<br>- spring_boot: 3.3.x<br>- node: 20.x<br>- vite: 5.x<br>- react: 18.x<br>- postgres: 16.x |
| REQ-038 | QS-01–QS-12의 소유 계층 검사 | - backend_dir: backend |
| REQ-039 | QS-01–QS-12의 소유 계층 검사 | - frontend_dir: frontend |
| REQ-040 | QS-01–QS-12의 소유 계층 검사 | - compose_file: infra/docker-compose.yml |
| REQ-041 | QS-01–QS-12의 소유 계층 검사 | - health_endpoint: /api/health |
| REQ-042 | QS-01–QS-12의 소유 계층 검사 | - 적용 방식: 레퍼런스는 복제 대상이 아니라 UI/UX 구현 계약이다. sidebar, header, dashboard card, table/form density, spacing, color token, responsive behavior를 대상 도메인에 맞게 적용해야 한다. |
| REQ-043 | QS-01–QS-12의 소유 계층 검사 | - spec.md에는 기술스택 고정, 필수 산출물, 백엔드/프론트엔드 범위를 명시한다. |
| REQ-044 | QS-01–QS-12의 소유 계층 검사 | - plan.md에는 backend/frontend/database/infra 구조와 생성 순서를 명시한다. |
| REQ-045 | QS-01–QS-12의 소유 계층 검사 | - tasks.md에는 backend/, frontend/, infra/ 구현 작업을 각각 포함한다. |
| REQ-046 | QS-01–QS-12의 소유 계층 검사 | - ui-design.md에는 레퍼런스 디자인의 레이아웃, 컴포넌트, 시각 규칙, 검증 기준을 명시한다. |
| REQ-047 | QS-01–QS-12의 소유 계층 검사 | - plan.md는 실행·영속성 계약의 exact marker+JSON block을 보존하고, 생성된 9개 문서의 semantic SSOT(의미상 정본)로 확정한다. |
| REQ-048 | QS-01–QS-12의 소유 계층 검사 | - DB 스키마 또는 migration 기준이 없으면 신규생성 구현은 불완전한 것으로 간주한다. |
| REQ-049 | QS-01–QS-12의 소유 계층 검사 | - preview 또는 quickstart 검증에서 backend API와 frontend 화면을 함께 확인한다. |
| REQ-050 | QS-01–QS-12의 소유 계층 검사 | - compose 의 각 build context 디렉터리(예: context: .. 는 저장소 루트)에 .dockerignore 를 두고 node_modules, .git, dist, build, .next, coverage 를 제외한다. |

## 정리

검증이 끝난 뒤 `docker compose -f infra/docker-compose.yml down`으로 중지할 수 있다. 이 정리 명령은 checks ledger에 넣지 않는다. 데이터를 지우는 `down -v`는 기본 절차로 제안하지 않는다.

## 검증 기록

이번 생성에서 실행한 것은 문서의 출처·회계·OpenAPI 구조 검증뿐이다. 위 runtime 명령의 실행 결과는 후속 구현 작업에서 기록한다. 값이 다른 사용자·조직 fixture와 거부 접근 fixture를 각각 써서 오검출을 방지한다.

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

## 기계 판독 검증 목록

<!-- aiops:quickstart-checks:start -->
```json
{
  "schema_version": "quickstart-checks-v1",
  "checks": [
    {
      "id": "QS-01",
      "step": "필수 출력·build context 확인",
      "command": "test -d backend && test -d frontend && test -f infra/docker-compose.yml && test -f backend/.dockerignore && test -f frontend/.dockerignore",
      "expected": "exit=0; backend_dir/frontend_dir/compose_file와 앱별 .dockerignore가 존재한다",
      "requirement_ids": [
        "REQ-038",
        "REQ-039",
        "REQ-040",
        "REQ-050"
      ]
    },
    {
      "id": "QS-02",
      "step": "BOM·stack 계약 검사",
      "command": "bash infra/tests/stack-contract.test.sh",
      "expected": "exit=0; Java 17, Spring Boot 3.3.x, Node 20.x, React 18.x, Vite 5.x, PostgreSQL 16.x 및 제외 목록 검증 통과",
      "requirement_ids": [
        "REQ-032",
        "REQ-033",
        "REQ-034",
        "REQ-035",
        "REQ-037",
        "REQ-050"
      ]
    },
    {
      "id": "QS-03",
      "step": "Compose 정적 해석",
      "command": "docker compose -f infra/docker-compose.yml config --quiet",
      "expected": "exit=0; backend/frontend/database build context·접속 설정 해석 성공",
      "requirement_ids": [
        "REQ-035",
        "REQ-040"
      ]
    },
    {
      "id": "QS-04",
      "step": "PostgreSQL 통합·MyBatis 시험",
      "command": "cd backend && ./mvnw verify",
      "expected": "exit=0; 실제 PostgreSQL mapping·transaction·원천 readonly·권한 거부·조직 이력 및 비간섭 시험 통과. 승인 미정 테스트는 pending 표시",
      "requirement_ids": [
        "REQ-004",
        "REQ-007",
        "REQ-008",
        "REQ-015",
        "REQ-018",
        "REQ-036",
        "REQ-048"
      ]
    },
    {
      "id": "QS-05",
      "step": "Frontend 단위시험·빌드",
      "command": "cd frontend && npm ci && npm run test -- --run && npm run build",
      "expected": "exit=0; React/Vite production build와 UI 공통 상태 시험 통과",
      "requirement_ids": [
        "REQ-032",
        "REQ-039",
        "REQ-042",
        "REQ-046"
      ]
    },
    {
      "id": "QS-06",
      "step": "Compose 앱·DB 실행",
      "command": "docker compose -f infra/docker-compose.yml up -d --build --wait",
      "expected": "exit=0; database readiness 뒤 backend/frontend 서비스가 준비된다. 동기 MyBatis가 PostgreSQL에 연결한다",
      "requirement_ids": [
        "REQ-034",
        "REQ-035",
        "REQ-036",
        "REQ-040"
      ]
    },
    {
      "id": "QS-07",
      "step": "필수 health_endpoint",
      "command": "curl --fail --silent http://localhost:8080/api/health",
      "expected": "HTTP 200; success=true, meta object, data.status=UP",
      "requirement_ids": [
        "REQ-041"
      ]
    },
    {
      "id": "QS-08",
      "step": "합성 fixture 확인",
      "command": "docker compose -f infra/docker-compose.yml exec -T database psql -U oct19 -d oct19 -c 'SELECT COUNT(*) FROM users;'",
      "expected": "쿼리 성공·FX-US1 합성 사용자 수가 양수. seed 적재 방식과 실제 계정 값은 승인된 로컬 설정으로 맞춘다",
      "requirement_ids": [
        "REQ-049"
      ]
    },
    {
      "id": "QS-09",
      "step": "사용자 검색 API smoke",
      "command": "curl --fail --silent --get -H \"Authorization: Bearer ${ACCESS_TOKEN:?}\" --data-urlencode \"page=1\" --data-urlencode \"size=20\" --data-urlencode \"employee_number=${EMPLOYEE_NUMBER:?}\" http://localhost:8080/api/users",
      "expected": "200; ApiResponse.data에 EMPLOYEE_NUMBER 환경변수로 지정한 합성 fixture 교번과 일치하는 사용자만 포함하고 보직·퇴직일자·최종 동기화일시를 포함한다. 두 환경변수는 승인된 검증 환경에서 사전 주입한다.",
      "requirement_ids": [
        "REQ-001",
        "REQ-002"
      ]
    },
    {
      "id": "QS-10",
      "step": "Frontend 응답 smoke",
      "command": "curl --fail --silent http://localhost:5173/admin/users",
      "expected": "HTTP 200 HTML; 화면 기능 성공은 다음 브라우저 시험에서 별도 검증한다",
      "requirement_ids": [
        "REQ-039",
        "REQ-049"
      ]
    },
    {
      "id": "QS-11",
      "step": "아홉 primary 여정 브라우저 검증",
      "command": "cd frontend && npm run test:e2e",
      "expected": "exit=0; US1–US9의 primary AC·고유 화면·UI 상태·저장 재조회가 검증됨. OQ 차단은 skip/pending을 따로 보고하며 전체 통과로 세지 않는다",
      "requirement_ids": [
        "REQ-001",
        "REQ-003",
        "REQ-005",
        "REQ-006",
        "REQ-009",
        "REQ-012",
        "REQ-013",
        "REQ-014",
        "REQ-016",
        "REQ-019",
        "REQ-020",
        "REQ-022",
        "REQ-023",
        "REQ-025",
        "REQ-026",
        "REQ-028",
        "REQ-029",
        "REQ-030",
        "REQ-031",
        "REQ-049"
      ]
    },
    {
      "id": "QS-12",
      "step": "필수 계층 통합 실행",
      "command": "bash infra/tests/compose-smoke.test.sh",
      "expected": "exit=0; migration, health, API 저장 재조회와 frontend 응답을 같은 Compose 실행에서 확인",
      "requirement_ids": [
        "REQ-040",
        "REQ-041",
        "REQ-048",
        "REQ-049"
      ]
    }
  ]
}
```
<!-- aiops:quickstart-checks:end -->

## 문서 생성 단계의 실제 검증 기록

이 기록은 위 QS-01~QS-12의 애플리케이션 실행 결과가 아니다. 생성된 파일을 다시 읽어 수행한 명세 정적 검증 결과다.

- OpenAPI 3.0.3 specification validator 통과: 12 paths, 21 operations, 124개 local $ref 해석 성공. API에 연결한 요구 32개와 x-uncovered-requirements 18개로 원장 전체를 회계 처리했다.
- 원장 50개 항목의 canonical_id 유일성, SPEC-REQUEST.md의 실제 1-based 인용 시작행·원문 quote, disposition을 확인했다. 아홉 관리 문서와 원장 Markdown의 요구 회계는 각각 50개 항목을 한 번씩 포함한다.
- User Story 9개의 spec/tasks 제목·번호, 79개 작업의 파일 경로·요구 ID·story tag·RED 선행 순서와 전체 요구 연결을 확인했다.
- 9개 화면의 고유 ASCII wireframe, 39개 action의 실제 operation/method/path, 21개 ui-primary 인수 기준 추적, 3단계 메뉴·route·화면 계약 일치를 확인했다.
- 10개 물리 entity/table와 51개 Field Contract 행의 inventory, enum $ref 재사용, 필수 DTO의 UI binding, 숨김 식별자 책임, 역할 action 값, readonly projection 및 메뉴 권한의 nullable 대상 표현을 확인했다.
- QS-01~QS-12의 본문 명령과 기계 판독 ledger 일치 및 12개 명령의 bash 문법 검사를 통과했다. 해당 명령 자체의 Compose/DB/서비스/브라우저 동작은 실행하지 않았다.
- plan.md 실행·영속성 marker+JSON이 원문 block과 정확히 같음을 확인했다. 미정 인증·승인자·기간 경계·상세코드 필드는 OQ로 남아 있으며 정책 승인이나 build-ready 판정은 아니다.

## 요구 회계

| canonical_id | status | evidence |
| --- | --- | --- |
| REQ-001 | covered | 행위별 수동/자동 관찰 기준 · users · searchUsers/updateUserSettings · 사용자 조건 검색 |
| REQ-002 | covered | 행위별 수동/자동 관찰 기준 · users · searchUsers/updateUserSettings · 사용자 인사정보 목록 표시 |
| REQ-003 | covered | 행위별 수동/자동 관찰 기준 · users · searchUsers/updateUserSettings · 사용여부·업무 역할 변경 유지 |
| REQ-004 | covered | 행위별 수동/자동 관찰 기준 · users · searchUsers/updateUserSettings · 원천 인사정보 변경 거부 |
| REQ-005 | covered | 행위별 수동/자동 관찰 기준 · organizations · getOrganizationRelations/saveOrganizationRelation · 조직 현재 관계 조회 |
| REQ-006 | covered | 행위별 수동/자동 관찰 기준 · organizations · getOrganizationRelations/saveOrganizationRelation · 상위조직 적용기간 저장 |
| REQ-007 | covered | 행위별 수동/자동 관찰 기준 · organizations · getOrganizationRelations/saveOrganizationRelation · 변경 전 조직관계 이력 조회 |
| REQ-008 | data-constraint | 행위별 수동/자동 관찰 기준 · organizations · getOrganizationRelations/saveOrganizationRelation · 조직변경의 사용자정보 비간섭 |
| REQ-009 | covered | 행위별 수동/자동 관찰 기준 · roles · listRoles/saveRole · 역할 정의 저장·재조회 |
| REQ-010 | covered | 행위별 수동/자동 관찰 기준 · roles · listRoles/saveRole · 역할 식별정보 구분 |
| REQ-011 | data-constraint | 행위별 수동/자동 관찰 기준 · roles · listRoles/saveRole · 역할 정의와 부여내역 분리 |
| REQ-012 | covered | 행위별 수동/자동 관찰 기준 · user_role_assignments · listUserRoleAssignments/saveUserRoleAssignment · 다중 역할·상이한 기간 부여 |
| REQ-013 | covered | 행위별 수동/자동 관찰 기준 · user_role_assignments · listUserRoleAssignments/saveUserRoleAssignment · 부여·변경·회수 승인자 기록 |
| REQ-014 | covered | 행위별 수동/자동 관찰 기준 · user_role_assignments · listUserRoleAssignments/saveUserRoleAssignment · 역할 발생 출처 구분 조회 |
| REQ-015 | covered | 행위별 수동/자동 관찰 기준 · user_role_assignments · listUserRoleAssignments/saveUserRoleAssignment · 종료된 역할 현재 적용 제외 |
| REQ-016 | covered | 행위별 수동/자동 관찰 기준 · menu_permissions · listMenuPermissions/saveMenuPermission · 메뉴 접근 허용 설정 저장 |
| REQ-017 | screen-only | 행위별 수동/자동 관찰 기준 · menu_permissions · listMenuPermissions/saveMenuPermission · 허용되지 않은 메뉴 숨김 |
| REQ-018 | covered | 행위별 수동/자동 관찰 기준 · menu_permissions · listMenuPermissions/saveMenuPermission · 비허용 URL 서버 차단 |
| REQ-019 | covered | 행위별 수동/자동 관찰 기준 · menus · getMenuStructure/saveMenuStructure · 부모메뉴·계층 저장 |
| REQ-020 | covered | 행위별 수동/자동 관찰 기준 · menus · getMenuStructure/saveMenuStructure · 동일 계층 표시순서 적용 |
| REQ-021 | data-constraint | 행위별 수동/자동 관찰 기준 · menus · getMenuStructure/saveMenuStructure · 다른 부모 아래 순서 보존 |
| REQ-022 | covered | 행위별 수동/자동 관찰 기준 · menu_execution_info · listMenuExecutionInfo/saveMenuExecutionInfo · 메뉴 실행정보 저장 |
| REQ-023 | covered | 행위별 수동/자동 관찰 기준 · menu_execution_info · listMenuExecutionInfo/saveMenuExecutionInfo · 사용 가능한 메뉴 화면 실행 |
| REQ-024 | data-constraint | 행위별 수동/자동 관찰 기준 · menu_execution_info · listMenuExecutionInfo/saveMenuExecutionInfo · 실행정보 변경의 권한 비간섭 |
| REQ-025 | covered | 행위별 수동/자동 관찰 기준 · code_groups · listCodeGroups/saveCodeGroup · 코드그룹 정의 저장 |
| REQ-026 | covered | 행위별 수동/자동 관찰 기준 · code_groups · listCodeGroups/saveCodeGroup · 그룹ID별 독립 조회 |
| REQ-027 | data-constraint | 행위별 수동/자동 관찰 기준 · code_groups · listCodeGroups/saveCodeGroup · 그룹 수정의 상세코드 비간섭 |
| REQ-028 | covered | 행위별 수동/자동 관찰 기준 · detail_codes · listDetailCodes/saveDetailCode · 그룹별 상세코드 조회 |
| REQ-029 | covered | 행위별 수동/자동 관찰 기준 · detail_codes · listDetailCodes/saveDetailCode · 상세코드 필드 저장 |
| REQ-030 | covered | 행위별 수동/자동 관찰 기준 · detail_codes · listDetailCodes/saveDetailCode · 선택값 정렬순서 반영 |
| REQ-031 | covered | 행위별 수동/자동 관찰 기준 · detail_codes · listDetailCodes/saveDetailCode · 상위코드 계층 식별 |
| REQ-032 | non-functional | 행위별 수동/자동 관찰 기준 · React/TypeScript/Vite 번들 선택은 frontend/package.json의 빌드 의존성 계약이며 REST 입출력이 아니다. |
| REQ-033 | non-functional | 행위별 수동/자동 관찰 기준 · Java 17/Spring Boot 선택은 backend/pom.xml과 런타임의 컴파일 계약이다. |
| REQ-034 | non-functional | 행위별 수동/자동 관찰 기준 · PostgreSQL 선택은 data-model.md의 매핑·DB 접속 대상이지 신규 업무 API가 아니다. |
| REQ-035 | non-functional | 행위별 수동/자동 관찰 기준 · Docker Compose 실행 방식은 infra/docker-compose.yml의 서비스 연결 책임이다. |
| REQ-036 | non-functional | 행위별 수동/자동 관찰 기준 · 동기 blocking MyBatis는 plan.md marker의 실행 결정으로 모든 요청 처리 내부에 적용된다. |
| REQ-037 | non-functional | 행위별 수동/자동 관찰 기준 · BOM은 frontend/package.json/backend/pom.xml/DB 이미지 버전 검증으로 충족한다. |
| REQ-038 | non-functional | 행위별 수동/자동 관찰 기준 · backend 디렉터리 산출 여부는 저장소 파일 검사 대상이다. |
| REQ-039 | non-functional | 행위별 수동/자동 관찰 기준 · frontend 디렉터리 산출 여부는 저장소 파일 검사 대상이다. |
| REQ-040 | non-functional | 행위별 수동/자동 관찰 기준 · Compose 파일 위치는 infra/docker-compose.yml 파일 존재와 config 검사로 확인한다. |
| REQ-041 | covered | 행위별 수동/자동 관찰 기준 · getHealth /api/health |
| REQ-042 | non-functional | 행위별 수동/자동 관찰 기준 · 레퍼런스의 sidebar/header/밀도/토큰/반응형은 ui-design.md 공통 시각 설계이며 API를 만들지 않는다. |
| REQ-043 | non-functional | 행위별 수동/자동 관찰 기준 · spec.md 기술스택 계약에서 백엔드·프론트엔드 범위를 문서화한다. |
| REQ-044 | non-functional | 행위별 수동/자동 관찰 기준 · plan.md Project Structure와 생성 순서가 소유하는 구현 배치 계약이다. |
| REQ-045 | non-functional | 행위별 수동/자동 관찰 기준 · tasks.md Setup/Foundational/US 단계가 backend/frontend/infra 작업을 배정한다. |
| REQ-046 | non-functional | 행위별 수동/자동 관찰 기준 · ui-design.md 레퍼런스 구현 검수 기준은 화면 공통 스타일 책임이다. |
| REQ-047 | non-functional | 행위별 수동/자동 관찰 기준 · plan.md의 exact marker JSON 보존은 문서 정본 검증 대상이다. |
| REQ-048 | data-constraint | 행위별 수동/자동 관찰 기준 · data-model.md Entity Registry와 tasks.md migration 작업이 스키마 기준을 충족한다. |
| REQ-049 | non-functional | 행위별 수동/자동 관찰 기준 · quickstart.md API/UI 동시 smoke 절차와 seed fixture로 검증한다. |
| REQ-050 | non-functional | 행위별 수동/자동 관찰 기준 · 각 build context의 .dockerignore 제외 집합을 infra 검사에서 확인한다. |

## Infrastructure generation receipt — execution pending

The text above is the preserved staged design baseline, including its historical document-generation
claims and planned broad commands. It is not a report of this infrastructure pass or a runner result.
This pass copied the nine canonical documents to durable paths, preserved the plan marker and authored
Compose, image definitions, context exclusions, a pinned jar-free Maven launcher and infrastructure
checks. It did not modify staged inputs, backend application sources/POM/SQL or frontend application
sources/packages/tests. No task has been marked complete.

The infrastructure checks were authored first but not executed: neither RED nor GREEN was observed.
No Docker, build, test, npm, Maven, network or development-server command was run. Static inspection
only is permitted in this generation phase. Runtime validation and policy approval are pending.

Use [the focused implementation handoff](../../../docs/implementation-handoff.md) for runner-only
commands and prerequisites instead of the broad historical commands above. A real npm lockfile must
be materialized by the frontend owner/authorized runner before `npm ci` or frontend image build.
The smoke checks do not load synthetic administrator fixtures or prove authenticated write/readback.
OQ-AUTH-001/002, period boundaries, approver validation and KORUS source integration remain blockers;
no auth backdoor, synthetic administrator or implicit approver policy has been introduced by this pass.
Record sanitized runner evidence here before completing any task checkbox.
