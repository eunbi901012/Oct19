# 구현 작성 및 runner 인계

## 판정

소스·테스트·배포 설정을 작성했다. 이 문서는 빌드 성공, API 실행 성공 또는 업무 승인 완료 증거가 아니다.
Fast-mode 경계에 따라 Maven/npm/Vitest/Playwright/PostgreSQL/Docker 실행은 runner에 남겼다.
도구의 자동 TypeScript 검사도 실행 가능한 compiler가 없어 skipped로 보고되었으며 타입 검사 통과가 아니다.

## 작성 범위

- Java 17 / Spring Boot 3.3.13 / Maven executable boot jar / blocking MyBatis.
- PostgreSQL 16 대상 Entity Registry 10개 테이블의 PK/FK와 인덱스, 동일한 초기 Flyway migration.
- OpenAPI의 21개 operation에 대응하는 HTTP 매핑, snake_case envelope, 입력 allowlist 및 오류 응답.
- 사용자 원천정보 수정 금지, 로컬 enabled 저장, 조직관계 이력, 역할·부여·권한·메뉴·코드 저장 경로.
- 인증 adapter, operation scope, 실효 메뉴 판정, 기간·승인·원천 소유권의 교체 가능한 정책 경계.
- nginx auth_request → ScreenAccessController → ScreenAccessService의 직접 화면 URL 권한 검사.
- React 18 / TypeScript / Vite 5의 9개 화면과 상대 /api 호출, 서버 메뉴 기반 탐색.
- 저장 후 재조회, 취소, 선택 ID 전달, UUID 재시도 보존, 원천·식별자 readonly와 공통 상태 처리.
- MockMvc 계약 시험, rollback 기반 PostgreSQL/MyBatis 시험, Vitest UI 시험 및 실제 서버 Playwright 시험.
- 앱별 Dockerfile/.dockerignore, infra/docker-compose.yml, healthcheck 및 runner용 스택 검사.

첨부파일·포털 콘텐츠·백업 API는 현재 OpenAPI/업무 범위에 없으므로 임의로 추가하지 않았다.

## 구조상 차이와 미완료 항목

tasks.md의 기능별 Controller/Service/Mapper XML 파일 분할 대신 현재 backend는
ManagementController → CatalogService → CatalogMapper/CatalogSql의 통합 경로를 사용한다.
MyBatis SQL Provider는 승인된 Resource 컬럼 allowlist만으로 동적 predicate와 SQL을 생성한다.
이는 XML mapper를 사용하는 작업별 파일 배치와 다르며, 해당 파일명 기반 task를 완료로 표시하지 않았다.
기능별 테스트도 BusinessHttpTest와 PostgreSqlHttpTest에 통합되어 있다.
frontend의 shell과 route 등록은 App.tsx에 있고, 화면별 계약은 9개 Page와 ResourcePage 설정으로 구성한다.
초기 승인 제약은 V001/schema.sql에 통합했으며 불필요한 빈 V002–V010 migration은 만들지 않았다.
따라서 tasks.md의 모든 파일별 산출물이 정확히 완성되었다고 주장하지 않는다.

npm 실행 금지로 package-lock.json은 생성하지 않았다. runner가 먼저 npm으로 lockfile을 materialize해야
Dockerfile의 npm ci 및 frontend 검증을 실행할 수 있다. lockfile을 수작업으로 작성하지 않았다.
Swagger는 의존성·런타임 경로를 임의로 늘리지 않기 위해 추가하지 않았다.

## 승인 차단

기본 AuthenticationAdapter는 미인증으로 처리하므로 보호 API/직접 화면 URL은 401이다.
기관 승인 인증 adapter를 연결해도 기본 권한·기간 정책은 승인을 요구한다.
임의 관리자 계정, 역할 코드, 헤더/토큰 우회, 실제 KORUS 연동 또는 운영 fixture는 없다.
승인된 정책 bean을 연결하기 전에는 이 앱을 업무 운영 가능/합격 상태로 판정하면 안 된다.

- OQ-AUTH-001/002: 인증, 관리 scope, 일반 사용자 탐색 scope, 다중 대상 권한 조합.
- OQ-ORG-001 / OQ-ROLE-001: 원천 소유권, 기간 경계, 조직 순환·중복, 승인자 신원, 회수 효력.
- OQ-UI-001/002/003/004: 루트·상위코드 표현, 실행 URL 안전성 및 시각 규칙 승인.
- OQ-DATA-001/002: 실제 KORUS 적재·동기화·원천 보존·접속 차단 정책.

MockMvc에서 주입한 허용 정책과 합성 fixture는 시험용이다. 운영 정책 승인 증거로 사용할 수 없다.

## 실제 수행한 정적 확인

- OpenAPI 파싱 후 21개 method/path와 생산 코드의 21개 literal API 매핑 집합 비교:
  누락 없음, 추가 API 없음, 중복 매핑 없음. 이는 런타임 등록·응답 검증이 아니다.
- durable classpath OpenAPI fixture와 제공 입력 byte 일치 확인.
- schema.sql과 V001__foundation.sql byte 일치 확인.
- 테이블 10개, PRIMARY KEY 10개, REFERENCES 13개 정적 확인.
- shell의 bash/sh 구문 및 shell에 포함된 Python AST 파싱.
- JSON manifest, Maven XML, Compose YAML 파싱 및 ignore/context 설정 점검.
- 생성 소스의 staging 입력 경로 참조·trailing whitespace·120자 초과 행 점검.

## runner 검증 순서

명령은 인계용이며 이 세션에서 실행하지 않았다. 전체 Maven suite 대신 클래스별로 실행한다.

1. backend에서 ./mvnw -Dtest=ApiFoundationTest test
2. backend에서 ./mvnw -Dtest=BusinessHttpTest test
3. backend에서 ./mvnw -Dtest=ScreenAccessContractTest test
4. 전용 PostgreSQL 16과 TEST_DATABASE_URL/TEST_DATABASE_USER/TEST_DATABASE_PASSWORD를 준비한 뒤 각각 실행:
   PersistenceContractTest, MyBatisPostgresContractTest, PostgreSqlHttpTest.
   환경변수 미설정 skip은 PASS가 아니다. 운영 DB 사용 금지.
5. frontend에서 npm install --package-lock-only로 npm lockfile 생성 후 npm ci.
6. frontend에서 npm run test -- --run, npm run typecheck, npm run build.
7. 승인된 인증/권한/기간 정책과 전용 합성 fixture를 연결한 Compose에서 API와 화면을 함께 확인.
8. 승인 세션은 E2E_STORAGE_STATE로 외부 또는 git-ignored 위치에서 제공한다. 인증 trace는 꺼 두었다.
   실제 fixture 쓰기 시험은 E2E_MUTATION_APPROVED=true를 명시해야 한다.
   조직·승인자·권한 쓰기는 각 추가 승인 환경변수도 필요하다.
   조직 복구는 과거 이력을 삭제하는 것이 아니라 관계 이력을 추가하므로 폐기 가능한 fixture를 사용한다.
9. infra/tests/stack-contract.test.sh 및 infra/tests/compose-smoke.test.sh를 runner에서 실행한다.

Compose·도구 버전·환경변수의 상세 인계는 implementation-handoff.md,
DB 단독 시험 조건은 backend/DATABASE-CONTRACT.md를 참조한다.
파일 목록은 generated-files.txt에 기록한다.
