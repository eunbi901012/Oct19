# PostgreSQL 영속성 인계

## 정본과 초기화

- `src/main/resources/schema.sql`은 10개 Entity Registry의 SQL 정본이다.
- `src/main/resources/db/migration/V001__foundation.sql`은 최초 적용용 byte-identical 복사본이다.
- 애플리케이션 초기화는 Flyway 단독으로 수행한다. Spring SQL init은 사용하지 않는다.
- 적용 후 V001 변경은 금지하며 후속 변경은 새 migration으로 작성해야 한다.
- 모든 CREATE TABLE/INDEX는 IF NOT EXISTS를 사용한다. PK/FK는 테이블 선언에 포함한다.
- 임의 전체 audit/soft-delete 필드, business_roles/is_current 물리 컬럼은 생성하지 않았다.
- 역할 정의·메뉴 실행정보·코드그룹 정의의 변경이 연결 데이터를 갱신하는 trigger/cascade는 없다.

## 승인 대기와 명세 충돌

- 비-PK 필드의 NOT NULL, 문자열 길이, 기간 순서·중복·순환 제약은 데이터 계약에 승인 근거가 없어 추가하지 않았다.
- menus.parent_menu_id, detail_codes.parent_code는 실제 FK이며 루트 표현은 승인 대기다.
  DB NULL을 응답 빈 문자열로 치환하지 않는다. 응답 required/nonnullable 루트 표현은 계약 확인이 필요하다.
- users.organization_code는 데이터 계약의 FK 대상이 지정되지 않은 KORUS 원천값이다.
  로컬 organizations에 존재해야 한다는 제약을 추가하지 않았다.
- users.employment_status 허용값은 KORUS 승인 대기다. AGENTS.md의 코드열거 주석 형식과 충돌하므로
  추측 코드 대신 해당 컬럼 주석에 승인 대기를 명시했다.
- user_role_assignments.origin/action의 한글값은 계약의 출처/명령값이다. lifecycle 상태 enum이 아니다.
- 기간·회수 효력·승인자 정책과 메뉴 권한 조합의 결정은 서비스의 승인 정책 경계에 속한다.
- 원천 연계와 초기 데이터 적재는 승인 규격이 없으므로 production seed 또는 가상 관리자를 넣지 않았다.

## 작성한 실행 검증 대상

`PersistenceContractTest`는 TEST_DATABASE_URL, TEST_DATABASE_USER, TEST_DATABASE_PASSWORD가 설정된
전용 PostgreSQL에서 임시 schema를 트랜잭션 내부에 생성한다. schema.sql의 이중 실행, 10개 table,
projection 비저장, 저장/재조회/rollback, 복합 PK, 같은 그룹 상위코드 FK와 사용자 FK를 검증한 뒤 rollback한다.
URL이 없으면 시험은 명시적으로 건너뛰며 이 결과는 PASS 근거가 아니다.
실행 계정에는 CREATE SCHEMA 권한이 필요하다. 운영 데이터베이스 사용은 금지한다.

`MyBatisPostgresContractTest`는 동일한 전용 DB 전제에서 실제 `CatalogMapper`와 `CatalogSql`을 등록한다.
null 선택 필터 생략, false 조건, 0행, nullable 키와 TIMESTAMPTZ microsecond materialization,
역할 정의의 부여내역 비간섭, 기존 origin 보존, 그룹별 코드 정렬·상위코드 생략 보존,
그룹 정의의 상세코드 비간섭을 검증한다. 기관의 권한·기간 정책이 승인됐다는 증거는 아니다.
각 시험은 schema와 fixture 전체를 최종 rollback하며 운영 fixture를 만들지 않는다.

이번 생성 세션에서는 Maven·시험·Docker·PostgreSQL 연결을 실행하지 않았다.
스키마 적용·JDBC materialization·API DB 재조회는 모두 awaiting runner verification이다.
