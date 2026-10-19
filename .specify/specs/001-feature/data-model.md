# 데이터 모델 — oct19

원문에 물리 table명이 없으므로 아래 이름·PK·FK·타입·인덱스는 구현을 위한 설계 제안이다. data-model.md가 데이터 계약 정본이며 별도 data-model.json/seed-data.md는 생성하지 않는다. 원천/로컬 경계 미확인 부분은 사업 승인 전 고정하지 않는다. REQ-048.

## Entity Registry

| entity/table | 목적 | 주요 키 | canonical_id | 승인 상태 |
| --- | --- | --- | --- | --- |
| users | 사용자 관리 | user_id | REQ-001, REQ-002, REQ-003, REQ-004 | 물리명·mapping 설계 제안; 정책 OQ 별도 |
| organizations | 조직 관리 | organization_code | REQ-005, REQ-006, REQ-007, REQ-008 | 물리명·mapping 설계 제안; 정책 OQ 별도 |
| roles | 역할 관리 | role_id | REQ-009, REQ-010, REQ-011 | 물리명·mapping 설계 제안; 정책 OQ 별도 |
| user_role_assignments | 사용자 역할 관리 | assignment_id | REQ-012, REQ-013, REQ-014, REQ-015 | 물리명·mapping 설계 제안; 정책 OQ 별도 |
| menu_permissions | 메뉴 권한 관리 | permission_id | REQ-016, REQ-017, REQ-018 | 물리명·mapping 설계 제안; 정책 OQ 별도 |
| menus | 메뉴 구조 관리 | menu_id | REQ-019, REQ-020, REQ-021 | 물리명·mapping 설계 제안; 정책 OQ 별도 |
| menu_execution_info | 메뉴 정보 관리 | menu_id | REQ-022, REQ-023, REQ-024 | 물리명·mapping 설계 제안; 정책 OQ 별도 |
| code_groups | 코드그룹 관리 | group_id | REQ-025, REQ-026, REQ-027 | 물리명·mapping 설계 제안; 정책 OQ 별도 |
| detail_codes | 상세코드 관리 | group_id + code | REQ-028, REQ-029, REQ-030, REQ-031 | 물리명·mapping 설계 제안; 정책 OQ 별도 |
| organization_relations | 조직 관리 변경 전 관계·기간 | relation_id | REQ-005, REQ-006, REQ-007, REQ-008 | 물리명·mapping 설계 제안; 정책 OQ 별도 |

## Entity/Table Contract

### users 계약

- 목적: 사용자 관리의 조회·저장 상태. 연결: REQ-001, REQ-002, REQ-003, REQ-004.
- PK: `user_id`. 역할 식별정보는 고유하며 상세코드는 그룹 내부 code를 복합키로 식별한다. 나머지 business key 중복 정책은 확인 필요.
- Source of truth: KORUS 인사 필드는 upstream readonly, enabled는 로컬; business_roles는 역할 부여 projection.
- 조회 인덱스 제안: `user_id` 및 `user_id`. 검색 규모와 추가 인덱스는 OQ-TECH-001 후 결정한다.
- 수명·삭제: 일반 삭제·cascade를 가정하지 않는다. 참조 행의 자동 삭제를 제안하지 않으며 보존기간·사용중지 정책은 OQ-DATA-002. 관계 이력 보존은 REQ-007로 확정이다.
- 트랜잭션: imperative 서비스의 단일 blocking MyBatis transaction에서 소유 table 변경과 원문 요구 검증을 수행한다. 동시 변경 충돌·잠금 수준은 미정이며 PostgreSQL 기본 동작 이상을 승인된 정책으로 주장하지 않는다.

### organizations 계약

- 목적: 조직 관리의 조회·저장 상태. 연결: REQ-005, REQ-006, REQ-007, REQ-008.
- PK: `organization_code`. 역할 식별정보는 고유하며 상세코드는 그룹 내부 code를 복합키로 식별한다. 나머지 business key 중복 정책은 확인 필요.
- Source of truth: 로컬 저장 제안; 조직 원천 필드 소유권과 보직 기반 부여 생성 규칙은 OQ-DATA-001/OQ-ORG-001/OQ-ROLE-001.
- 조회 인덱스 제안: `organization_code` 및 `organization_code`. 검색 규모와 추가 인덱스는 OQ-TECH-001 후 결정한다.
- 수명·삭제: 일반 삭제·cascade를 가정하지 않는다. 참조 행의 자동 삭제를 제안하지 않으며 보존기간·사용중지 정책은 OQ-DATA-002. 관계 이력 보존은 REQ-007로 확정이다.
- 트랜잭션: imperative 서비스의 단일 blocking MyBatis transaction에서 소유 table 변경과 원문 요구 검증을 수행한다. 동시 변경 충돌·잠금 수준은 미정이며 PostgreSQL 기본 동작 이상을 승인된 정책으로 주장하지 않는다.

### roles 계약

- 목적: 역할 관리의 조회·저장 상태. 연결: REQ-009, REQ-010, REQ-011.
- PK: `role_id`. 역할 식별정보는 고유하며 상세코드는 그룹 내부 code를 복합키로 식별한다. 나머지 business key 중복 정책은 확인 필요.
- Source of truth: 로컬 저장 제안; 조직 원천 필드 소유권과 보직 기반 부여 생성 규칙은 OQ-DATA-001/OQ-ORG-001/OQ-ROLE-001.
- 조회 인덱스 제안: `role_id` 및 `role_id`. 검색 규모와 추가 인덱스는 OQ-TECH-001 후 결정한다.
- 수명·삭제: 일반 삭제·cascade를 가정하지 않는다. 참조 행의 자동 삭제를 제안하지 않으며 보존기간·사용중지 정책은 OQ-DATA-002. 관계 이력 보존은 REQ-007로 확정이다.
- 트랜잭션: imperative 서비스의 단일 blocking MyBatis transaction에서 소유 table 변경과 원문 요구 검증을 수행한다. 동시 변경 충돌·잠금 수준은 미정이며 PostgreSQL 기본 동작 이상을 승인된 정책으로 주장하지 않는다.

### user_role_assignments 계약

- 목적: 사용자 역할 관리의 조회·저장 상태. 연결: REQ-012, REQ-013, REQ-014, REQ-015.
- PK: `assignment_id`. 역할 식별정보는 고유하며 상세코드는 그룹 내부 code를 복합키로 식별한다. 나머지 business key 중복 정책은 확인 필요.
- Source of truth: 로컬 저장 제안; 조직 원천 필드 소유권과 보직 기반 부여 생성 규칙은 OQ-DATA-001/OQ-ORG-001/OQ-ROLE-001.
- 조회 인덱스 제안: `assignment_id` 및 `assignment_id`. 검색 규모와 추가 인덱스는 OQ-TECH-001 후 결정한다.
- 수명·삭제: 일반 삭제·cascade를 가정하지 않는다. 참조 행의 자동 삭제를 제안하지 않으며 보존기간·사용중지 정책은 OQ-DATA-002. 관계 이력 보존은 REQ-007로 확정이다.
- 트랜잭션: imperative 서비스의 단일 blocking MyBatis transaction에서 소유 table 변경과 원문 요구 검증을 수행한다. 동시 변경 충돌·잠금 수준은 미정이며 PostgreSQL 기본 동작 이상을 승인된 정책으로 주장하지 않는다.

### menu_permissions 계약

- 목적: 메뉴 권한 관리의 조회·저장 상태. 연결: REQ-016, REQ-017, REQ-018.
- PK: `permission_id`. 역할 식별정보는 고유하며 상세코드는 그룹 내부 code를 복합키로 식별한다. 나머지 business key 중복 정책은 확인 필요.
- Source of truth: 로컬 저장 제안; 조직 원천 필드 소유권과 보직 기반 부여 생성 규칙은 OQ-DATA-001/OQ-ORG-001/OQ-ROLE-001.
- 조회 인덱스 제안: `permission_id` 및 `permission_id`. 검색 규모와 추가 인덱스는 OQ-TECH-001 후 결정한다.
- 수명·삭제: 일반 삭제·cascade를 가정하지 않는다. 참조 행의 자동 삭제를 제안하지 않으며 보존기간·사용중지 정책은 OQ-DATA-002. 관계 이력 보존은 REQ-007로 확정이다.
- 트랜잭션: imperative 서비스의 단일 blocking MyBatis transaction에서 소유 table 변경과 원문 요구 검증을 수행한다. 동시 변경 충돌·잠금 수준은 미정이며 PostgreSQL 기본 동작 이상을 승인된 정책으로 주장하지 않는다.

### menus 계약

- 목적: 메뉴 구조 관리의 조회·저장 상태. 연결: REQ-019, REQ-020, REQ-021.
- PK: `menu_id`. 역할 식별정보는 고유하며 상세코드는 그룹 내부 code를 복합키로 식별한다. 나머지 business key 중복 정책은 확인 필요.
- Source of truth: 로컬 저장 제안; 조직 원천 필드 소유권과 보직 기반 부여 생성 규칙은 OQ-DATA-001/OQ-ORG-001/OQ-ROLE-001.
- 조회 인덱스 제안: `menu_id` 및 `menu_id`. 검색 규모와 추가 인덱스는 OQ-TECH-001 후 결정한다.
- 수명·삭제: 일반 삭제·cascade를 가정하지 않는다. 참조 행의 자동 삭제를 제안하지 않으며 보존기간·사용중지 정책은 OQ-DATA-002. 관계 이력 보존은 REQ-007로 확정이다.
- 트랜잭션: imperative 서비스의 단일 blocking MyBatis transaction에서 소유 table 변경과 원문 요구 검증을 수행한다. 동시 변경 충돌·잠금 수준은 미정이며 PostgreSQL 기본 동작 이상을 승인된 정책으로 주장하지 않는다.

### menu_execution_info 계약

- 목적: 메뉴 정보 관리의 조회·저장 상태. 연결: REQ-022, REQ-023, REQ-024.
- PK: `menu_id`. 역할 식별정보는 고유하며 상세코드는 그룹 내부 code를 복합키로 식별한다. 나머지 business key 중복 정책은 확인 필요.
- Source of truth: 로컬 저장 제안; 조직 원천 필드 소유권과 보직 기반 부여 생성 규칙은 OQ-DATA-001/OQ-ORG-001/OQ-ROLE-001.
- 조회 인덱스 제안: `menu_id` 및 `menu_id`. 검색 규모와 추가 인덱스는 OQ-TECH-001 후 결정한다.
- 수명·삭제: 일반 삭제·cascade를 가정하지 않는다. 참조 행의 자동 삭제를 제안하지 않으며 보존기간·사용중지 정책은 OQ-DATA-002. 관계 이력 보존은 REQ-007로 확정이다.
- 트랜잭션: imperative 서비스의 단일 blocking MyBatis transaction에서 소유 table 변경과 원문 요구 검증을 수행한다. 동시 변경 충돌·잠금 수준은 미정이며 PostgreSQL 기본 동작 이상을 승인된 정책으로 주장하지 않는다.

### code_groups 계약

- 목적: 코드그룹 관리의 조회·저장 상태. 연결: REQ-025, REQ-026, REQ-027.
- PK: `group_id`. 역할 식별정보는 고유하며 상세코드는 그룹 내부 code를 복합키로 식별한다. 나머지 business key 중복 정책은 확인 필요.
- Source of truth: 로컬 저장 제안; 조직 원천 필드 소유권과 보직 기반 부여 생성 규칙은 OQ-DATA-001/OQ-ORG-001/OQ-ROLE-001.
- 조회 인덱스 제안: `group_id` 및 `group_id`. 검색 규모와 추가 인덱스는 OQ-TECH-001 후 결정한다.
- 수명·삭제: 일반 삭제·cascade를 가정하지 않는다. 참조 행의 자동 삭제를 제안하지 않으며 보존기간·사용중지 정책은 OQ-DATA-002. 관계 이력 보존은 REQ-007로 확정이다.
- 트랜잭션: imperative 서비스의 단일 blocking MyBatis transaction에서 소유 table 변경과 원문 요구 검증을 수행한다. 동시 변경 충돌·잠금 수준은 미정이며 PostgreSQL 기본 동작 이상을 승인된 정책으로 주장하지 않는다.

### detail_codes 계약

- 목적: 상세코드 관리의 조회·저장 상태. 연결: REQ-028, REQ-029, REQ-030, REQ-031.
- PK: `group_id + code`. 역할 식별정보는 고유하며 상세코드는 그룹 내부 code를 복합키로 식별한다. 나머지 business key 중복 정책은 확인 필요.
- Source of truth: 로컬 저장 제안; 조직 원천 필드 소유권과 보직 기반 부여 생성 규칙은 OQ-DATA-001/OQ-ORG-001/OQ-ROLE-001.
- 조회 인덱스 제안: `group_id + code` 및 `group_id`. 검색 규모와 추가 인덱스는 OQ-TECH-001 후 결정한다.
- 수명·삭제: 일반 삭제·cascade를 가정하지 않는다. 참조 행의 자동 삭제를 제안하지 않으며 보존기간·사용중지 정책은 OQ-DATA-002. 관계 이력 보존은 REQ-007로 확정이다.
- 트랜잭션: imperative 서비스의 단일 blocking MyBatis transaction에서 소유 table 변경과 원문 요구 검증을 수행한다. 동시 변경 충돌·잠금 수준은 미정이며 PostgreSQL 기본 동작 이상을 승인된 정책으로 주장하지 않는다.

### organization_relations 계약

- 목적: 조직 관리의 변경 전 상위관계·기간 보존. 연결: REQ-005, REQ-006, REQ-007, REQ-008.
- PK: `relation_id`. 역할 식별정보는 고유하며 상세코드는 그룹 내부 code를 복합키로 식별한다. 나머지 business key 중복 정책은 확인 필요.
- Source of truth: 로컬 저장 제안; 조직 원천 필드 소유권과 보직 기반 부여 생성 규칙은 OQ-DATA-001/OQ-ORG-001/OQ-ROLE-001.
- 조회 인덱스 제안: `relation_id` 및 `organization_code, valid_from, valid_to`. 검색 규모와 추가 인덱스는 OQ-TECH-001 후 결정한다.
- 수명·삭제: 일반 삭제·cascade를 가정하지 않는다. 참조 행의 자동 삭제를 제안하지 않으며 보존기간·사용중지 정책은 OQ-DATA-002. 관계 이력 보존은 REQ-007로 확정이다.
- 트랜잭션: imperative 서비스의 단일 blocking MyBatis transaction에서 소유 table 변경과 원문 요구 검증을 수행한다. 동시 변경 충돌·잠금 수준은 미정이며 PostgreSQL 기본 동작 이상을 승인된 정책으로 주장하지 않는다.

## Field Contract

| Entity | Field | Type | Constraint | Enum | Relationship | Audit/Delete Policy | canonical_id |
| --- | --- | --- | --- | --- | --- | --- | --- |
| users | user_id | text | PK | — | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-001, REQ-002, REQ-003, REQ-004 |
| users | employee_number | text | 길이·NULL 허용은 원문 미지정; DTO required는 기술 제안 | — | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-001, REQ-002, REQ-003, REQ-004 |
| users | person_name | text | 길이·NULL 허용은 원문 미지정; DTO required는 기술 제안 | — | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-001, REQ-002, REQ-003, REQ-004 |
| users | organization_code | text | 길이·NULL 허용은 원문 미지정; DTO required는 기술 제안 | — | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-001, REQ-002, REQ-003, REQ-004 |
| users | job_grade | text | 길이·NULL 허용은 원문 미지정; DTO required는 기술 제안 | — | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-001, REQ-002, REQ-003, REQ-004 |
| users | employment_status | text | 길이·NULL 허용은 원문 미지정; DTO required는 기술 제안 | — | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-001, REQ-002, REQ-003, REQ-004 |
| users | business_roles | roles 연결 projection; 물리 저장하지 않음 | 길이·NULL 허용은 원문 미지정; DTO required는 기술 제안 | — | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-001, REQ-002, REQ-003, REQ-004 |
| users | enabled | boolean | 길이·NULL 허용은 원문 미지정; DTO required는 기술 제안 | — | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-001, REQ-002, REQ-003, REQ-004 |
| users | position | text | 길이·NULL 허용은 원문 미지정; DTO required는 기술 제안 | — | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-001, REQ-002, REQ-003, REQ-004 |
| users | retired_on | date | 길이·NULL 허용은 원문 미지정; DTO required는 기술 제안 | — | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-001, REQ-002, REQ-003, REQ-004 |
| users | last_synced_at | timestamptz | 길이·NULL 허용은 원문 미지정; DTO required는 기술 제안 | — | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-001, REQ-002, REQ-003, REQ-004 |
| organizations | organization_code | text | PK | — | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-005, REQ-006, REQ-007, REQ-008 |
| organizations | organization_type | text | 길이·NULL 허용은 원문 미지정; DTO required는 기술 제안 | — | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-005, REQ-006, REQ-007, REQ-008 |
| roles | role_id | text | PK | — | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-009, REQ-010, REQ-011 |
| roles | purpose | text | 길이·NULL 허용은 원문 미지정; DTO required는 기술 제안 | — | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-009, REQ-010, REQ-011 |
| roles | grant_criteria | text | 길이·NULL 허용은 원문 미지정; DTO required는 기술 제안 | — | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-009, REQ-010, REQ-011 |
| roles | default_data_scope | text | 길이·NULL 허용은 원문 미지정; DTO required는 기술 제안 | — | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-009, REQ-010, REQ-011 |
| user_role_assignments | assignment_id | text | PK | — | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-012, REQ-013, REQ-014, REQ-015 |
| user_role_assignments | user_id | text | FK | — | users.user_id | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-012, REQ-013, REQ-014, REQ-015 |
| user_role_assignments | role_id | text | FK | — | roles.role_id | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-012, REQ-013, REQ-014, REQ-015 |
| user_role_assignments | valid_from | date | 길이·NULL 허용은 원문 미지정; DTO required는 기술 제안 | — | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-012, REQ-013, REQ-014, REQ-015 |
| user_role_assignments | valid_to | date | 길이·NULL 허용은 원문 미지정; DTO required는 기술 제안 | — | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-012, REQ-013, REQ-014, REQ-015 |
| user_role_assignments | approver_id | text | FK | — | users.user_id (승인자 원천은 OQ-ROLE-001) | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-012, REQ-013, REQ-014, REQ-015 |
| user_role_assignments | origin | text | 길이·NULL 허용은 원문 미지정; DTO required는 기술 제안 | 보직 기반, 수동 | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-012, REQ-013, REQ-014, REQ-015 |
| user_role_assignments | action | text | 길이·NULL 허용은 원문 미지정; DTO required는 기술 제안 | 부여, 변경, 회수 | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-012, REQ-013, REQ-014, REQ-015 |
| menu_permissions | permission_id | text | PK | — | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-016, REQ-017, REQ-018 |
| menu_permissions | role_id | text | FK | — | roles.role_id | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-016, REQ-017, REQ-018 |
| menu_permissions | organization_code | text | FK | — | organizations.organization_code | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-016, REQ-017, REQ-018 |
| menu_permissions | user_id | text | FK | — | users.user_id | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-016, REQ-017, REQ-018 |
| menu_permissions | menu_id | text | FK | — | menus.menu_id | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-016, REQ-017, REQ-018 |
| menu_permissions | allowed | boolean | 길이·NULL 허용은 원문 미지정; DTO required는 기술 제안 | — | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-016, REQ-017, REQ-018 |
| menus | menu_id | text | PK | — | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-019, REQ-020, REQ-021 |
| menus | parent_menu_id | text | FK | — | menus.menu_id | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-019, REQ-020, REQ-021 |
| menus | level | integer | 길이·NULL 허용은 원문 미지정; DTO required는 기술 제안 | — | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-019, REQ-020, REQ-021 |
| menus | display_order | integer | 길이·NULL 허용은 원문 미지정; DTO required는 기술 제안 | — | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-019, REQ-020, REQ-021 |
| menu_execution_info | menu_id | text | PK | — | menus.menu_id | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-022, REQ-023, REQ-024 |
| menu_execution_info | screen_id | text | 길이·NULL 허용은 원문 미지정; DTO required는 기술 제안 | — | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-022, REQ-023, REQ-024 |
| menu_execution_info | url | text | 길이·NULL 허용은 원문 미지정; DTO required는 기술 제안 | — | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-022, REQ-023, REQ-024 |
| code_groups | group_id | text | PK | — | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-025, REQ-026, REQ-027 |
| code_groups | name | text | 길이·NULL 허용은 원문 미지정; DTO required는 기술 제안 | — | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-025, REQ-026, REQ-027 |
| code_groups | description | text | 길이·NULL 허용은 원문 미지정; DTO required는 기술 제안 | — | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-025, REQ-026, REQ-027 |
| code_groups | managing_department | text | 길이·NULL 허용은 원문 미지정; DTO required는 기술 제안 | — | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-025, REQ-026, REQ-027 |
| detail_codes | group_id | text | PK | — | code_groups.group_id | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-028, REQ-029, REQ-030, REQ-031 |
| detail_codes | code | text | PK | — | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-028, REQ-029, REQ-030, REQ-031 |
| detail_codes | sort_order | integer | 길이·NULL 허용은 원문 미지정; DTO required는 기술 제안 | — | 없음 | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-028, REQ-029, REQ-030, REQ-031 |
| detail_codes | parent_code | text | FK | — | detail_codes.(group_id, code) | 보존·삭제 정책 OQ-DATA-002; 자동 cascade 제안 안 함 | REQ-028, REQ-029, REQ-030, REQ-031 |
| organization_relations | relation_id | text | PK | — | 없음 | 이전 관계·적용기간 보존(REQ-007) | REQ-005, REQ-006, REQ-007, REQ-008 |
| organization_relations | organization_code | text | FK | — | organizations.organization_code | 이전 관계·적용기간 보존(REQ-007) | REQ-005, REQ-006, REQ-007, REQ-008 |
| organization_relations | parent_code | text | FK | — | organizations.organization_code | 이전 관계·적용기간 보존(REQ-007) | REQ-005, REQ-006, REQ-007, REQ-008 |
| organization_relations | valid_from | date | 길이·NULL 허용은 원문 미지정; DTO required는 기술 제안 | — | 없음 | 이전 관계·적용기간 보존(REQ-007) | REQ-005, REQ-006, REQ-007, REQ-008 |
| organization_relations | valid_to | date | 길이·NULL 허용은 원문 미지정; DTO required는 기술 제안 | — | 없음 | 이전 관계·적용기간 보존(REQ-007) | REQ-005, REQ-006, REQ-007, REQ-008 |

## Field Mutability Matrix

| Entity | Field | 표시명 | source-of-truth | mutable states | readonly states | editable screen | API operationId/path | validation | canonical_id |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| users | user_id | 사용자 식별정보 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 기존 PK 변경 불가; 신규 식별 또는 저장 대상 선택에만 사용 | 선택 후 식별자 유지; identity rename API 없음 | SCR-USERS | searchUsers / updateUserSettings | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-001, REQ-002, REQ-003, REQ-004 |
| users | employee_number | 교번 | KORUS 원천; 앱에서 readonly | 없음: 앱 원천 수정 금지 | 항상 readonly | N/A: SCR-USERS 표시 전용 | searchUsers (조회만) | REQ-004 원천 필드 전송 거부 | REQ-001, REQ-002, REQ-003, REQ-004 |
| users | person_name | 성명 | KORUS 원천; 앱에서 readonly | 없음: 앱 원천 수정 금지 | 항상 readonly | N/A: SCR-USERS 표시 전용 | searchUsers (조회만) | REQ-004 원천 필드 전송 거부 | REQ-001, REQ-002, REQ-003, REQ-004 |
| users | organization_code | 소속 | KORUS 원천; 앱에서 readonly | 없음: 앱 원천 수정 금지 | 항상 readonly | N/A: SCR-USERS 표시 전용 | searchUsers (조회만) | REQ-004 원천 필드 전송 거부 | REQ-001, REQ-002, REQ-003, REQ-004 |
| users | job_grade | 직급 | KORUS 원천; 앱에서 readonly | 없음: 앱 원천 수정 금지 | 항상 readonly | N/A: SCR-USERS 표시 전용 | searchUsers (조회만) | REQ-004 원천 필드 전송 거부 | REQ-001, REQ-002, REQ-003, REQ-004 |
| users | employment_status | 재직상태 | KORUS 원천; 앱에서 readonly | 없음: 앱 원천 수정 금지 | 항상 readonly | N/A: SCR-USERS 표시 전용 | searchUsers (조회만) | REQ-004 원천 필드 전송 거부 | REQ-001, REQ-002, REQ-003, REQ-004 |
| users | business_roles | 역할 | user_role_assignments + roles의 현재 적용 역할 projection | 직접 편집·물리 저장 없음; 역할 부여 명령 결과로 재계산 | 항상 projection readonly | SCR-USER-ROLES에서 원본 부여만 변경 | searchUsers (projection 조회) / saveUserRoleAssignment (원본 변경) | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-001, REQ-002, REQ-003, REQ-004 |
| users | enabled | 사용여부 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 승인된 관리 접근에서 소유 저장 API의 로컬 값 편집 | 실효 관리 접근 거부 시 변경 불가; 추가 상태·기간 정책은 미정 | SCR-USERS | searchUsers / updateUserSettings | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-001, REQ-002, REQ-003, REQ-004 |
| users | position | 보직 | KORUS 원천; 앱에서 readonly | 없음: 앱 원천 수정 금지 | 항상 readonly | N/A: SCR-USERS 표시 전용 | searchUsers (조회만) | REQ-004 원천 필드 전송 거부 | REQ-001, REQ-002, REQ-003, REQ-004 |
| users | retired_on | 퇴직일자 | KORUS 원천; 앱에서 readonly | 없음: 앱 원천 수정 금지 | 항상 readonly | N/A: SCR-USERS 표시 전용 | searchUsers (조회만) | REQ-004 원천 필드 전송 거부 | REQ-001, REQ-002, REQ-003, REQ-004 |
| users | last_synced_at | 최종 동기화일시 | KORUS 원천; 앱에서 readonly | 없음: 앱 원천 수정 금지 | 항상 readonly | N/A: SCR-USERS 표시 전용 | searchUsers (조회만) | REQ-004 원천 필드 전송 거부 | REQ-001, REQ-002, REQ-003, REQ-004 |
| organizations | organization_code | 조직코드 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 없음: 현재 계약은 조직 원천 정의의 수정 API를 제공하지 않음 | 조회·대상 선택 전용; 원천 소유권 OQ-ORG-001 | N/A: SCR-ORGANIZATIONS 조회 전용 | getOrganizationRelations | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-005, REQ-006, REQ-007, REQ-008 |
| organizations | organization_type | 조직구분 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 없음: 현재 계약은 조직 원천 정의의 수정 API를 제공하지 않음 | 조회·대상 선택 전용; 원천 소유권 OQ-ORG-001 | N/A: SCR-ORGANIZATIONS 조회 전용 | getOrganizationRelations | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-005, REQ-006, REQ-007, REQ-008 |
| roles | role_id | 역할 식별정보 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 기존 PK 변경 불가; 신규 식별 또는 저장 대상 선택에만 사용 | 선택 후 식별자 유지; identity rename API 없음 | SCR-ROLES | listRoles / saveRole | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-009, REQ-010, REQ-011 |
| roles | purpose | 목적 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 승인된 관리 접근에서 소유 저장 API의 로컬 값 편집 | 실효 관리 접근 거부 시 변경 불가; 추가 상태·기간 정책은 미정 | SCR-ROLES | listRoles / saveRole | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-009, REQ-010, REQ-011 |
| roles | grant_criteria | 부여 기준 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 승인된 관리 접근에서 소유 저장 API의 로컬 값 편집 | 실효 관리 접근 거부 시 변경 불가; 추가 상태·기간 정책은 미정 | SCR-ROLES | listRoles / saveRole | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-009, REQ-010, REQ-011 |
| roles | default_data_scope | 데이터 범위 기본값 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 승인된 관리 접근에서 소유 저장 API의 로컬 값 편집 | 실효 관리 접근 거부 시 변경 불가; 추가 상태·기간 정책은 미정 | SCR-ROLES | listRoles / saveRole | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-009, REQ-010, REQ-011 |
| user_role_assignments | assignment_id | 역할 부여 식별정보 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 기존 PK 변경 불가; 신규 식별 또는 저장 대상 선택에만 사용 | 선택 후 식별자 유지; identity rename API 없음 | SCR-USER-ROLES | listUserRoleAssignments / saveUserRoleAssignment | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-012, REQ-013, REQ-014, REQ-015 |
| user_role_assignments | user_id | 대상 사용자 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 승인된 관리 접근에서 소유 저장 API의 로컬 값 편집 | 실효 관리 접근 거부 시 변경 불가; 추가 상태·기간 정책은 미정 | SCR-USER-ROLES | listUserRoleAssignments / saveUserRoleAssignment | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-012, REQ-013, REQ-014, REQ-015 |
| user_role_assignments | role_id | 역할 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 승인된 관리 접근에서 소유 저장 API의 로컬 값 편집 | 실효 관리 접근 거부 시 변경 불가; 추가 상태·기간 정책은 미정 | SCR-USER-ROLES | listUserRoleAssignments / saveUserRoleAssignment | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-012, REQ-013, REQ-014, REQ-015 |
| user_role_assignments | valid_from | 유효 시작일 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 승인된 관리 접근에서 소유 저장 API의 로컬 값 편집 | 실효 관리 접근 거부 시 변경 불가; 추가 상태·기간 정책은 미정 | SCR-USER-ROLES | listUserRoleAssignments / saveUserRoleAssignment | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-012, REQ-013, REQ-014, REQ-015 |
| user_role_assignments | valid_to | 유효 종료일 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 승인된 관리 접근에서 소유 저장 API의 로컬 값 편집 | 실효 관리 접근 거부 시 변경 불가; 추가 상태·기간 정책은 미정 | SCR-USER-ROLES | listUserRoleAssignments / saveUserRoleAssignment | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-012, REQ-013, REQ-014, REQ-015 |
| user_role_assignments | approver_id | 승인자 | 명시 승인자 입력/선택; 신원 확인 OQ-ROLE-001 | 승인된 관리 접근에서 소유 저장 API의 로컬 값 편집 | 실효 관리 접근 거부 시 변경 불가; 추가 상태·기간 정책은 미정 | SCR-USER-ROLES | listUserRoleAssignments / saveUserRoleAssignment | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-012, REQ-013, REQ-014, REQ-015 |
| user_role_assignments | origin | 보직 기반·수동 구분 | 발생 출처; 보직 연결 기준 OQ-ROLE-001 | 사용자 직접 편집 없음; backend가 신규 수동 부여 출처를 기록하고 기존 출처를 보존 | 폼에서 항상 readonly | N/A: SCR-USER-ROLES 표시 전용 | listUserRoleAssignments (조회) / saveUserRoleAssignment (서버 출처 기록) | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-012, REQ-013, REQ-014, REQ-015 |
| user_role_assignments | action | 부여·변경·회수 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 승인된 관리 접근에서 소유 저장 API의 로컬 값 편집 | 실효 관리 접근 거부 시 변경 불가; 추가 상태·기간 정책은 미정 | SCR-USER-ROLES | listUserRoleAssignments / saveUserRoleAssignment | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-012, REQ-013, REQ-014, REQ-015 |
| menu_permissions | permission_id | 설정 식별정보 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 기존 PK 변경 불가; 신규 식별 또는 저장 대상 선택에만 사용 | 선택 후 식별자 유지; identity rename API 없음 | SCR-MENU-PERMISSIONS | listMenuPermissions / saveMenuPermission | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-016, REQ-017, REQ-018 |
| menu_permissions | role_id | 역할 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 승인된 관리 접근에서 소유 저장 API의 로컬 값 편집 | 실효 관리 접근 거부 시 변경 불가; 추가 상태·기간 정책은 미정 | SCR-MENU-PERMISSIONS | listMenuPermissions / saveMenuPermission | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-016, REQ-017, REQ-018 |
| menu_permissions | organization_code | 조직 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 승인된 관리 접근에서 소유 저장 API의 로컬 값 편집 | 실효 관리 접근 거부 시 변경 불가; 추가 상태·기간 정책은 미정 | SCR-MENU-PERMISSIONS | listMenuPermissions / saveMenuPermission | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-016, REQ-017, REQ-018 |
| menu_permissions | user_id | 사용자 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 승인된 관리 접근에서 소유 저장 API의 로컬 값 편집 | 실효 관리 접근 거부 시 변경 불가; 추가 상태·기간 정책은 미정 | SCR-MENU-PERMISSIONS | listMenuPermissions / saveMenuPermission | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-016, REQ-017, REQ-018 |
| menu_permissions | menu_id | 메뉴 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 승인된 관리 접근에서 소유 저장 API의 로컬 값 편집 | 실효 관리 접근 거부 시 변경 불가; 추가 상태·기간 정책은 미정 | SCR-MENU-PERMISSIONS | listMenuPermissions / saveMenuPermission | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-016, REQ-017, REQ-018 |
| menu_permissions | allowed | 접근 허용 여부 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 승인된 관리 접근에서 소유 저장 API의 로컬 값 편집 | 실효 관리 접근 거부 시 변경 불가; 추가 상태·기간 정책은 미정 | SCR-MENU-PERMISSIONS | listMenuPermissions / saveMenuPermission | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-016, REQ-017, REQ-018 |
| menus | menu_id | 메뉴 식별정보 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 기존 PK 변경 불가; 신규 식별 또는 저장 대상 선택에만 사용 | 선택 후 식별자 유지; identity rename API 없음 | SCR-MENU-STRUCTURE | getMenuStructure / saveMenuStructure | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-019, REQ-020, REQ-021 |
| menus | parent_menu_id | 부모메뉴 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 승인된 관리 접근에서 소유 저장 API의 로컬 값 편집 | 실효 관리 접근 거부 시 변경 불가; 추가 상태·기간 정책은 미정 | SCR-MENU-STRUCTURE | getMenuStructure / saveMenuStructure | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-019, REQ-020, REQ-021 |
| menus | level | 계층단계 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 승인된 관리 접근에서 소유 저장 API의 로컬 값 편집 | 실효 관리 접근 거부 시 변경 불가; 추가 상태·기간 정책은 미정 | SCR-MENU-STRUCTURE | getMenuStructure / saveMenuStructure | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-019, REQ-020, REQ-021 |
| menus | display_order | 표시순서 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 승인된 관리 접근에서 소유 저장 API의 로컬 값 편집 | 실효 관리 접근 거부 시 변경 불가; 추가 상태·기간 정책은 미정 | SCR-MENU-STRUCTURE | getMenuStructure / saveMenuStructure | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-019, REQ-020, REQ-021 |
| menu_execution_info | menu_id | 메뉴 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 기존 PK 변경 불가; 신규 식별 또는 저장 대상 선택에만 사용 | 선택 후 식별자 유지; identity rename API 없음 | SCR-MENU-INFO | listMenuExecutionInfo / saveMenuExecutionInfo | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-022, REQ-023, REQ-024 |
| menu_execution_info | screen_id | 화면ID | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 승인된 관리 접근에서 소유 저장 API의 로컬 값 편집 | 실효 관리 접근 거부 시 변경 불가; 추가 상태·기간 정책은 미정 | SCR-MENU-INFO | listMenuExecutionInfo / saveMenuExecutionInfo | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-022, REQ-023, REQ-024 |
| menu_execution_info | url | URL | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 승인된 관리 접근에서 소유 저장 API의 로컬 값 편집 | 실효 관리 접근 거부 시 변경 불가; 추가 상태·기간 정책은 미정 | SCR-MENU-INFO | listMenuExecutionInfo / saveMenuExecutionInfo | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-022, REQ-023, REQ-024 |
| code_groups | group_id | 그룹ID | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 기존 PK 변경 불가; 신규 식별 또는 저장 대상 선택에만 사용 | 선택 후 식별자 유지; identity rename API 없음 | SCR-CODE-GROUPS | listCodeGroups / saveCodeGroup | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-025, REQ-026, REQ-027 |
| code_groups | name | 명칭 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 승인된 관리 접근에서 소유 저장 API의 로컬 값 편집 | 실효 관리 접근 거부 시 변경 불가; 추가 상태·기간 정책은 미정 | SCR-CODE-GROUPS | listCodeGroups / saveCodeGroup | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-025, REQ-026, REQ-027 |
| code_groups | description | 설명 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 승인된 관리 접근에서 소유 저장 API의 로컬 값 편집 | 실효 관리 접근 거부 시 변경 불가; 추가 상태·기간 정책은 미정 | SCR-CODE-GROUPS | listCodeGroups / saveCodeGroup | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-025, REQ-026, REQ-027 |
| code_groups | managing_department | 관리부서 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 승인된 관리 접근에서 소유 저장 API의 로컬 값 편집 | 실효 관리 접근 거부 시 변경 불가; 추가 상태·기간 정책은 미정 | SCR-CODE-GROUPS | listCodeGroups / saveCodeGroup | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-025, REQ-026, REQ-027 |
| detail_codes | group_id | 코드그룹 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 기존 PK 변경 불가; 신규 식별 또는 저장 대상 선택에만 사용 | 선택 후 식별자 유지; identity rename API 없음 | SCR-DETAIL-CODES | listDetailCodes / saveDetailCode | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-028, REQ-029, REQ-030, REQ-031 |
| detail_codes | code | 상세코드 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 기존 PK 변경 불가; 신규 식별 또는 저장 대상 선택에만 사용 | 선택 후 식별자 유지; identity rename API 없음 | SCR-DETAIL-CODES | listDetailCodes / saveDetailCode | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-028, REQ-029, REQ-030, REQ-031 |
| detail_codes | sort_order | 정렬순서 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 승인된 관리 접근에서 소유 저장 API의 로컬 값 편집 | 실효 관리 접근 거부 시 변경 불가; 추가 상태·기간 정책은 미정 | SCR-DETAIL-CODES | listDetailCodes / saveDetailCode | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-028, REQ-029, REQ-030, REQ-031 |
| detail_codes | parent_code | 상위코드 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 승인된 관리 접근에서 소유 저장 API의 로컬 값 편집 | 실효 관리 접근 거부 시 변경 불가; 추가 상태·기간 정책은 미정 | SCR-DETAIL-CODES | listDetailCodes / saveDetailCode | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-028, REQ-029, REQ-030, REQ-031 |
| organization_relations | relation_id | 조직관계 식별정보 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 기존 PK 변경 불가; 신규 식별 또는 저장 대상 선택에만 사용 | 선택 후 식별자 유지; identity rename API 없음 | SCR-ORGANIZATIONS | listOrganizationRelationHistory / saveOrganizationRelation | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-005, REQ-006, REQ-007, REQ-008 |
| organization_relations | organization_code | 조직코드 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 승인된 관리 접근에서 소유 저장 API의 로컬 값 편집 | 실효 관리 접근 거부 시 변경 불가; 추가 상태·기간 정책은 미정 | SCR-ORGANIZATIONS | listOrganizationRelationHistory / saveOrganizationRelation | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-005, REQ-006, REQ-007, REQ-008 |
| organization_relations | parent_code | 상위조직 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 승인된 관리 접근에서 소유 저장 API의 로컬 값 편집 | 실효 관리 접근 거부 시 변경 불가; 추가 상태·기간 정책은 미정 | SCR-ORGANIZATIONS | listOrganizationRelationHistory / saveOrganizationRelation | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-005, REQ-006, REQ-007, REQ-008 |
| organization_relations | valid_from | 적용 시작일 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 승인된 관리 접근에서 소유 저장 API의 로컬 값 편집 | 실효 관리 접근 거부 시 변경 불가; 추가 상태·기간 정책은 미정 | SCR-ORGANIZATIONS | listOrganizationRelationHistory / saveOrganizationRelation | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-005, REQ-006, REQ-007, REQ-008 |
| organization_relations | valid_to | 적용 종료일 | 로컬 저장 설계 제안; 조직 원천 소유권은 OQ-ORG-001 | 승인된 관리 접근에서 소유 저장 API의 로컬 값 편집 | 실효 관리 접근 거부 시 변경 불가; 추가 상태·기간 정책은 미정 | SCR-ORGANIZATIONS | listOrganizationRelationHistory / saveOrganizationRelation | Validation Rule Matrix; 허용 requestBody 필드만 전송 | REQ-005, REQ-006, REQ-007, REQ-008 |

### 기술 식별자·projection·대상 null 표현

- users.user_id는 조회 응답에서 선택 행과 함께 보존하여 UserInput.user_id로 전달한다. 사용자가 타이핑하거나 PK를 바꾸지 않는다.
- 신규 부여 action=부여의 assignment_id와 미설정 대상·메뉴의 permission_id는 frontend가 내부 UUID를 생성하고 요청 재시도까지 유지한다는 기술 제안이다. 변경·회수/기존 설정 수정은 응답의 ID를 재사용한다. 새 business 필드나 seed가 아니다. relation_id는 backend의 새 조직관계 기록 식별자다.
- 메뉴 권한의 선택되지 않은 role_id/organization_code/user_id는 DB NULL·응답 null로 표현하고 입력은 생략/null을 허용한다. 최소 한 대상 선택은 기술 validation이며 여러 대상 조합·권한 우선순위·대상 UNIQUE 키는 OQ-AUTH-001 승인 전 확정하지 않는다.
- user_role_assignments.is_current는 REQ-015를 관찰하는 계산 projection이며 물리 컬럼이 아니다. business_roles는 해당 현재 적용 역할을 읽어 구성하며 직접 저장하지 않는다.

## Relationship Contract

| 관계 | cardinality·키 | 삭제·고아·이력 의도 | canonical_id |
| --- | --- | --- | --- |
| organizations → organization_relations | 조직 1 : 관계 N; organization_code와 parent_code는 조직 참조 | 이전 관계와 기간 보존. 한 시점의 단일 부모·순환 제한은 OQ-ORG-001이며 확정 금지. | REQ-005, REQ-006, REQ-007 |
| users/roles → user_role_assignments | 사용자 1 : 부여 N, 역할 1 : 부여 N; approver_id 별도 참조 | 역할 정의 수정은 부여내역 불변. 회수 명령에도 대상·기간·승인자를 기록; 전체 이력 retention은 미정. | REQ-010, REQ-011, REQ-012, REQ-013 |
| menus → menus | 부모 1 : 자식 N; parent_menu_id | 다른 부모의 display_order 불변. 루트 표기·순환 검증은 확인 필요. | REQ-019, REQ-020, REQ-021 |
| menus → menu_execution_info | 메뉴 1 : 실행정보 0..1 제안 | 화면ID·URL 변경은 메뉴 권한 불변. 사용 가능 판정은 OQ-UI-003. | REQ-022, REQ-023, REQ-024 |
| roles/organizations/users/menus → menu_permissions | 대상과 메뉴별 허용 설정; 복합 대상 조합과 UNIQUE 키는 OQ-AUTH-001 | 허용 결과에 따른 숨김·서버 차단은 확정. 합성 우선순위는 미정. | REQ-016, REQ-017, REQ-018 |
| code_groups → detail_codes | 그룹 1 : 코드 N; (group_id, code) 복합키 제안 | 그룹 수정은 상세값 불변. parent_code는 동일 그룹 자기 참조 제안; 순환 거부 정책은 미정. | REQ-025, REQ-026, REQ-027, REQ-028, REQ-031 |

## Validation Rule Matrix

| canonical_id | 필드·작업 | 규칙 | 기대 결과 | 필수 시험 |
| --- | --- | --- | --- | --- |
| REQ-001 | searchUsers | 교번·성명·소속·직급·재직상태·역할·사용여부 중 하나 이상의 조건으로 검색하면 조건에 맞는 사용자만 표시된다. | CMN-FR-001/AC-01: 교번·성명·소속·직급·재직상태·역할·사용여부 중 하나 이상의 조건으로 검색하면 조건에 맞는 사용자만 표시된다. | backend/src/test/java/org/example/oct19/UserContractTest.java; 미정 정책은 승인 후 구체화 |
| REQ-002 | searchUsers | 사용자 목록에 보직·퇴직일자·최종 동기화일시를 포함한 명시 항목이 표시된다. | CMN-FR-001/AC-02: 사용자 목록에 보직·퇴직일자·최종 동기화일시를 포함한 명시 항목이 표시된다. | backend/src/test/java/org/example/oct19/UserContractTest.java; 미정 정책은 승인 후 구체화 |
| REQ-003 | updateUserSettings | 시스템 사용여부 또는 업무 역할을 변경하고 다시 조회하면 변경값이 유지된다. | CMN-FR-001/AC-03: 시스템 사용여부 또는 업무 역할을 변경하고 다시 조회하면 변경값이 유지된다. | backend/src/test/java/org/example/oct19/UserContractTest.java; 미정 정책은 승인 후 구체화 |
| REQ-004 | updateUserSettings | KORUS 원천 인사정보는 화면과 서버 처리에서 변경할 수 없다. | CMN-FR-001/AC-04: KORUS 원천 인사정보는 화면과 서버 처리에서 변경할 수 없다. | backend/src/test/java/org/example/oct19/UserContractTest.java; 미정 정책은 승인 후 구체화 |
| REQ-005 | getOrganizationRelations | 조직코드로 조회하면 해당 조직의 조직구분과 현재 상위·하위 관계가 표시된다. | CMN-FR-002/AC-01: 조직코드로 조회하면 해당 조직의 조직구분과 현재 상위·하위 관계가 표시된다. | backend/src/test/java/org/example/oct19/OrganizationContractTest.java; 미정 정책은 승인 후 구체화 |
| REQ-006 | saveOrganizationRelation | 상위조직 변경 시 적용 시작일·종료일과 변경된 상위조직이 함께 저장된다. | CMN-FR-002/AC-02: 상위조직 변경 시 적용 시작일·종료일과 변경된 상위조직이 함께 저장된다. | backend/src/test/java/org/example/oct19/OrganizationContractTest.java; 미정 정책은 승인 후 구체화 |
| REQ-007 | saveOrganizationRelation | 변경 이후에도 변경 전 조직관계와 적용기간을 이력으로 조회할 수 있다. | CMN-FR-002/AC-03: 변경 이후에도 변경 전 조직관계와 적용기간을 이력으로 조회할 수 있다. | backend/src/test/java/org/example/oct19/OrganizationContractTest.java; 미정 정책은 승인 후 구체화 |
| REQ-008 | saveOrganizationRelation | 조직구조 변경이 사용자의 소속·보직 또는 역할정보를 직접 변경하지 않는다. | CMN-FR-002/AC-04: 조직구조 변경이 사용자의 소속·보직 또는 역할정보를 직접 변경하지 않는다. | backend/src/test/java/org/example/oct19/OrganizationContractTest.java; 미정 정책은 승인 후 구체화 |
| REQ-009 | saveRole | 역할별 목적·부여 기준·데이터 범위 기본값을 저장하고 재조회할 수 있다. | CMN-FR-005/AC-01: 역할별 목적·부여 기준·데이터 범위 기본값을 저장하고 재조회할 수 있다. | backend/src/test/java/org/example/oct19/RoleContractTest.java; 미정 정책은 승인 후 구체화 |
| REQ-010 | saveRole | 정의된 역할은 서로 다른 역할 식별정보로 구분된다. | CMN-FR-005/AC-02: 정의된 역할은 서로 다른 역할 식별정보로 구분된다. | backend/src/test/java/org/example/oct19/RoleContractTest.java; 미정 정책은 승인 후 구체화 |
| REQ-011 | saveRole | 역할 정의 변경만으로 특정 사용자의 역할 부여내역이 직접 변경되지 않는다. | CMN-FR-005/AC-03: 역할 정의 변경만으로 특정 사용자의 역할 부여내역이 직접 변경되지 않는다. | backend/src/test/java/org/example/oct19/RoleContractTest.java; 미정 정책은 승인 후 구체화 |
| REQ-012 | saveUserRoleAssignment | 한 사용자에게 둘 이상의 역할을 서로 다른 유효기간으로 부여할 수 있다. | CMN-FR-006/AC-01: 한 사용자에게 둘 이상의 역할을 서로 다른 유효기간으로 부여할 수 있다. | backend/src/test/java/org/example/oct19/UserRoleAssignmentContractTest.java; 미정 정책은 승인 후 구체화 |
| REQ-013 | saveUserRoleAssignment | 역할을 부여·변경·회수하면 대상 사용자·역할·유효기간·승인자가 함께 저장된다. | CMN-FR-006/AC-02: 역할을 부여·변경·회수하면 대상 사용자·역할·유효기간·승인자가 함께 저장된다. | backend/src/test/java/org/example/oct19/UserRoleAssignmentContractTest.java; 미정 정책은 승인 후 구체화 |
| REQ-014 | listUserRoleAssignments | 보직 기반 역할과 수동 역할을 조회 결과에서 구분할 수 있다. | CMN-FR-006/AC-03: 보직 기반 역할과 수동 역할을 조회 결과에서 구분할 수 있다. | backend/src/test/java/org/example/oct19/UserRoleAssignmentContractTest.java; 미정 정책은 승인 후 구체화 |
| REQ-015 | saveUserRoleAssignment | 유효기간이 종료된 역할은 현재 적용 역할로 판정되지 않는다. | CMN-FR-006/AC-04: 유효기간이 종료된 역할은 현재 적용 역할로 판정되지 않는다. | backend/src/test/java/org/example/oct19/UserRoleAssignmentContractTest.java; 미정 정책은 승인 후 구체화 |
| REQ-016 | saveMenuPermission | 역할·조직·사용자와 메뉴를 선택해 접근 허용 여부를 저장하고 재조회할 수 있다. | CMN-FR-007/AC-01: 역할·조직·사용자와 메뉴를 선택해 접근 허용 여부를 저장하고 재조회할 수 있다. | backend/src/test/java/org/example/oct19/MenuPermissionContractTest.java; 미정 정책은 승인 후 구체화 |
| REQ-017 | saveMenuPermission | 접근이 허용되지 않은 메뉴는 사용자 메뉴에 표시되지 않는다. | CMN-FR-007/AC-02: 접근이 허용되지 않은 메뉴는 사용자 메뉴에 표시되지 않는다. | backend/src/test/java/org/example/oct19/MenuPermissionContractTest.java; 미정 정책은 승인 후 구체화 |
| REQ-018 | saveMenuPermission | 접근이 허용되지 않은 사용자가 URL로 직접 요청해도 서버에서 차단된다. | CMN-FR-007/AC-03: 접근이 허용되지 않은 사용자가 URL로 직접 요청해도 서버에서 차단된다. | backend/src/test/java/org/example/oct19/MenuPermissionContractTest.java; 미정 정책은 승인 후 구체화 |
| REQ-019 | saveMenuStructure | 메뉴별 부모메뉴와 계층단계를 저장하고 계층구조로 재조회할 수 있다. | CMN-FR-013/AC-01: 메뉴별 부모메뉴와 계층단계를 저장하고 계층구조로 재조회할 수 있다. | backend/src/test/java/org/example/oct19/MenuStructureContractTest.java; 미정 정책은 승인 후 구체화 |
| REQ-020 | saveMenuStructure | 동일 계층의 표시순서를 변경하면 사용자 메뉴에 변경된 순서가 적용된다. | CMN-FR-013/AC-02: 동일 계층의 표시순서를 변경하면 사용자 메뉴에 변경된 순서가 적용된다. | backend/src/test/java/org/example/oct19/MenuStructureContractTest.java; 미정 정책은 승인 후 구체화 |
| REQ-021 | saveMenuStructure | 한 계층의 순서 변경이 다른 부모메뉴 아래 순서를 변경하지 않는다. | CMN-FR-013/AC-03: 한 계층의 순서 변경이 다른 부모메뉴 아래 순서를 변경하지 않는다. | backend/src/test/java/org/example/oct19/MenuStructureContractTest.java; 미정 정책은 승인 후 구체화 |
| REQ-022 | saveMenuExecutionInfo | 메뉴의 실행정보를 저장하고 동일한 값으로 재조회할 수 있다. | CMN-FR-014/AC-01: 메뉴의 실행정보를 저장하고 동일한 값으로 재조회할 수 있다. | backend/src/test/java/org/example/oct19/MenuExecutionInfoContractTest.java; 미정 정책은 승인 후 구체화 |
| REQ-023 | saveMenuExecutionInfo | 사용 가능한 메뉴를 선택하면 연결된 화면ID와 URL의 화면이 실행된다. | CMN-FR-014/AC-02: 사용 가능한 메뉴를 선택하면 연결된 화면ID와 URL의 화면이 실행된다. | backend/src/test/java/org/example/oct19/MenuExecutionInfoContractTest.java; 미정 정책은 승인 후 구체화 |
| REQ-024 | saveMenuExecutionInfo | 메뉴 실행정보 변경이 메뉴 권한 설정을 변경하지 않는다. | CMN-FR-014/AC-03: 메뉴 실행정보 변경이 메뉴 권한 설정을 변경하지 않는다. | backend/src/test/java/org/example/oct19/MenuExecutionInfoContractTest.java; 미정 정책은 승인 후 구체화 |
| REQ-025 | saveCodeGroup | 그룹ID·명칭·설명·관리부서를 저장하고 재조회할 수 있다. | CMN-FR-016/AC-01: 그룹ID·명칭·설명·관리부서를 저장하고 재조회할 수 있다. | backend/src/test/java/org/example/oct19/CodeGroupContractTest.java; 미정 정책은 승인 후 구체화 |
| REQ-026 | listCodeGroups | 서로 다른 그룹ID의 코드그룹이 독립적으로 조회된다. | CMN-FR-016/AC-02: 서로 다른 그룹ID의 코드그룹이 독립적으로 조회된다. | backend/src/test/java/org/example/oct19/CodeGroupContractTest.java; 미정 정책은 승인 후 구체화 |
| REQ-027 | saveCodeGroup | 코드그룹 수정이 해당 그룹의 상세코드 값을 임의로 변경하지 않는다. | CMN-FR-016/AC-03: 코드그룹 수정이 해당 그룹의 상세코드 값을 임의로 변경하지 않는다. | backend/src/test/java/org/example/oct19/CodeGroupContractTest.java; 미정 정책은 승인 후 구체화 |
| REQ-028 | listDetailCodes | 코드그룹을 선택하면 해당 그룹의 상세코드만 조회된다. | CMN-FR-017/AC-01: 코드그룹을 선택하면 해당 그룹의 상세코드만 조회된다. | backend/src/test/java/org/example/oct19/DetailCodeContractTest.java; 미정 정책은 승인 후 구체화 |
| REQ-029 | saveDetailCode | 상세코드의 명시 필드를 저장하고 재조회할 수 있다. | CMN-FR-017/AC-02: 상세코드의 명시 필드를 저장하고 재조회할 수 있다. | backend/src/test/java/org/example/oct19/DetailCodeContractTest.java; 미정 정책은 승인 후 구체화 |
| REQ-030 | saveDetailCode | 정렬순서를 변경하면 화면 선택값이 같은 순서로 제공된다. | CMN-FR-017/AC-03: 정렬순서를 변경하면 화면 선택값이 같은 순서로 제공된다. | backend/src/test/java/org/example/oct19/DetailCodeContractTest.java; 미정 정책은 승인 후 구체화 |
| REQ-031 | listDetailCodes | 상위코드를 지정한 코드는 계층관계로 식별할 수 있다. | CMN-FR-017/AC-04: 상위코드를 지정한 코드는 계층관계로 식별할 수 있다. | backend/src/test/java/org/example/oct19/DetailCodeContractTest.java; 미정 정책은 승인 후 구체화 |

## State Transition Contract

REQ-015는 종료된 유효기간 → 현재 역할에서 제외를 확정한다. role assignment의 부여·변경·회수는 `action` 명령값이지 승인된 lifecycle enum이 아니다. active/revoked 등 새 상태를 만들지 않는다. 회수 전후·기간 경계·보직 종료 시점·접속 차단은 OQ-ROLE-001/OQ-DATA-002. menu `enabled` 상태값은 원문에 없어 추가하지 않는다.

## Seed Data Contract

아래는 제안 fixture의 구조만 정의한다. 실제 인물·로그인 비밀번호·토큰을 넣지 않는다. 식별자는 테스트가 생성하고 scope/권한을 승인받은 별도 검증 환경에만 넣는다. 초기 administrator seed를 영구 관리자 전용 정책으로 삼지 않는다. 원문에 승인된 고정 로그인 자격증명은 없다.

| seed ID | entity/table | minimal fields | 목적 | 소비 acceptance/test |
| --- | --- | --- | --- | --- |
| FX-US1 | users | employee_number, person_name, organization_code, job_grade, employment_status, business_roles, enabled, position, retired_on, last_synced_at | 검색에 맞는/맞지 않는 사용자와 원천 snapshot | REQ-001, REQ-002, REQ-003, REQ-004 / US1ContractTest |
| FX-US2 | organizations | organization_code, organization_type, parent_code, child_codes, valid_from, valid_to | 현재/변경 전 기간 관계와 사용자 snapshot | REQ-005, REQ-006, REQ-007, REQ-008 / US2ContractTest |
| FX-US3 | roles | role_id, purpose, grant_criteria, default_data_scope | 서로 다른 식별정보·참조 행 snapshot | REQ-009, REQ-010, REQ-011 / US3ContractTest |
| FX-US4 | user_role_assignments | assignment_id, user_id, role_id, valid_from, valid_to, approver_id, origin | 다중 기간·출처·승인자를 가진 역할 부여 | REQ-012, REQ-013, REQ-014, REQ-015 / US4ContractTest |
| FX-US5 | menu_permissions | role_id, organization_code, user_id, menu_id, allowed | 서로 다른 식별정보·참조 행 snapshot | REQ-016, REQ-017, REQ-018 / US5ContractTest |
| FX-US6 | menus | menu_id, parent_menu_id, level, display_order | 서로 다른 식별정보·참조 행 snapshot | REQ-019, REQ-020, REQ-021 / US6ContractTest |
| FX-US7 | menu_execution_info | menu_id, screen_id, url | 서로 다른 식별정보·참조 행 snapshot | REQ-022, REQ-023, REQ-024 / US7ContractTest |
| FX-US8 | code_groups | group_id, name, description, managing_department | 서로 다른 식별정보·참조 행 snapshot | REQ-025, REQ-026, REQ-027 / US8ContractTest |
| FX-US9 | detail_codes | group_id, code, sort_order, parent_code | 서로 다른 식별정보·참조 행 snapshot | REQ-028, REQ-029, REQ-030, REQ-031 / US9ContractTest |

## Screen/API Reference

| entity/table | screen_id | route | operationId/path | task slice | canonical_id |
| --- | --- | --- | --- | --- | --- |
| users | SCR-USERS | /admin/users | searchUsers / updateUserSettings | US1 | REQ-001, REQ-002, REQ-003, REQ-004 |
| organizations | SCR-ORGANIZATIONS | /admin/organizations | getOrganizationRelations / saveOrganizationRelation | US2 | REQ-005, REQ-006, REQ-007, REQ-008 |
| roles | SCR-ROLES | /admin/roles | listRoles / saveRole | US3 | REQ-009, REQ-010, REQ-011 |
| user_role_assignments | SCR-USER-ROLES | /admin/user-roles | listUserRoleAssignments / saveUserRoleAssignment | US4 | REQ-012, REQ-013, REQ-014, REQ-015 |
| menu_permissions | SCR-MENU-PERMISSIONS | /admin/menu-permissions | listMenuPermissions / saveMenuPermission | US5 | REQ-016, REQ-017, REQ-018 |
| menus | SCR-MENU-STRUCTURE | /admin/menu-structure | getMenuStructure / saveMenuStructure | US6 | REQ-019, REQ-020, REQ-021 |
| menu_execution_info | SCR-MENU-INFO | /admin/menu-info | listMenuExecutionInfo / saveMenuExecutionInfo | US7 | REQ-022, REQ-023, REQ-024 |
| code_groups | SCR-CODE-GROUPS | /admin/code-groups | listCodeGroups / saveCodeGroup | US8 | REQ-025, REQ-026, REQ-027 |
| detail_codes | SCR-DETAIL-CODES | /admin/detail-codes | listDetailCodes / saveDetailCode | US9 | REQ-028, REQ-029, REQ-030, REQ-031 |
| organization_relations | SCR-ORGANIZATIONS | /admin/organizations | listOrganizationRelationHistory / saveOrganizationRelation | US2 | REQ-005, REQ-006, REQ-007, REQ-008 |

## Persistence Assumptions

REQ-036/REQ-047: plan.md marker가 정본이며 MyBatis blocking mapper·imperative transaction만 설계한다. JPA/R2DBC 대체를 암묵적으로 하지 않는다. 날짜는 date 설계 제안, 동기화 시각은 timestamptz 제안이며 기간 경계 시간대는 미정. 인사 필드 PUT는 SQL 동적 갱신 대상으로 노출하지 않는다. source에 없는 created_at/updated_at 전역 audit 의무·soft-delete 컬럼은 생성하지 않는다.

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
| REQ-001 | covered | users Entity/Table Contract·Validation Rule Matrix의 REQ-001 (사용자 조건 검색) |
| REQ-002 | covered | users Entity/Table Contract·Validation Rule Matrix의 REQ-002 (사용자 인사정보 목록 표시) |
| REQ-003 | covered | users Entity/Table Contract·Validation Rule Matrix의 REQ-003 (사용여부·업무 역할 변경 유지) |
| REQ-004 | covered | users Entity/Table Contract·Validation Rule Matrix의 REQ-004 (원천 인사정보 변경 거부) |
| REQ-005 | covered | organizations Entity/Table Contract·Validation Rule Matrix의 REQ-005 (조직 현재 관계 조회) |
| REQ-006 | covered | organizations Entity/Table Contract·Validation Rule Matrix의 REQ-006 (상위조직 적용기간 저장) |
| REQ-007 | covered | organizations Entity/Table Contract·Validation Rule Matrix의 REQ-007 (변경 전 조직관계 이력 조회) |
| REQ-008 | covered | organizations Entity/Table Contract·Validation Rule Matrix의 REQ-008 (조직변경의 사용자정보 비간섭) |
| REQ-009 | covered | roles Entity/Table Contract·Validation Rule Matrix의 REQ-009 (역할 정의 저장·재조회) |
| REQ-010 | covered | roles Entity/Table Contract·Validation Rule Matrix의 REQ-010 (역할 식별정보 구분) |
| REQ-011 | covered | roles Entity/Table Contract·Validation Rule Matrix의 REQ-011 (역할 정의와 부여내역 분리) |
| REQ-012 | covered | user_role_assignments Entity/Table Contract·Validation Rule Matrix의 REQ-012 (다중 역할·상이한 기간 부여) |
| REQ-013 | covered | user_role_assignments Entity/Table Contract·Validation Rule Matrix의 REQ-013 (부여·변경·회수 승인자 기록) |
| REQ-014 | covered | user_role_assignments Entity/Table Contract·Validation Rule Matrix의 REQ-014 (역할 발생 출처 구분 조회) |
| REQ-015 | covered | user_role_assignments Entity/Table Contract·Validation Rule Matrix의 REQ-015 (종료된 역할 현재 적용 제외) |
| REQ-016 | covered | menu_permissions Entity/Table Contract·Validation Rule Matrix의 REQ-016 (메뉴 접근 허용 설정 저장) |
| REQ-017 | covered | menu_permissions Entity/Table Contract·Validation Rule Matrix의 REQ-017 (허용되지 않은 메뉴 숨김) |
| REQ-018 | covered | menu_permissions Entity/Table Contract·Validation Rule Matrix의 REQ-018 (비허용 URL 서버 차단) |
| REQ-019 | covered | menus Entity/Table Contract·Validation Rule Matrix의 REQ-019 (부모메뉴·계층 저장) |
| REQ-020 | covered | menus Entity/Table Contract·Validation Rule Matrix의 REQ-020 (동일 계층 표시순서 적용) |
| REQ-021 | covered | menus Entity/Table Contract·Validation Rule Matrix의 REQ-021 (다른 부모 아래 순서 보존) |
| REQ-022 | covered | menu_execution_info Entity/Table Contract·Validation Rule Matrix의 REQ-022 (메뉴 실행정보 저장) |
| REQ-023 | screen-only | 메뉴 실행은 SCR-MENU-INFO의 링크 동작이며 menu_execution_info.screen_id/url을 읽는다. |
| REQ-024 | covered | menu_execution_info Entity/Table Contract·Validation Rule Matrix의 REQ-024 (실행정보 변경의 권한 비간섭) |
| REQ-025 | covered | code_groups Entity/Table Contract·Validation Rule Matrix의 REQ-025 (코드그룹 정의 저장) |
| REQ-026 | covered | code_groups Entity/Table Contract·Validation Rule Matrix의 REQ-026 (그룹ID별 독립 조회) |
| REQ-027 | covered | code_groups Entity/Table Contract·Validation Rule Matrix의 REQ-027 (그룹 수정의 상세코드 비간섭) |
| REQ-028 | covered | detail_codes Entity/Table Contract·Validation Rule Matrix의 REQ-028 (그룹별 상세코드 조회) |
| REQ-029 | covered | detail_codes Entity/Table Contract·Validation Rule Matrix의 REQ-029 (상세코드 필드 저장) |
| REQ-030 | covered | detail_codes Entity/Table Contract·Validation Rule Matrix의 REQ-030 (선택값 정렬순서 반영) |
| REQ-031 | covered | detail_codes Entity/Table Contract·Validation Rule Matrix의 REQ-031 (상위코드 계층 식별) |
| REQ-032 | non-functional | React/TypeScript/Vite 번들 선택은 frontend/package.json의 빌드 의존성 계약이며 REST 입출력이 아니다. |
| REQ-033 | non-functional | Java 17/Spring Boot 선택은 backend/pom.xml과 런타임의 컴파일 계약이다. |
| REQ-034 | non-functional | PostgreSQL 선택은 data-model.md의 매핑·DB 접속 대상이지 신규 업무 API가 아니다. |
| REQ-035 | non-functional | Docker Compose 실행 방식은 infra/docker-compose.yml의 서비스 연결 책임이다. |
| REQ-036 | non-functional | 동기 blocking MyBatis는 plan.md marker의 실행 결정으로 모든 요청 처리 내부에 적용된다. |
| REQ-037 | non-functional | BOM은 frontend/package.json/backend/pom.xml/DB 이미지 버전 검증으로 충족한다. |
| REQ-038 | non-functional | backend 디렉터리 산출 여부는 저장소 파일 검사 대상이다. |
| REQ-039 | non-functional | frontend 디렉터리 산출 여부는 저장소 파일 검사 대상이다. |
| REQ-040 | non-functional | Compose 파일 위치는 infra/docker-compose.yml 파일 존재와 config 검사로 확인한다. |
| REQ-041 | non-functional | REQ-041의 /api/health는 DB 업무 엔티티를 만들지 않는 상태 조회다. |
| REQ-042 | non-functional | 레퍼런스의 sidebar/header/밀도/토큰/반응형은 ui-design.md 공통 시각 설계이며 API를 만들지 않는다. |
| REQ-043 | non-functional | spec.md 기술스택 계약에서 백엔드·프론트엔드 범위를 문서화한다. |
| REQ-044 | non-functional | plan.md Project Structure와 생성 순서가 소유하는 구현 배치 계약이다. |
| REQ-045 | non-functional | tasks.md Setup/Foundational/US 단계가 backend/frontend/infra 작업을 배정한다. |
| REQ-046 | non-functional | ui-design.md 레퍼런스 구현 검수 기준은 화면 공통 스타일 책임이다. |
| REQ-047 | non-functional | plan.md의 exact marker JSON 보존은 문서 정본 검증 대상이다. |
| REQ-048 | data-constraint | data-model.md Entity Registry와 tasks.md migration 작업이 스키마 기준을 충족한다. |
| REQ-049 | non-functional | quickstart.md API/UI 동시 smoke 절차와 seed fixture로 검증한다. |
| REQ-050 | non-functional | 각 build context의 .dockerignore 제외 집합을 infra 검사에서 확인한다. |
