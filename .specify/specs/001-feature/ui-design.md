# UI 설계 — oct19

## 디자인 참조와 적용 경계

REQ-042/REQ-046의 reference 계약은 sidebar/header, table/form density, spacing, color token, responsive behavior를 도메인에 적용하는 것이다. 실제 레퍼런스 파일·URL·색상 값은 제공되지 않았다(OQ-UI-004). 확정 브랜드를 복제했다고 주장하지 않는다. sidebar는 허용 메뉴 탐색, header는 현재 업무 제목, 본문은 도메인별 작업을 담당하는 제안이다. dashboard card를 제품 기능으로 추가하지 않는다. React 18.x + TypeScript + Vite 5.x를 유지한다.

화면별 route/menu_path/화면ID는 원문 미지정이므로 OQ-UI-001의 안정된 설계 제안이다. 역할은 권한 승인된 관리 주체이며 실제 role code·인증·범위는 OQ-AUTH-001이다. 모든 명확화의 추정 응답은 승인으로 보지 않는다. 본문 화면·wireframe·action·trace·브라우저 대상에는 ui-primary만 배정하고 비간섭·API 보안·인프라·seed는 해당 소유 산출물에 남긴다.

## 화면 목록

| 화면ID | archetype | interaction_summary | route | role | state | operationId 또는 path | canonical_id | source_id | 비고 | menu_path | primary_entity |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| SCR-USERS | search/list/detail | 조건 입력·검색 → 목록 선택 → 사용여부 저장; 업무역할은 사용자 역할 관리로 이동 | /admin/users | 관리권한 확인 주체(OQ-AUTH-001) | loading/empty/error/permission/success | searchUsers / updateUserSettings | REQ-001, REQ-002, REQ-003 | CMN-FR-001 | 검색과 전체폭 목록 아래 로컬 설정 편집; 명명 승인 OQ-UI-001 | 공통 관리/인사·권한/사용자 관리 | users |
| SCR-ORGANIZATIONS | effective-period form | 조직코드 조회 → 상위조직·기간 저장 → 변경 전 관계 이력 조회 | /admin/organizations | 관리권한 확인 주체(OQ-AUTH-001) | loading/empty/error/permission/success | getOrganizationRelations / saveOrganizationRelation / listOrganizationRelationHistory | REQ-005, REQ-006, REQ-007 | CMN-FR-002 | 조직 관계 조회 → 기간 편집 → 변경 전 이력; 명명 승인 OQ-UI-001 | 공통 관리/인사·권한/조직 관리 | organizations |
| SCR-ROLES | content editor | 역할 식별정보 선택 → 목적·기준·범위 정의 저장 → 같은 역할 재조회 | /admin/roles | 관리권한 확인 주체(OQ-AUTH-001) | loading/empty/error/permission/success | listRoles / saveRole | REQ-009 | CMN-FR-005 | 식별정보로 역할 선택 후 정의 편집; 명명 승인 OQ-UI-001 | 공통 관리/인사·권한/역할 관리 | roles |
| SCR-USER-ROLES | effective-period form | 사용자 조회 → 기간·승인자와 부여·변경·회수 → 부여 목록 재조회 | /admin/user-roles | 관리권한 확인 주체(OQ-AUTH-001) | loading/empty/error/permission/success | listUserRoleAssignments / saveUserRoleAssignment | REQ-012, REQ-013, REQ-014 | CMN-FR-006 | 사용자별 부여 목록과 부여·변경·회수 기록 폼; 명명 승인 OQ-UI-001 | 공통 관리/인사·권한/사용자 역할 관리 | user_role_assignments |
| SCR-MENU-PERMISSIONS | permission matrix | 대상·메뉴 선택 → 현재 설정 조회 → 접근 허용 저장 → 같은 설정 확인 | /admin/menu-permissions | 관리권한 확인 주체(OQ-AUTH-001) | loading/empty/error/permission/success | listMenuPermissions / saveMenuPermission | REQ-016 | CMN-FR-007 | 대상·메뉴 선택 후 단일 허용 여부 편집; 전 조합 매트릭스 아님; 명명 승인 OQ-UI-001 | 공통 관리/메뉴/메뉴 권한 관리 | menu_permissions |
| SCR-MENU-STRUCTURE | tree editor | 메뉴 계층 조회 → 메뉴 선택 → 부모·계층·표시순서 저장 → 해당 부모 순서 확인 | /admin/menu-structure | 관리권한 확인 주체(OQ-AUTH-001) | loading/empty/error/permission/success | getMenuStructure / saveMenuStructure | REQ-019, REQ-020 | CMN-FR-013 | 전체폭 계층구조 아래 선택 메뉴의 부모·순서 편집; 명명 승인 OQ-UI-001 | 공통 관리/메뉴/메뉴 구조 관리 | menus |
| SCR-MENU-INFO | content editor | 메뉴 실행정보 저장 → 사용 가능한 메뉴 링크 선택 → 연결 화면으로 이동 | /admin/menu-info | 관리권한 확인 주체(OQ-AUTH-001) | loading/empty/error/permission/success | listMenuExecutionInfo / saveMenuExecutionInfo / getAccessibleMenus | REQ-022, REQ-023 | CMN-FR-014 | 실행정보 편집과 사용 가능한 메뉴 링크 실행; 명명 승인 OQ-UI-001 | 공통 관리/메뉴/메뉴 정보 관리 | menu_execution_info |
| SCR-CODE-GROUPS | search/list/detail | 그룹ID 조회 → 그룹 정의 저장 → 독립 그룹 값 재조회 | /admin/code-groups | 관리권한 확인 주체(OQ-AUTH-001) | loading/empty/error/permission/success | listCodeGroups / saveCodeGroup | REQ-025, REQ-026 | CMN-FR-016 | 그룹ID별 독립 조회와 전체폭 그룹 정의 폼; 명명 승인 OQ-UI-001 | 공통 관리/코드/코드그룹 관리 | code_groups |
| SCR-DETAIL-CODES | tree editor | 코드그룹 선택·조회 → 상세코드 순서·상위코드 저장 → 그룹별 계층·선택값 확인 | /admin/detail-codes | 관리권한 확인 주체(OQ-AUTH-001) | loading/empty/error/permission/success | listDetailCodes / saveDetailCode | REQ-028, REQ-029, REQ-030, REQ-031 | CMN-FR-017 | 그룹 선택 → 계층 들여쓰기 코드 목록 → 코드 편집; 명명 승인 OQ-UI-001 | 공통 관리/코드/상세코드 관리 | detail_codes |

## route 목록

| 화면ID | route | 진입 | 소유 UI 요구 |
| --- | --- | --- | --- |
| SCR-USERS | /admin/users | 3단계 메뉴 leaf 선택 | REQ-001, REQ-002, REQ-003 |
| SCR-ORGANIZATIONS | /admin/organizations | 3단계 메뉴 leaf 선택 | REQ-005, REQ-006, REQ-007 |
| SCR-ROLES | /admin/roles | 3단계 메뉴 leaf 선택 | REQ-009 |
| SCR-USER-ROLES | /admin/user-roles | 3단계 메뉴 leaf 선택 | REQ-012, REQ-013, REQ-014 |
| SCR-MENU-PERMISSIONS | /admin/menu-permissions | 3단계 메뉴 leaf 선택 | REQ-016 |
| SCR-MENU-STRUCTURE | /admin/menu-structure | 3단계 메뉴 leaf 선택 | REQ-019, REQ-020 |
| SCR-MENU-INFO | /admin/menu-info | 3단계 메뉴 leaf 선택 | REQ-022, REQ-023 |
| SCR-CODE-GROUPS | /admin/code-groups | 3단계 메뉴 leaf 선택 | REQ-025, REQ-026 |
| SCR-DETAIL-CODES | /admin/detail-codes | 3단계 메뉴 leaf 선택 | REQ-028, REQ-029, REQ-030, REQ-031 |

## 탐색 지도

공개: 사용자 화면을 새로 정의하지 않는다(/api/health는 probe). 관리 surface: 공통 관리 → 인사·권한/메뉴/코드 → 아래 leaf. 역할보호 surface: 각 route는 승인된 관리 접근 판정에 따라 진입; 일반 사용자 탐색 shell의 메뉴 읽기는 별도 지원 책임이다. 최초 인증/로그인 화면은 원문에 없어 생성하지 않는다.

## Screen Action Mapping

| action_id | 화면ID | trigger | operationId 또는 path | method | 성공 상태 | 오류 상태 |
| --- | --- | --- | --- | --- | --- | --- |
| ACT-01 | SCR-USERS | 화면 진입·조회/검색 및 실패 후 재시도 | searchUsers · GET /api/users | GET | 해당 화면의 원문 데이터 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-02 | SCR-USERS | 목록 행 선택 | N/A — local-only: 서버 응답의 선택 행을 같은 화면의 편집 영역에 바인딩; 독립 API 불필요 | N/A | 선택값이 하단 폼에 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-03 | SCR-USERS | 저장 선택: 사용여부 | updateUserSettings · PUT /api/users | PUT | /admin/users에 남아 저장값 재조회 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-04 | SCR-USERS | 취소 | N/A — local-only: 미저장 입력을 마지막 조회값으로 되돌림; 저장 API 호출하지 않음 | N/A | /admin/users의 마지막 조회값 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-05 | SCR-USERS | 사용자 역할 관리로 이동 링크 | N/A — navigation-only: 선택 user_id를 /admin/user-roles의 초기 조회 조건으로 전달; 쓰기 없음 | N/A | /admin/user-roles에 해당 사용자 역할 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-06 | SCR-ORGANIZATIONS | 화면 진입·조회/검색 및 실패 후 재시도 | getOrganizationRelations · GET /api/organizations | GET | 해당 화면의 원문 데이터 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-07 | SCR-ORGANIZATIONS | 저장 선택: 현재 편집값 | saveOrganizationRelation · PUT /api/organizations | PUT | /admin/organizations에 남아 저장값 재조회 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-08 | SCR-ORGANIZATIONS | 취소 | N/A — local-only: 미저장 입력을 마지막 조회값으로 되돌림; 저장 API 호출하지 않음 | N/A | /admin/organizations의 마지막 조회값 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-09 | SCR-ORGANIZATIONS | 이력 조회 선택 | listOrganizationRelationHistory · GET /api/organization-relation-history | GET | 하단에 변경 전 상위조직·적용 시작일·종료일 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-10 | SCR-ROLES | 화면 진입·조회/검색 및 실패 후 재시도 | listRoles · GET /api/roles | GET | 해당 화면의 원문 데이터 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-11 | SCR-ROLES | 목록 행 선택 | N/A — local-only: 서버 응답의 선택 행을 같은 화면의 편집 영역에 바인딩; 독립 API 불필요 | N/A | 선택값이 하단 폼에 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-12 | SCR-ROLES | 저장 선택: 현재 편집값 | saveRole · PUT /api/roles | PUT | /admin/roles에 남아 저장값 재조회 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-13 | SCR-ROLES | 취소 | N/A — local-only: 미저장 입력을 마지막 조회값으로 되돌림; 저장 API 호출하지 않음 | N/A | /admin/roles의 마지막 조회값 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-14 | SCR-USER-ROLES | 화면 진입·조회/검색 및 실패 후 재시도 | listUserRoleAssignments · GET /api/user-role-assignments | GET | 해당 화면의 원문 데이터 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-15 | SCR-USER-ROLES | 목록 행 선택 | N/A — local-only: 서버 응답의 선택 행을 같은 화면의 편집 영역에 바인딩; 독립 API 불필요 | N/A | 선택값이 하단 폼에 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-16 | SCR-USER-ROLES | 부여 선택: requestBody.action=부여; 승인자·기간 포함 제출 | saveUserRoleAssignment · PUT /api/user-role-assignments | PUT | 사용자 역할·기간·승인자 목록 재조회; 부여 기록 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-17 | SCR-USER-ROLES | 변경 선택: requestBody.action=변경; 승인자·기간 포함 제출 | saveUserRoleAssignment · PUT /api/user-role-assignments | PUT | 사용자 역할·기간·승인자 목록 재조회; 변경 기록 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-18 | SCR-USER-ROLES | 회수 선택: requestBody.action=회수; 승인자·기간 포함 제출 | saveUserRoleAssignment · PUT /api/user-role-assignments | PUT | 사용자 역할·기간·승인자 목록 재조회; 회수 기록 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-19 | SCR-USER-ROLES | 취소 | N/A — local-only: 미저장 입력을 마지막 조회값으로 되돌림; 저장 API 호출하지 않음 | N/A | /admin/user-roles의 마지막 조회값 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-20 | SCR-MENU-PERMISSIONS | 화면 진입·조회/검색 및 실패 후 재시도 | listMenuPermissions · GET /api/menu-permissions | GET | 해당 화면의 원문 데이터 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-21 | SCR-MENU-PERMISSIONS | 저장 선택: 현재 편집값 | saveMenuPermission · PUT /api/menu-permissions | PUT | /admin/menu-permissions에 남아 저장값 재조회 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-22 | SCR-MENU-PERMISSIONS | 취소 | N/A — local-only: 미저장 입력을 마지막 조회값으로 되돌림; 저장 API 호출하지 않음 | N/A | /admin/menu-permissions의 마지막 조회값 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-23 | SCR-MENU-STRUCTURE | 화면 진입·조회/검색 및 실패 후 재시도 | getMenuStructure · GET /api/menu-structure | GET | 해당 화면의 원문 데이터 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-24 | SCR-MENU-STRUCTURE | 목록 행 선택 | N/A — local-only: 서버 응답의 선택 행을 같은 화면의 편집 영역에 바인딩; 독립 API 불필요 | N/A | 선택값이 하단 폼에 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-25 | SCR-MENU-STRUCTURE | 저장 선택: 현재 편집값 | saveMenuStructure · PUT /api/menu-structure | PUT | /admin/menu-structure에 남아 저장값 재조회 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-26 | SCR-MENU-STRUCTURE | 취소 | N/A — local-only: 미저장 입력을 마지막 조회값으로 되돌림; 저장 API 호출하지 않음 | N/A | /admin/menu-structure의 마지막 조회값 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-27 | SCR-MENU-INFO | 화면 진입·조회/검색 및 실패 후 재시도 | listMenuExecutionInfo · GET /api/menu-execution-info | GET | 해당 화면의 원문 데이터 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-28 | SCR-MENU-INFO | 저장 선택: 현재 편집값 | saveMenuExecutionInfo · PUT /api/menu-execution-info | PUT | /admin/menu-info에 남아 저장값 재조회 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-29 | SCR-MENU-INFO | 취소 | N/A — local-only: 미저장 입력을 마지막 조회값으로 되돌림; 저장 API 호출하지 않음 | N/A | /admin/menu-info의 마지막 조회값 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-30 | SCR-MENU-INFO | 화면 진입 시 사용 가능한 메뉴 영역 읽기 | getAccessibleMenus · GET /api/accessible-menus | GET | 사용 가능한 메뉴의 screen_id/url 링크 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-31 | SCR-MENU-INFO | 사용 가능한 메뉴 링크 선택 | N/A — navigation-only: 응답 URL·screen_id의 연결 화면으로 사용자가 선택 이동; 추가 API/확인 버튼/자동 로그인 없음 | N/A | 선택한 menu_execution_info.url 화면으로 이동; 판정 근거 OQ-UI-003 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-32 | SCR-CODE-GROUPS | 화면 진입·조회/검색 및 실패 후 재시도 | listCodeGroups · GET /api/code-groups | GET | 해당 화면의 원문 데이터 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-33 | SCR-CODE-GROUPS | 목록 행 선택 | N/A — local-only: 서버 응답의 선택 행을 같은 화면의 편집 영역에 바인딩; 독립 API 불필요 | N/A | 선택값이 하단 폼에 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-34 | SCR-CODE-GROUPS | 저장 선택: 현재 편집값 | saveCodeGroup · PUT /api/code-groups | PUT | /admin/code-groups에 남아 저장값 재조회 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-35 | SCR-CODE-GROUPS | 취소 | N/A — local-only: 미저장 입력을 마지막 조회값으로 되돌림; 저장 API 호출하지 않음 | N/A | /admin/code-groups의 마지막 조회값 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-36 | SCR-DETAIL-CODES | 화면 진입·조회/검색 및 실패 후 재시도 | listDetailCodes · GET /api/detail-codes | GET | 해당 화면의 원문 데이터 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-37 | SCR-DETAIL-CODES | 목록 행 선택 | N/A — local-only: 서버 응답의 선택 행을 같은 화면의 편집 영역에 바인딩; 독립 API 불필요 | N/A | 선택값이 하단 폼에 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-38 | SCR-DETAIL-CODES | 저장 선택: 현재 편집값 | saveDetailCode · PUT /api/detail-codes | PUT | /admin/detail-codes에 남아 저장값 재조회 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |
| ACT-39 | SCR-DETAIL-CODES | 취소 | N/A — local-only: 미저장 입력을 마지막 조회값으로 되돌림; 저장 API 호출하지 않음 | N/A | /admin/detail-codes의 마지막 조회값 표시 | 검증 오류는 해당 필드·기존값 표시; 접근 불허는 현재 화면의 permission 상태, 데이터 무변경; 통신 오류는 입력 보존 |

## 화면별 wireframe 및 데이터 바인딩

원문이 확인 대화상자를 요구하지 않아 본 설계는 inline 편집과 취소를 사용한다. 회수는 삭제가 아니라 명시 명령이다. 승인 절차 OQ를 확인 modal로 바꾸지 않는다. 모달을 만들지 않았으므로 modal leaf도 없다. 필드 길이·추가 필수성은 미확인이다. 사용자 입력 token·비밀번호·업로드·결과확인 버튼 등 요청 없는 흐름을 추가하지 않는다.

### SCR-USERS · 사용자 관리

사용자 목표: 조건 입력·검색 → 목록 선택 → 사용여부 저장; 업무역할은 사용자 역할 관리로 이동.
레이아웃 전략: REQ-001의 일곱 검색 조건을 상단에 모으고 REQ-002의 인사정보는 가로로 읽는 전체폭 목록으로 유지한다. 행 선택 후 하단에는 REQ-003의 사용여부 편집과 사용자 역할 관리 이동만 노출한다. 코드그룹의 정의 편집과 달리 인사 원천값을 입력 폼으로 반복하지 않는다.

```text
 +----------------------------------------------------------------+
 | 사용자 관리                              사용자 검색           |
 | 교번 [           ] 성명 [           ] 소속 [          ]          |
 | 직급 [       ] 재직상태 [       ] 역할 [       ] 사용여부 [     ] |
 |                                                   [검색]       |
 +----------------------------------------------------------------+
 | 교번 / 성명 / 소속 / 직급 / 재직상태 / 역할 / 사용여부           |
 | 보직 / 퇴직일자 / 최종 동기화일시                               |
 | <API user row>                                  [행 선택]      |
 +----------------------------------------------------------------+
 | 선택 사용자: 원천 인사정보 읽기전용                             |
 | 시스템 사용여부 [     ]                 [사용여부 저장] [취소] |
 | 업무 역할 변경: [사용자 역할 관리로 이동]                       |
 | 저장 결과: 사용자 목록을 다시 조회하여 변경 사용여부 표시       |
 +----------------------------------------------------------------+
```

상호작용 요약: 진입은 `/admin/users`의 제안 메뉴 leaf. 주요 동작은 조건 입력·검색 → 목록 선택 → 사용여부 저장; 업무역할은 사용자 역할 관리로 이동. 파괴적 삭제는 not applicable(원문 미정; 삭제 제외 승인 아님). 확인 대화상자는 원문 미명시로 추가하지 않는다. 취소 시 마지막 조회값을 복원한다. 성공은 `/admin/users`의 저장값 재표시이며 통신 오류는 입력을 보존하고 권한 거부는 내용을 숨긴다.

#### Data Binding Table

| 화면 표시명 | field | 입력·표시 책임 | 정확한 API binding | canonical_id |
| --- | --- | --- | --- | --- |
| 교번 | employee_number | 표시·조회 조건 | searchUsers: data[].employee_number; 조회 전용, 저장 본문에 없음; query.employee_number | REQ-001, REQ-002, REQ-003 |
| 성명 | person_name | 표시·조회 조건 | searchUsers: data[].person_name; 조회 전용, 저장 본문에 없음; query.person_name | REQ-001, REQ-002, REQ-003 |
| 소속 | organization_code | 표시·조회 조건 | searchUsers: data[].organization_code; 조회 전용, 저장 본문에 없음; query.organization_code | REQ-001, REQ-002, REQ-003 |
| 직급 | job_grade | 표시·조회 조건 | searchUsers: data[].job_grade; 조회 전용, 저장 본문에 없음; query.job_grade | REQ-001, REQ-002, REQ-003 |
| 재직상태 | employment_status | 표시·조회 조건 | searchUsers: data[].employment_status; 조회 전용, 저장 본문에 없음; query.employment_status | REQ-001, REQ-002, REQ-003 |
| 역할 | business_roles | 표시·조회 조건 | searchUsers: data[].business_roles; 업무역할 변경은 /admin/user-roles에서 saveUserRoleAssignment 요청으로 연결; query.business_roles | REQ-001, REQ-002, REQ-003 |
| 사용여부 | enabled | 표시·편집 | searchUsers: data[].enabled; updateUserSettings: requestBody.enabled; query.enabled | REQ-001, REQ-002, REQ-003 |
| 보직 | position | 표시·조회 조건 | searchUsers: data[].position; 조회 전용, 저장 본문에 없음 | REQ-001, REQ-002, REQ-003 |
| 퇴직일자 | retired_on | 표시·조회 조건 | searchUsers: data[].retired_on; 조회 전용, 저장 본문에 없음 | REQ-001, REQ-002, REQ-003 |
| 최종 동기화일시 | last_synced_at | 표시·조회 조건 | searchUsers: data[].last_synced_at; 조회 전용, 저장 본문에 없음 | REQ-001, REQ-002, REQ-003 |
| 선택 사용자 내부 식별자(숨김) | user_id | 행 선택 시 응답 ID 보존; 직접 편집 안 함 | searchUsers: data[].user_id -> updateUserSettings: requestBody.user_id | REQ-003 |

#### 화면 상태

| state | 화면별 관찰과 복구 |
| --- | --- |
| loading | 사용자 관리 조회 완료 전 데이터 영역에 진행 표시; 중복 저장 방지. 동기 API 응답이 올 때까지 대기한다. |
| empty | 사용자 관리 조회 조건에 맞는 결과가 없음을 표시하고 이전 선택값을 제거; 미승인 생성 버튼을 추가하지 않는다. |
| error | searchUsers 또는 updateUserSettings 실패 원인을 표시; 재시도는 해당 원래 동작만 다시 호출하고 입력을 보존한다. |
| permission | /admin/users 접근 불허 판정 시 관리 데이터와 편집을 표시하지 않음. 권한 조합 OQ-AUTH-001. |
| success | 사용자 관리 응답 데이터를 표시; 저장 후 같은 화면에서 해당 식별정보로 재조회해 입력값 유지 확인. |

### SCR-ORGANIZATIONS · 조직 관리

사용자 목표: 조직코드 조회 → 상위조직·기간 저장 → 변경 전 관계 이력 조회.
레이아웃 전략: REQ-005의 상위조직 → 조회 조직 → 하위조직을 세로 관계도로 배치하고 REQ-006의 변경할 상위조직·적용기간을 그 아래 하나의 저장 묶음으로 둔다. REQ-007은 스크롤 아래의 별도 변경 전 이력 표로 분리한다. 사용자 역할 화면과 기간 입력을 공유하지만, 이 화면의 중심은 다중 역할 명령이 아니라 조직의 전후 관계다.

```text
 조직 관리    조직코드 [                         ] [조회]

               +--------------------------------------+
 현재 상위조직 | <API parent organization>            |
               +------------------+-------------------+
                                  |
               +------------------v-------------------+
 조회 조직     | 조직코드 / 조직구분                   |
               | <API organization row>               |
               | 적용 시작일 / 적용 종료일             |
               +------------------+-------------------+
                                  |
               +------------------v-------------------+
 현재 하위조직 | <API child organizations>            |
               +--------------------------------------+

 +-- 상위관계와 적용기간 함께 변경 ---------------------+
 | 변경할 상위조직 [                                 ] |
 | 적용 시작일 [          ] -> 적용 종료일 [          ] |
 |                         [상위관계 저장]      [취소] |
 +----------------------------------------------------+
   저장 후: 위 현재 관계의 상위조직·기간을 다시 조회

 --- 아래로 스크롤: 변경 전 관계 이력 ---
 [이력 조회]
 +-------------------+----------------+----------------+
 | 변경 전 상위조직  | 적용 시작일    | 적용 종료일    |
 +-------------------+----------------+----------------+
 | <API organization relation history row>            |
 +----------------------------------------------------+
```

상호작용 요약: 진입은 `/admin/organizations`의 제안 메뉴 leaf. 주요 동작은 조직코드 조회 → 상위조직·기간 저장 → 변경 전 관계 이력 조회. 파괴적 삭제는 not applicable(원문 미정; 삭제 제외 승인 아님). 확인 대화상자는 원문 미명시로 추가하지 않는다. 취소 시 마지막 조회값을 복원한다. 성공은 `/admin/organizations`의 저장값 재표시이며 통신 오류는 입력을 보존하고 권한 거부는 내용을 숨긴다.

#### Data Binding Table

| 화면 표시명 | field | 입력·표시 책임 | 정확한 API binding | canonical_id |
| --- | --- | --- | --- | --- |
| 조직코드 | organization_code | 표시·편집 | getOrganizationRelations: data[].organization_code; saveOrganizationRelation: requestBody.organization_code; query.organization_code | REQ-005, REQ-006, REQ-007 |
| 조직구분 | organization_type | 서버 응답 readonly | getOrganizationRelations: data[].organization_type; 조회 전용, 저장 본문에 없음 | REQ-005, REQ-006, REQ-007 |
| 상위조직 | parent_code | 표시·편집 | getOrganizationRelations: data[].parent_code; saveOrganizationRelation: requestBody.parent_code | REQ-005, REQ-006, REQ-007 |
| 하위조직 | child_codes | 서버 응답 readonly | getOrganizationRelations: data[].child_codes; 조회 전용, 저장 본문에 없음 | REQ-005, REQ-006, REQ-007 |
| 적용 시작일 | valid_from | 표시·편집 | getOrganizationRelations: data[].valid_from; saveOrganizationRelation: requestBody.valid_from | REQ-005, REQ-006, REQ-007 |
| 적용 종료일 | valid_to | 표시·편집 | getOrganizationRelations: data[].valid_to; saveOrganizationRelation: requestBody.valid_to | REQ-005, REQ-006, REQ-007 |
| 변경 전 상위조직·기간 | parent_code / valid_from / valid_to | 이력 readonly | listOrganizationRelationHistory: data[].parent_code/valid_from/valid_to | REQ-007 |

#### 화면 상태

| state | 화면별 관찰과 복구 |
| --- | --- |
| loading | 조직 관리 조회 완료 전 데이터 영역에 진행 표시; 중복 저장 방지. 동기 API 응답이 올 때까지 대기한다. |
| empty | 조직 관리 조회 조건에 맞는 결과가 없음을 표시하고 이전 선택값을 제거; 미승인 생성 버튼을 추가하지 않는다. |
| error | getOrganizationRelations 또는 saveOrganizationRelation 실패 원인을 표시; 재시도는 해당 원래 동작만 다시 호출하고 입력을 보존한다. |
| permission | /admin/organizations 접근 불허 판정 시 관리 데이터와 편집을 표시하지 않음. 권한 조합 OQ-AUTH-001. |
| success | 조직 관리 응답 데이터를 표시; 저장 후 같은 화면에서 해당 식별정보로 재조회해 입력값 유지 확인. |

### SCR-ROLES · 역할 관리

사용자 목표: 역할 식별정보 선택 → 목적·기준·범위 정의 저장 → 같은 역할 재조회.
레이아웃 전략: REQ-009의 목적·부여 기준·데이터 범위 기본값은 역할 정의를 작성하는 문서형 전체폭 편집 영역으로 표현한다. 조회·행 선택 영역은 위쪽의 간결한 식별 목록이며, 선택한 역할의 세 정의를 충분한 폭으로 읽고 저장한다. 메뉴 실행정보의 짧은 화면ID/URL 폼이나 코드그룹의 명칭·관리부서 폼과 구분하며 승인 단계나 범위 선택 옵션을 새로 만들지 않는다.

```text
 역할 관리     역할 식별정보 [                    ] [조회]
 조회 목록: 역할 식별정보 / 목적 / 부여 기준 / 데이터 범위 기본값
 <API role row>                                    [선택]
                          |
                          v
 +-- 역할 정의 -----------------------------------------------+
 | 역할 식별정보 [                                         ] |
 |                                                           |
 | 목적                                                      |
 | +-------------------------------------------------------+ |
 | |                                                       | |
 | +-------------------------------------------------------+ |
 | 부여 기준                                                 |
 | +-------------------------------------------------------+ |
 | |                                                       | |
 | +-------------------------------------------------------+ |
 | 데이터 범위 기본값                                        |
 | +-------------------------------------------------------+ |
 | |                                                       | |
 | +-------------------------------------------------------+ |
 | [취소]                                             [저장] |
 +-----------------------------------------------------------+
 저장 후 같은 역할 재조회: 목적·부여 기준·데이터 범위 기본값 유지
```

상호작용 요약: 진입은 `/admin/roles`의 제안 메뉴 leaf. 주요 동작은 역할 식별정보 선택 → 목적·기준·범위 정의 저장 → 같은 역할 재조회. 파괴적 삭제는 not applicable(원문 미정; 삭제 제외 승인 아님). 확인 대화상자는 원문 미명시로 추가하지 않는다. 취소 시 마지막 조회값을 복원한다. 성공은 `/admin/roles`의 저장값 재표시이며 통신 오류는 입력을 보존하고 권한 거부는 내용을 숨긴다.

#### Data Binding Table

| 화면 표시명 | field | 입력·표시 책임 | 정확한 API binding | canonical_id |
| --- | --- | --- | --- | --- |
| 역할 식별정보 | role_id | 표시·편집 | listRoles: data[].role_id; saveRole: requestBody.role_id; query.role_id | REQ-009 |
| 목적 | purpose | 표시·편집 | listRoles: data[].purpose; saveRole: requestBody.purpose | REQ-009 |
| 부여 기준 | grant_criteria | 표시·편집 | listRoles: data[].grant_criteria; saveRole: requestBody.grant_criteria | REQ-009 |
| 데이터 범위 기본값 | default_data_scope | 표시·편집 | listRoles: data[].default_data_scope; saveRole: requestBody.default_data_scope | REQ-009 |

#### 화면 상태

| state | 화면별 관찰과 복구 |
| --- | --- |
| loading | 역할 관리 조회 완료 전 데이터 영역에 진행 표시; 중복 저장 방지. 동기 API 응답이 올 때까지 대기한다. |
| empty | 역할 관리 조회 조건에 맞는 결과가 없음을 표시하고 이전 선택값을 제거; 미승인 생성 버튼을 추가하지 않는다. |
| error | listRoles 또는 saveRole 실패 원인을 표시; 재시도는 해당 원래 동작만 다시 호출하고 입력을 보존한다. |
| permission | /admin/roles 접근 불허 판정 시 관리 데이터와 편집을 표시하지 않음. 권한 조합 OQ-AUTH-001. |
| success | 역할 관리 응답 데이터를 표시; 저장 후 같은 화면에서 해당 식별정보로 재조회해 입력값 유지 확인. |

### SCR-USER-ROLES · 사용자 역할 관리

사용자 목표: 사용자 조회 → 기간·승인자와 부여·변경·회수 → 부여 목록 재조회.
레이아웃 전략: REQ-012의 한 사용자에 대한 여러 역할·서로 다른 기간을 먼저 부여 목록으로 비교한다. 선택 행 아래의 전체폭 기록 폼에서 REQ-013의 대상·역할·기간·승인자를 함께 입력하며 부여·변경과 회수를 분리 배치한다. REQ-014의 출처는 목록과 선택 기록 모두 읽기전용이다. 조직관계도의 상하위 연결·과거 이력 대신 사용자별 다중 부여와 명령 선택을 강조한다.

```text
 +-- 사용자 역할 관리 ------------------------------------------+
 | 대상 사용자 [                                  ] [조회]     |
 +-------------------------------------------------------------+
   사용자별 부여 목록: 서로 다른 역할과 각 유효기간을 함께 비교
 +-----------+-------------------------+--------+---------------+
 | 역할      | 유효 시작일 / 종료일    | 승인자 | 보직 기반·수동 |
 +-----------+-------------------------+--------+---------------+
 | <API user role assignment row>                      [선택]   |
 +-------------------------------------------------------------+
                            |
                 선택 부여의 기록 편집
                            v
 +-------------------------------------------------------------+
 | 대상 사용자 [                   ] 역할 [                  ] |
 | 유효 시작일 [                   ] 유효 종료일 [            ] |
 | 승인자      [                                             ] |
 | 보직 기반·수동 구분: <API assignment origin> (읽기전용)       |
 +------------------------------+------------------------------+
 | [부여]  [변경]       [취소]   | [회수]                       |
 | 대상·역할·기간·승인자 제출    | 같은 부여 식별정보로 제출    |
 +------------------------------+------------------------------+
   회수 효력·승인 절차: OQ-ROLE-001 (확인 필요)
   완료 후: 대상 사용자 목록에 역할별 기간·승인자를 다시 표시
```

상호작용 요약: 진입은 `/admin/user-roles`의 제안 메뉴 leaf. 주요 동작은 사용자 조회 → 기간·승인자와 부여·변경·회수 → 부여 목록 재조회. 파괴적 삭제는 not applicable(원문 미정; 삭제 제외 승인 아님). 회수 명령의 승인·효력은 OQ-ROLE-001. 취소 시 마지막 조회값을 복원한다. 성공은 `/admin/user-roles`의 저장값 재표시이며 통신 오류는 입력을 보존하고 권한 거부는 내용을 숨긴다.

식별자 전달: 부여 버튼은 frontend 내부에서 새 assignment_id UUID를 만들고 전송·재시도 동안 보존한다. 변경·회수 버튼은 선택한 listUserRoleAssignments.data[].assignment_id를 재사용한다. 신규 ID를 사용자가 입력하지 않는다. origin은 readonly이며 요청하지 않는다. 이 UUID 책임은 기술 설계 제안이고 승인자 자동 결정 정책이 아니다.

#### Data Binding Table

| 화면 표시명 | field | 입력·표시 책임 | 정확한 API binding | canonical_id |
| --- | --- | --- | --- | --- |
| 역할 부여 식별정보 | assignment_id | 화면 내부 숨김 식별자; 사용자 입력 아님 | listUserRoleAssignments: data[].assignment_id; saveUserRoleAssignment: requestBody.assignment_id | REQ-012, REQ-013, REQ-014 |
| 대상 사용자 | user_id | 표시·편집 | listUserRoleAssignments: data[].user_id; saveUserRoleAssignment: requestBody.user_id; query.user_id | REQ-012, REQ-013, REQ-014 |
| 역할 | role_id | 표시·편집 | listUserRoleAssignments: data[].role_id; saveUserRoleAssignment: requestBody.role_id | REQ-012, REQ-013, REQ-014 |
| 유효 시작일 | valid_from | 표시·편집 | listUserRoleAssignments: data[].valid_from; saveUserRoleAssignment: requestBody.valid_from | REQ-012, REQ-013, REQ-014 |
| 유효 종료일 | valid_to | 표시·편집 | listUserRoleAssignments: data[].valid_to; saveUserRoleAssignment: requestBody.valid_to | REQ-012, REQ-013, REQ-014 |
| 승인자 | approver_id | 표시·편집 | listUserRoleAssignments: data[].approver_id; saveUserRoleAssignment: requestBody.approver_id | REQ-012, REQ-013, REQ-014 |
| 보직 기반·수동 구분 | origin | 서버 응답 readonly | listUserRoleAssignments: data[].origin; 조회 전용, 저장 본문에 없음 | REQ-012, REQ-013, REQ-014 |
| 부여·변경·회수 버튼의 명령값 | action | 부여 -> 부여 / 변경 -> 변경 / 회수 -> 회수; 사용자 입력 필드 아님 | saveUserRoleAssignment: requestBody.action; AssignmentAction $ref | REQ-012, REQ-013 |

#### 화면 상태

| state | 화면별 관찰과 복구 |
| --- | --- |
| loading | 사용자 역할 관리 조회 완료 전 데이터 영역에 진행 표시; 중복 저장 방지. 동기 API 응답이 올 때까지 대기한다. |
| empty | 사용자 역할 관리 조회 조건에 맞는 결과가 없음을 표시하고 이전 선택값을 제거; 미승인 생성 버튼을 추가하지 않는다. |
| error | listUserRoleAssignments 또는 saveUserRoleAssignment 실패 원인을 표시; 재시도는 해당 원래 동작만 다시 호출하고 입력을 보존한다. |
| permission | /admin/user-roles 접근 불허 판정 시 관리 데이터와 편집을 표시하지 않음. 권한 조합 OQ-AUTH-001. |
| success | 사용자 역할 관리 응답 데이터를 표시; 저장 후 같은 화면에서 해당 식별정보로 재조회해 입력값 유지 확인. |

### SCR-MENU-PERMISSIONS · 메뉴 권한 관리

사용자 목표: 대상·메뉴 선택 → 현재 설정 조회 → 접근 허용 저장 → 같은 설정 확인.
레이아웃 전략: REQ-016의 권한 주체(역할·조직·사용자)와 접근 대상 메뉴를 한 쌍으로 배치하고, 아래 단일 설정 행에서 현재 응답값과 편집할 접근 허용 여부를 구분한다. archetype은 permission matrix이지만 전 대상×전 메뉴 격자를 생성하지 않는다. 조회 결과가 없다는 사실을 접근 불허값으로 채우지 않으며 조합·우선순위는 OQ-AUTH-001로 남긴다.

```text
 메뉴 권한 관리
 +-- 권한 대상 -----------------+     +-- 접근 대상 ------------+
 | 역할   [                   ] |     | 메뉴                   |
 | 조직   [                   ] +---->| [                    ] |
 | 사용자 [                   ] |     |                        |
 +------------------------------+     +------------------------+
                                         [현재 설정 조회]
   대상 조합·우선순위: OQ-AUTH-001 (확인 필요)

 +-- 선택 대상과 메뉴의 단일 설정 ------------------------------+
 | 선택 대상 / 메뉴 / 현재 접근 허용 여부                       |
 | <API menu permission row>                                   |
 +-----------------------------+-------------------------------+
 | 현재값은 조회 응답에서 표시 | 변경할 접근 허용 여부 [      ] |
 | 미조회·미설정은 불허 아님   | [허용 설정 저장]        [취소] |
 +-----------------------------+-------------------------------+
   저장 후: 같은 대상·메뉴를 재조회하여 허용 여부 표시
```

상호작용 요약: 진입은 `/admin/menu-permissions`의 제안 메뉴 leaf. 주요 동작은 대상·메뉴 선택 → 현재 설정 조회 → 접근 허용 저장 → 같은 설정 확인. 파괴적 삭제는 not applicable(원문 미정; 삭제 제외 승인 아님). 확인 대화상자는 원문 미명시로 추가하지 않는다. 취소 시 마지막 조회값을 복원한다. 성공은 `/admin/menu-permissions`의 저장값 재표시이며 통신 오류는 입력을 보존하고 권한 거부는 내용을 숨긴다.

대상 입력 표현: 선택되지 않은 role_id/organization_code/user_id는 생략 또는 null로 전송하고 응답 null로 표시한다. 최소 한 대상 선택은 기술 validation이며 여러 대상의 결합·우선순위와 기존 설정의 동일성 판정은 OQ-AUTH-001 승인 후 확정한다. ID는 숨긴 채 화면 내부에서 전달한다.

#### Data Binding Table

| 화면 표시명 | field | 입력·표시 책임 | 정확한 API binding | canonical_id |
| --- | --- | --- | --- | --- |
| 역할 | role_id | 표시·편집 | listMenuPermissions: data[].role_id; saveMenuPermission: requestBody.role_id; query.role_id | REQ-016 |
| 조직 | organization_code | 표시·편집 | listMenuPermissions: data[].organization_code; saveMenuPermission: requestBody.organization_code; query.organization_code | REQ-016 |
| 사용자 | user_id | 표시·편집 | listMenuPermissions: data[].user_id; saveMenuPermission: requestBody.user_id; query.user_id | REQ-016 |
| 메뉴 | menu_id | 표시·편집 | listMenuPermissions: data[].menu_id; saveMenuPermission: requestBody.menu_id; query.menu_id | REQ-016 |
| 접근 허용 여부 | allowed | 표시·편집 | listMenuPermissions: data[].allowed; saveMenuPermission: requestBody.allowed | REQ-016 |
| 설정 내부 식별자(숨김) | permission_id | 기존 설정은 응답 ID 보존; 미설정 대상·메뉴의 최초 저장은 내부 UUID 생성·재시도 보존 | listMenuPermissions: data[].permission_id -> saveMenuPermission: requestBody.permission_id | REQ-016 |

#### 화면 상태

| state | 화면별 관찰과 복구 |
| --- | --- |
| loading | 메뉴 권한 관리 조회 완료 전 데이터 영역에 진행 표시; 중복 저장 방지. 동기 API 응답이 올 때까지 대기한다. |
| empty | 메뉴 권한 관리 조회 조건에 맞는 결과가 없음을 표시하고 이전 선택값을 제거; 미승인 생성 버튼을 추가하지 않는다. |
| error | listMenuPermissions 또는 saveMenuPermission 실패 원인을 표시; 재시도는 해당 원래 동작만 다시 호출하고 입력을 보존한다. |
| permission | /admin/menu-permissions 접근 불허 판정 시 관리 데이터와 편집을 표시하지 않음. 권한 조합 OQ-AUTH-001. |
| success | 메뉴 권한 관리 응답 데이터를 표시; 저장 후 같은 화면에서 해당 식별정보로 재조회해 입력값 유지 확인. |

### SCR-MENU-STRUCTURE · 메뉴 구조 관리

사용자 목표: 메뉴 계층 조회 → 메뉴 선택 → 부모·계층·표시순서 저장 → 해당 부모 순서 확인.
레이아웃 전략: REQ-019의 전체폭 메뉴 트리에 선택 메뉴를 표시하고 바로 아래 연결된 구조 편집 영역에서 부모·계층을 다룬다. REQ-020의 표시순서는 편집 영역 하단에 따로 두어 동일 부모 내 순서를 변경함을 드러낸다. 상세코드 화면도 tree editor이지만 그 화면은 선택 코드그룹의 정렬된 코드 표이며, 여기서는 메뉴 부모 연결 자체가 탐색 중심이다. 드래그·일괄 재정렬은 추가하지 않는다.

```text
 메뉴 구조 관리                                      [계층 조회]
 +-- 메뉴 계층 ------------------------------------------------+
 | 메뉴 / 부모메뉴 / 계층단계 / 동일 계층 표시순서              |
 | <API parent menu row>                                       |
 |    |                                                       |
 |    +-- <API child menu row>                     [선택]       |
 |         |                                                  |
 +---------|--------------------------------------------------+
           v
     +-- 선택 메뉴 구조 --------------------------------------+
     | 메뉴     [                                          ] |
     | 부모메뉴 [                                          ] |
     |                  |                                    |
     |                  +--> 계층단계 [                    ] |
     +-------------------------------------------------------+
     | 동일 부모 내 표시순서 [                             ] |
     | [취소]                                    [구조 저장] |
     +-------------------------------------------------------+
       저장 후: 메뉴 계층의 부모 연결·계층단계 갱신
                해당 부모 아래 메뉴를 변경된 표시순서로 재조회
```

상호작용 요약: 진입은 `/admin/menu-structure`의 제안 메뉴 leaf. 주요 동작은 메뉴 계층 조회 → 메뉴 선택 → 부모·계층·표시순서 저장 → 해당 부모 순서 확인. 파괴적 삭제는 not applicable(원문 미정; 삭제 제외 승인 아님). 확인 대화상자는 원문 미명시로 추가하지 않는다. 취소 시 마지막 조회값을 복원한다. 성공은 `/admin/menu-structure`의 저장값 재표시이며 통신 오류는 입력을 보존하고 권한 거부는 내용을 숨긴다.

#### Data Binding Table

| 화면 표시명 | field | 입력·표시 책임 | 정확한 API binding | canonical_id |
| --- | --- | --- | --- | --- |
| 메뉴 | menu_id | 표시·편집 | getMenuStructure: data[].menu_id; saveMenuStructure: requestBody.menu_id | REQ-019, REQ-020 |
| 부모메뉴 | parent_menu_id | 표시·편집 | getMenuStructure: data[].parent_menu_id; saveMenuStructure: requestBody.parent_menu_id; query.parent_menu_id | REQ-019, REQ-020 |
| 계층단계 | level | 표시·편집 | getMenuStructure: data[].level; saveMenuStructure: requestBody.level | REQ-019, REQ-020 |
| 표시순서 | display_order | 표시·편집 | getMenuStructure: data[].display_order; saveMenuStructure: requestBody.display_order | REQ-019, REQ-020 |

#### 화면 상태

| state | 화면별 관찰과 복구 |
| --- | --- |
| loading | 메뉴 구조 관리 조회 완료 전 데이터 영역에 진행 표시; 중복 저장 방지. 동기 API 응답이 올 때까지 대기한다. |
| empty | 메뉴 구조 관리 조회 조건에 맞는 결과가 없음을 표시하고 이전 선택값을 제거; 미승인 생성 버튼을 추가하지 않는다. |
| error | getMenuStructure 또는 saveMenuStructure 실패 원인을 표시; 재시도는 해당 원래 동작만 다시 호출하고 입력을 보존한다. |
| permission | /admin/menu-structure 접근 불허 판정 시 관리 데이터와 편집을 표시하지 않음. 권한 조합 OQ-AUTH-001. |
| success | 메뉴 구조 관리 응답 데이터를 표시; 저장 후 같은 화면에서 해당 식별정보로 재조회해 입력값 유지 확인. |

### SCR-MENU-INFO · 메뉴 정보 관리

사용자 목표: 메뉴 실행정보 저장 → 사용 가능한 메뉴 링크 선택 → 연결 화면으로 이동.
레이아웃 전략: REQ-022의 메뉴→화면ID/URL 연결을 짧은 실행정보 편집 폼으로 보여주고, REQ-023의 사용 가능한 메뉴 링크는 폼과 분리된 탐색 영역에 둔다. 저장 결과를 읽는 단계와 사용자가 링크를 선택해 실제 연결 화면으로 이동하는 단계를 시각적으로 분리한다. 역할 정의 편집과 같은 content editor이지만 긴 정의 입력이나 목록 행 선택 폼을 복제하지 않는다.

```text
 메뉴 정보 관리
 메뉴 [                                    ] [조회]
   |
   +--> +-- 실행정보 -----------------------------------------+
        | 화면ID [                                         ] |
        | URL    [                                         ] |
        |                       [취소]                [저장] |
        +----------------------------------------------------+
          저장 후: 같은 메뉴의 화면ID·URL을 재조회하여 표시

 +-- 사용 가능한 메뉴 ---------------------------------------+
 | <API accessible menu link>                                |
 | 연결 화면ID / URL은 조회 응답의 실행정보                  |
 +----------------------------+------------------------------+
                              |
                    사용자가 메뉴 링크 선택
                              v
                 연결된 화면ID·URL의 화면으로 이동
   사용 가능 판정: OQ-UI-003; 자동 redirect·자동 로그인 아님
```

상호작용 요약: 진입은 `/admin/menu-info`의 제안 메뉴 leaf. 주요 동작은 메뉴 실행정보 저장 → 사용 가능한 메뉴 링크 선택 → 연결 화면으로 이동. 파괴적 삭제는 not applicable(원문 미정; 삭제 제외 승인 아님). 확인 대화상자는 원문 미명시로 추가하지 않는다. 취소 시 마지막 조회값을 복원한다. 성공은 `/admin/menu-info`의 저장값 재표시 또는 사용자가 선택한 메뉴 URL 화면 이동이며 통신 오류는 입력을 보존하고 권한 거부는 내용을 숨긴다.

#### Data Binding Table

| 화면 표시명 | field | 입력·표시 책임 | 정확한 API binding | canonical_id |
| --- | --- | --- | --- | --- |
| 메뉴 | menu_id | 표시·편집 | listMenuExecutionInfo: data[].menu_id; saveMenuExecutionInfo: requestBody.menu_id; query.menu_id | REQ-022, REQ-023 |
| 화면ID | screen_id | 표시·편집 | listMenuExecutionInfo: data[].screen_id; saveMenuExecutionInfo: requestBody.screen_id | REQ-022, REQ-023 |
| URL | url | 표시·편집 | listMenuExecutionInfo: data[].url; saveMenuExecutionInfo: requestBody.url | REQ-022, REQ-023 |
| 사용 가능한 메뉴 링크 | screen_id / url | 사용자 선택 이동 | getAccessibleMenus: data.menus[].screen_id/url | REQ-023 |

#### 화면 상태

| state | 화면별 관찰과 복구 |
| --- | --- |
| loading | 메뉴 정보 관리 조회 완료 전 데이터 영역에 진행 표시; 중복 저장 방지. 동기 API 응답이 올 때까지 대기한다. |
| empty | 메뉴 정보 관리 조회 조건에 맞는 결과가 없음을 표시하고 이전 선택값을 제거; 미승인 생성 버튼을 추가하지 않는다. |
| error | listMenuExecutionInfo 또는 saveMenuExecutionInfo 실패 원인을 표시; 재시도는 해당 원래 동작만 다시 호출하고 입력을 보존한다. |
| permission | /admin/menu-info 접근 불허 판정 시 관리 데이터와 편집을 표시하지 않음. 권한 조합 OQ-AUTH-001. |
| success | 메뉴 정보 관리 응답 데이터를 표시; 저장 후 같은 화면에서 해당 식별정보로 재조회해 입력값 유지 확인. |

### SCR-CODE-GROUPS · 코드그룹 관리

사용자 목표: 그룹ID 조회 → 그룹 정의 저장 → 독립 그룹 값 재조회.
레이아웃 전략: REQ-026의 그룹ID 조회·선택 목록 뒤에 REQ-025의 그룹 정의를 배치한다. 그룹ID·명칭·관리부서는 짧은 식별·관리 정보로 묶고 설명은 넓은 입력 영역으로 분리한다. 사용자 검색과 같은 search/list/detail이지만 인사 원천 목록·사용여부만의 편집 대신 선택 그룹의 네 정의값을 저장하는 화면이며, 상세코드를 이 화면에 끼워 넣지 않는다.

```text
 코드그룹 관리     그룹ID [                     ] [조회]
 +----------+------------+------------------+---------------+
 | 그룹ID   | 명칭       | 설명             | 관리부서      |
 +----------+------------+------------------+---------------+
 | <API code group row>                           [선택]   |
 +--------------------------------------------------------+

 +-- 선택 그룹 정의 --------------------------------------+
 | 그룹ID [                                             ] |
 +--------------------------+-----------------------------+
 | 명칭 [                 ] | 관리부서 [                ] |
 +--------------------------+-----------------------------+
 | 설명                                                   |
 | +----------------------------------------------------+ |
 | |                                                    | |
 | |                                                    | |
 | +----------------------------------------------------+ |
 +--------------------------------------------------------+
                                  [취소]          [저장]
 저장 후: 해당 그룹ID의 명칭·설명·관리부서를 목록에 다시 표시
```

상호작용 요약: 진입은 `/admin/code-groups`의 제안 메뉴 leaf. 주요 동작은 그룹ID 조회 → 그룹 정의 저장 → 독립 그룹 값 재조회. 파괴적 삭제는 not applicable(원문 미정; 삭제 제외 승인 아님). 확인 대화상자는 원문 미명시로 추가하지 않는다. 취소 시 마지막 조회값을 복원한다. 성공은 `/admin/code-groups`의 저장값 재표시이며 통신 오류는 입력을 보존하고 권한 거부는 내용을 숨긴다.

#### Data Binding Table

| 화면 표시명 | field | 입력·표시 책임 | 정확한 API binding | canonical_id |
| --- | --- | --- | --- | --- |
| 그룹ID | group_id | 표시·편집 | listCodeGroups: data[].group_id; saveCodeGroup: requestBody.group_id; query.group_id | REQ-025, REQ-026 |
| 명칭 | name | 표시·편집 | listCodeGroups: data[].name; saveCodeGroup: requestBody.name | REQ-025, REQ-026 |
| 설명 | description | 표시·편집 | listCodeGroups: data[].description; saveCodeGroup: requestBody.description | REQ-025, REQ-026 |
| 관리부서 | managing_department | 표시·편집 | listCodeGroups: data[].managing_department; saveCodeGroup: requestBody.managing_department | REQ-025, REQ-026 |

#### 화면 상태

| state | 화면별 관찰과 복구 |
| --- | --- |
| loading | 코드그룹 관리 조회 완료 전 데이터 영역에 진행 표시; 중복 저장 방지. 동기 API 응답이 올 때까지 대기한다. |
| empty | 코드그룹 관리 조회 조건에 맞는 결과가 없음을 표시하고 이전 선택값을 제거; 미승인 생성 버튼을 추가하지 않는다. |
| error | listCodeGroups 또는 saveCodeGroup 실패 원인을 표시; 재시도는 해당 원래 동작만 다시 호출하고 입력을 보존한다. |
| permission | /admin/code-groups 접근 불허 판정 시 관리 데이터와 편집을 표시하지 않음. 권한 조합 OQ-AUTH-001. |
| success | 코드그룹 관리 응답 데이터를 표시; 저장 후 같은 화면에서 해당 식별정보로 재조회해 입력값 유지 확인. |

### SCR-DETAIL-CODES · 상세코드 관리

사용자 목표: 코드그룹 선택·조회 → 상세코드 순서·상위코드 저장 → 그룹별 계층·선택값 확인.
레이아웃 전략: REQ-028의 코드그룹을 최상단 조회 문맥으로 고정하고 그 그룹에 한정한 코드 표에 REQ-031의 상하위 관계와 REQ-030의 정렬순서를 함께 보인다. 선택 코드의 하단 편집은 코드·상위코드와 정렬순서를 나누어 REQ-029의 저장 후 동일 그룹 목록을 갱신한다. 메뉴 트리의 노드 연결 편집을 복제하지 않으며, 추가 명시 필드는 OQ-UI-002로 유지한다.

```text
 +-- 상세코드 관리 -------------------------------------------+
 | 코드그룹 [                                    ] [조회]    |
 +==========================================================+
 | 선택 그룹의 상세코드만 표시 / 정렬순서대로 제공            |
 +----------------------------------+------------+----------+
 | 상세코드 계층                     | 상위코드   | 정렬순서 |
 +----------------------------------+------------+----------+
 | <API detail code parent row>                             |
 |   +-- <API detail code child row>                [선택]   |
 +----------------------------------------------------------+

 --- 아래로 스크롤: 선택 코드 편집 ---
 +-- 상세코드·상위 연결 ------------------+-------------------+
 | 상세코드 [                          ] | 정렬순서          |
 | 상위코드 [                          ] | [               ] |
 +---------------------------------------+-------------------+
 | 추가 명시 필드: OQ-UI-002 (확인 필요)                      |
 | [취소]                                            [저장] |
 +----------------------------------------------------------+
 저장 후: 같은 코드그룹 목록에 코드·상위관계·변경 순서를 반영
```

상호작용 요약: 진입은 `/admin/detail-codes`의 제안 메뉴 leaf. 주요 동작은 코드그룹 선택·조회 → 상세코드 순서·상위코드 저장 → 그룹별 계층·선택값 확인. 파괴적 삭제는 not applicable(원문 미정; 삭제 제외 승인 아님). 확인 대화상자는 원문 미명시로 추가하지 않는다. 취소 시 마지막 조회값을 복원한다. 성공은 `/admin/detail-codes`의 저장값 재표시이며 통신 오류는 입력을 보존하고 권한 거부는 내용을 숨긴다.

#### Data Binding Table

| 화면 표시명 | field | 입력·표시 책임 | 정확한 API binding | canonical_id |
| --- | --- | --- | --- | --- |
| 코드그룹 | group_id | 표시·편집 | listDetailCodes: data[].group_id; saveDetailCode: requestBody.group_id; query.group_id | REQ-028, REQ-029, REQ-030, REQ-031 |
| 상세코드 | code | 표시·편집 | listDetailCodes: data[].code; saveDetailCode: requestBody.code | REQ-028, REQ-029, REQ-030, REQ-031 |
| 정렬순서 | sort_order | 표시·편집 | listDetailCodes: data[].sort_order; saveDetailCode: requestBody.sort_order | REQ-028, REQ-029, REQ-030, REQ-031 |
| 상위코드 | parent_code | 표시·편집 | listDetailCodes: data[].parent_code; saveDetailCode: requestBody.parent_code | REQ-028, REQ-029, REQ-030, REQ-031 |
| 추가 명시 필드 | OQ-UI-002 | 확인 필요 | 코드 저장 계약을 추가 확정하기 전 누락을 구현 완료로 세지 않음 | REQ-029 |

#### 화면 상태

| state | 화면별 관찰과 복구 |
| --- | --- |
| loading | 상세코드 관리 조회 완료 전 데이터 영역에 진행 표시; 중복 저장 방지. 동기 API 응답이 올 때까지 대기한다. |
| empty | 상세코드 관리 조회 조건에 맞는 결과가 없음을 표시하고 이전 선택값을 제거; 미승인 생성 버튼을 추가하지 않는다. |
| error | listDetailCodes 또는 saveDetailCode 실패 원인을 표시; 재시도는 해당 원래 동작만 다시 호출하고 입력을 보존한다. |
| permission | /admin/detail-codes 접근 불허 판정 시 관리 데이터와 편집을 표시하지 않음. 권한 조합 OQ-AUTH-001. |
| success | 상세코드 관리 응답 데이터를 표시; 저장 후 같은 화면에서 해당 식별정보로 재조회해 입력값 유지 확인. |

## 화면별 UI 계약

| 화면ID | 사용자 목적 | 주요 데이터/입력 | 주요 CTA·확인 | 입력 검증 | 성공 후 이동 | 권한·기간·상태 제약 |
| --- | --- | --- | --- | --- | --- | --- |
| SCR-USERS | 조건 입력·검색 → 목록 선택 → 사용여부 저장; 업무역할은 사용자 역할 관리로 이동 | 교번 / 성명 / 소속 / 직급 / 재직상태 / 역할 / 사용여부 / 보직 / 퇴직일자 / 최종 동기화일시 | 조회·저장; 확인 dialog 원문 미명시로 추가하지 않음 | 인사 원천 수정 거부; use 필드는 boolean; 역할 변경은 사용자 역할 화면 | /admin/users에서 같은 대상 재조회; 역할 변경 링크는 /admin/user-roles | 관리 접근 실효 거부 시 403 및 데이터·편집 차단. 추가 기간·상태 차단 조건은 원문 미정이며 없음으로 단정하지 않음. |
| SCR-ORGANIZATIONS | 조직코드 조회 → 상위조직·기간 저장 → 변경 전 관계 이력 조회 | 조직코드 / 조직구분 / 상위조직 / 하위조직 / 적용 시작일 / 적용 종료일 | 조회·저장; 확인 dialog 원문 미명시로 추가하지 않음 | organization_code 누락 -> 400 field_errors.field=organization_code이며 organization_relations 행 증가 없음; 추가 사업 검증 OQ | /admin/organizations에서 저장한 대상·관계 재조회 | 관리 접근 실효 거부 시 403 및 데이터·편집 차단. 추가 기간·상태 차단 조건은 원문 미정이며 없음으로 단정하지 않음. |
| SCR-ROLES | 역할 식별정보 선택 → 목적·기준·범위 정의 저장 → 같은 역할 재조회 | 역할 식별정보 / 목적 / 부여 기준 / 데이터 범위 기본값 | 조회·저장; 확인 dialog 원문 미명시로 추가하지 않음 | role_id 누락 -> 400 field_errors.field=role_id이며 roles 변경 없음; 추가 사업 검증 OQ | /admin/roles에서 저장한 대상·관계 재조회 | 관리 접근 실효 거부 시 403 및 데이터·편집 차단. 추가 기간·상태 차단 조건은 원문 미정이며 없음으로 단정하지 않음. |
| SCR-USER-ROLES | 사용자 조회 → 기간·승인자와 부여·변경·회수 → 부여 목록 재조회 | 역할 부여 식별정보 / 대상 사용자 / 역할 / 유효 시작일 / 유효 종료일 / 승인자 / 보직 기반·수동 구분 | 부여·변경·회수; 별도 승인·확인 OQ-ROLE-001 | approver_id 누락 -> 400 field_errors.field=approver_id이며 user_role_assignments 변경 없음; 추가 사업 검증 OQ | /admin/user-roles에서 저장한 대상·관계 재조회 | 관리 접근 실효 거부 시 403 및 데이터·편집 차단. 종료 역할은 현재 적용 제외(REQ-015는 API 판정 책임); UI 기간 경계 OQ-ROLE-001. |
| SCR-MENU-PERMISSIONS | 대상·메뉴 선택 → 현재 설정 조회 → 접근 허용 저장 → 같은 설정 확인 | 역할 / 조직 / 사용자 / 메뉴 / 접근 허용 여부 | 조회·저장; 확인 dialog 원문 미명시로 추가하지 않음 | allowed를 boolean이 아닌 값으로 전송 -> 400 field_errors.field=allowed이며 menu_permissions 변경 없음; 추가 사업 검증 OQ | /admin/menu-permissions에서 저장한 대상·관계 재조회 | 관리 접근 실효 거부 시 403 및 데이터·편집 차단. 추가 기간·상태 차단 조건은 원문 미정이며 없음으로 단정하지 않음. |
| SCR-MENU-STRUCTURE | 메뉴 계층 조회 → 메뉴 선택 → 부모·계층·표시순서 저장 → 해당 부모 순서 확인 | 메뉴 / 부모메뉴 / 계층단계 / 표시순서 | 조회·저장; 확인 dialog 원문 미명시로 추가하지 않음 | display_order를 정수가 아닌 값으로 전송 -> 400 field_errors.field=display_order이며 menus 변경 없음; 추가 사업 검증 OQ | /admin/menu-structure에서 저장한 대상·관계 재조회 | 관리 접근 실효 거부 시 403 및 데이터·편집 차단. 추가 기간·상태 차단 조건은 원문 미정이며 없음으로 단정하지 않음. |
| SCR-MENU-INFO | 메뉴 실행정보 저장 → 사용 가능한 메뉴 링크 선택 → 연결 화면으로 이동 | 메뉴 / 화면ID / URL | 조회·저장; 확인 dialog 원문 미명시로 추가하지 않음 | screen_id 누락 -> 400 field_errors.field=screen_id이며 menu_execution_info 변경 없음; 추가 사업 검증 OQ | /admin/menu-info에 저장 결과 재표시; 메뉴 링크 선택은 해당 menu_execution_info.url | 관리 접근 실효 거부 시 403 및 데이터·편집 차단. 추가 기간·상태 차단 조건은 원문 미정이며 없음으로 단정하지 않음. |
| SCR-CODE-GROUPS | 그룹ID 조회 → 그룹 정의 저장 → 독립 그룹 값 재조회 | 그룹ID / 명칭 / 설명 / 관리부서 | 조회·저장; 확인 dialog 원문 미명시로 추가하지 않음 | group_id 누락 -> 400 field_errors.field=group_id이며 code_groups 변경 없음; 추가 사업 검증 OQ | /admin/code-groups에서 저장한 대상·관계 재조회 | 관리 접근 실효 거부 시 403 및 데이터·편집 차단. 추가 기간·상태 차단 조건은 원문 미정이며 없음으로 단정하지 않음. |
| SCR-DETAIL-CODES | 코드그룹 선택·조회 → 상세코드 순서·상위코드 저장 → 그룹별 계층·선택값 확인 | 코드그룹 / 상세코드 / 정렬순서 / 상위코드; 추가 명시 필드 OQ-UI-002 | 조회·저장; 확인 dialog 원문 미명시로 추가하지 않음 | sort_order를 정수가 아닌 값으로 전송 -> 400 field_errors.field=sort_order이며 detail_codes 변경 없음; 추가 사업 검증 OQ | /admin/detail-codes에서 저장한 대상·관계 재조회 | 관리 접근 실효 거부 시 403 및 데이터·편집 차단. 추가 기간·상태 차단 조건은 원문 미정이며 없음으로 단정하지 않음. |

## 목록 화면 규칙

| 화면ID | filters | default sort | pagination | bulk action | export | empty-result |
| --- | --- | --- | --- | --- | --- | --- |
| SCR-USERS | 교번 / 성명 / 소속 / 직급 / 재직상태 / 역할 / 사용여부 | OQ-UI-001: 원문 기본 정렬 미지정 | API page/size 기술 제안; 기본값·시각적 pager 승인 OQ-UI-001 | not applicable: 요청 없음 | not applicable: 요청 없음 | 사용자 관리 일치 결과 없음; 잘못된 기존 선택값 제거 |
| SCR-ORGANIZATIONS | not applicable: 조직코드 단건 관계/이력 선택 | not applicable | not applicable: 화면 전체 pager는 만들지 않음; 조회 API는 page/size 명시 | not applicable: 요청 없음 | not applicable: 요청 없음 | 조직 관리 일치 결과 없음; 잘못된 기존 선택값 제거 |
| SCR-ROLES | 역할 식별정보 | OQ-UI-001: 원문 기본 정렬 미지정 | API page/size 기술 제안; 기본값·시각적 pager 승인 OQ-UI-001 | not applicable: 요청 없음 | not applicable: 요청 없음 | 역할 관리 일치 결과 없음; 잘못된 기존 선택값 제거 |
| SCR-USER-ROLES | 대상 사용자 | OQ-UI-001: 원문 기본 정렬 미지정 | API page/size 기술 제안; 기본값·시각적 pager 승인 OQ-UI-001 | not applicable: 요청 없음 | not applicable: 요청 없음 | 사용자 역할 관리 일치 결과 없음; 잘못된 기존 선택값 제거 |
| SCR-MENU-PERMISSIONS | 역할 / 조직 / 사용자 / 메뉴 | OQ-UI-001: 원문 기본 정렬 미지정 | API page/size 기술 제안; 기본값·시각적 pager 승인 OQ-UI-001 | not applicable: 요청 없음 | not applicable: 요청 없음 | 메뉴 권한 관리 일치 결과 없음; 잘못된 기존 선택값 제거 |
| SCR-MENU-STRUCTURE | 부모메뉴 | 동일 부모 내 display_order(REQ-020) | API page/size 기술 제안; 기본값·시각적 pager 승인 OQ-UI-001 | not applicable: 요청 없음 | not applicable: 요청 없음 | 메뉴 구조 관리 일치 결과 없음; 잘못된 기존 선택값 제거 |
| SCR-MENU-INFO | not applicable: 메뉴 실행정보 편집 | not applicable | not applicable: 화면 전체 pager는 만들지 않음; 조회 API는 page/size 명시 | not applicable: 요청 없음 | not applicable: 요청 없음 | 메뉴 정보 관리 일치 결과 없음; 잘못된 기존 선택값 제거 |
| SCR-CODE-GROUPS | 그룹ID | OQ-UI-001: 원문 기본 정렬 미지정 | API page/size 기술 제안; 기본값·시각적 pager 승인 OQ-UI-001 | not applicable: 요청 없음 | not applicable: 요청 없음 | 코드그룹 관리 일치 결과 없음; 잘못된 기존 선택값 제거 |
| SCR-DETAIL-CODES | 코드그룹 | sort_order 순서(REQ-030) | API page/size 기술 제안; 기본값·시각적 pager 승인 OQ-UI-001 | not applicable: 요청 없음 | not applicable: 요청 없음 | 상세코드 관리 일치 결과 없음; 잘못된 기존 선택값 제거 |

## archetype별 navigation/sequence flow

| archetype | entry | success | cancel | error | permission-denied |
| --- | --- | --- | --- | --- | --- |
| search/list/detail / SCR-USERS | /admin/users 메뉴 진입 | 조건 입력·검색 → 목록 선택 → 사용여부 저장; 업무역할은 사용자 역할 관리로 이동 | /admin/users 마지막 조회값 복원 | searchUsers 재시도 또는 입력 유지 | /admin/users 관리 데이터 숨김; 서버 실효 불허 403 |
| effective-period form / SCR-ORGANIZATIONS | /admin/organizations 메뉴 진입 | 조직코드 조회 → 상위조직·기간 저장 → 변경 전 관계 이력 조회 | /admin/organizations 마지막 조회값 복원 | getOrganizationRelations 재시도 또는 입력 유지 | /admin/organizations 관리 데이터 숨김; 서버 실효 불허 403 |
| content editor / SCR-ROLES | /admin/roles 메뉴 진입 | 역할 식별정보 선택 → 목적·기준·범위 정의 저장 → 같은 역할 재조회 | /admin/roles 마지막 조회값 복원 | listRoles 재시도 또는 입력 유지 | /admin/roles 관리 데이터 숨김; 서버 실효 불허 403 |
| effective-period form / SCR-USER-ROLES | /admin/user-roles 메뉴 진입 | 사용자 조회 → 기간·승인자와 부여·변경·회수 → 부여 목록 재조회 | /admin/user-roles 마지막 조회값 복원 | listUserRoleAssignments 재시도 또는 입력 유지 | /admin/user-roles 관리 데이터 숨김; 서버 실효 불허 403 |
| permission matrix / SCR-MENU-PERMISSIONS | /admin/menu-permissions 메뉴 진입 | 대상·메뉴 선택 → 현재 설정 조회 → 접근 허용 저장 → 같은 설정 확인 | /admin/menu-permissions 마지막 조회값 복원 | listMenuPermissions 재시도 또는 입력 유지 | /admin/menu-permissions 관리 데이터 숨김; 서버 실효 불허 403 |
| tree editor / SCR-MENU-STRUCTURE | /admin/menu-structure 메뉴 진입 | 메뉴 계층 조회 → 메뉴 선택 → 부모·계층·표시순서 저장 → 해당 부모 순서 확인 | /admin/menu-structure 마지막 조회값 복원 | getMenuStructure 재시도 또는 입력 유지 | /admin/menu-structure 관리 데이터 숨김; 서버 실효 불허 403 |
| content editor / SCR-MENU-INFO | /admin/menu-info 메뉴 진입 | 메뉴 실행정보 저장 → 사용 가능한 메뉴 링크 선택 → 연결 화면으로 이동 | /admin/menu-info 마지막 조회값 복원 | listMenuExecutionInfo 재시도 또는 입력 유지 | /admin/menu-info 관리 데이터 숨김; 서버 실효 불허 403 |
| search/list/detail / SCR-CODE-GROUPS | /admin/code-groups 메뉴 진입 | 그룹ID 조회 → 그룹 정의 저장 → 독립 그룹 값 재조회 | /admin/code-groups 마지막 조회값 복원 | listCodeGroups 재시도 또는 입력 유지 | /admin/code-groups 관리 데이터 숨김; 서버 실효 불허 403 |
| tree editor / SCR-DETAIL-CODES | /admin/detail-codes 메뉴 진입 | 코드그룹 선택·조회 → 상세코드 순서·상위코드 저장 → 그룹별 계층·선택값 확인 | /admin/detail-codes 마지막 조회값 복원 | listDetailCodes 재시도 또는 입력 유지 | /admin/detail-codes 관리 데이터 숨김; 서버 실효 불허 403 |

## 역할·기간·상태 전이 대응

원문에 역할 코드나 승인 lifecycle enum이 없으므로 임의 관리자 전용 코드·active/revoked enum을 만들지 않는다. 아래는 허용/차단 결과만 기술한다.

| 영역 | 전제·변경 | UI 처리 | 서버 인가·저장 | 미정 정책 |
| --- | --- | --- | --- | --- |
| 사용자 설정 | 관리 접근 허용, 원천 필드 편집 시도 | 원천 readonly, 사용여부만 편집 | 원천 변경 거부·기존 값 보존(REQ-004) | 퇴직·사용중지의 접속 처리 OQ-DATA-002 |
| 조직관계 | 상위조직·기간 변경 | 현재 관계와 이력을 분리 표시 | 조직관계·기간 저장, 사용자 소속·보직·역할 불변 | 기간 중복/순환·과거정정 OQ-ORG-001 |
| 사용자 역할 | 부여·변경·회수, 기간 종료 | 기간·승인자 입력, 출처 구분 표시 | 대상·역할·기간·승인자 저장, 종료 역할 현재 제외 | 승인 단계·회수 효력·보직 매핑 OQ-ROLE-001 |
| 메뉴 권한 | 실효 관리 접근 거부 | 관리 데이터·버튼 숨김 | 403 및 protected write 없음 | 권한 우선순위·scope OQ-AUTH-001 |
| 메뉴·코드 정의 | 관리 접근 허용된 저장 | 해당 정의만 재표시 | 비간섭 table snapshot 유지 | 삭제·참조 보관 OQ-DATA-002 |

## requirement-to-screen trace

| source_id | canonical_id | acceptance criterion id | 화면ID | route | OpenAPI operationId/path | UI verification |
| --- | --- | --- | --- | --- | --- | --- |
| CMN-FR-001 | REQ-001 | CMN-FR-001-AC-01 | SCR-USERS | /admin/users | searchUsers | 사용자 관리에서 검색 조건을 입력하고 검색한다 → 교번·성명·소속·직급·재직상태·역할·사용여부 중 하나 이상의 조건으로 검색하면 조건에 맞는 사용자만 표시된다. |
| CMN-FR-001 | REQ-002 | CMN-FR-001-AC-02 | SCR-USERS | /admin/users | searchUsers | 사용자 관리에서 검색 조건을 입력하고 검색한다 → 사용자 목록에 보직·퇴직일자·최종 동기화일시를 포함한 명시 항목이 표시된다. |
| CMN-FR-001 | REQ-003 | CMN-FR-001-AC-03 | SCR-USERS | /admin/users | updateUserSettings | 사용자 관리에서 해당 편집값을 저장하고 같은 대상을 다시 조회한다 → 시스템 사용여부 또는 업무 역할을 변경하고 다시 조회하면 변경값이 유지된다. |
| CMN-FR-002 | REQ-005 | CMN-FR-002-AC-01 | SCR-ORGANIZATIONS | /admin/organizations | getOrganizationRelations | 조직 관리에서 식별정보·조회 조건을 선택하여 조회한다 → 조직코드로 조회하면 해당 조직의 조직구분과 현재 상위·하위 관계가 표시된다. |
| CMN-FR-002 | REQ-006 | CMN-FR-002-AC-02 | SCR-ORGANIZATIONS | /admin/organizations | saveOrganizationRelation | 조직 관리에서 해당 편집값을 저장하고 같은 대상을 다시 조회한다 → 상위조직 변경 시 적용 시작일·종료일과 변경된 상위조직이 함께 저장된다. |
| CMN-FR-002 | REQ-007 | CMN-FR-002-AC-03 | SCR-ORGANIZATIONS | /admin/organizations | listOrganizationRelationHistory | 조직코드를 조회한 뒤 이력 조회를 선택한다 → 변경 이후에도 변경 전 조직관계와 적용기간을 이력으로 조회할 수 있다. |
| CMN-FR-005 | REQ-009 | CMN-FR-005-AC-01 | SCR-ROLES | /admin/roles | saveRole | 역할 관리에서 해당 편집값을 저장하고 같은 대상을 다시 조회한다 → 역할별 목적·부여 기준·데이터 범위 기본값을 저장하고 재조회할 수 있다. |
| CMN-FR-006 | REQ-012 | CMN-FR-006-AC-01 | SCR-USER-ROLES | /admin/user-roles | saveUserRoleAssignment | 사용자 역할 관리에서 부여할 역할·각 기간을 입력하고 부여한다 → 한 사용자에게 둘 이상의 역할을 서로 다른 유효기간으로 부여할 수 있다. |
| CMN-FR-006 | REQ-013 | CMN-FR-006-AC-02 | SCR-USER-ROLES | /admin/user-roles | saveUserRoleAssignment | 사용자 역할 관리에서 대상·역할·기간·승인자를 입력하고 부여·변경·회수를 각각 저장한다 → 역할을 부여·변경·회수하면 대상 사용자·역할·유효기간·승인자가 함께 저장된다. |
| CMN-FR-006 | REQ-014 | CMN-FR-006-AC-03 | SCR-USER-ROLES | /admin/user-roles | listUserRoleAssignments | 사용자 역할 관리에서 식별정보·조회 조건을 선택하여 조회한다 → 보직 기반 역할과 수동 역할을 조회 결과에서 구분할 수 있다. |
| CMN-FR-007 | REQ-016 | CMN-FR-007-AC-01 | SCR-MENU-PERMISSIONS | /admin/menu-permissions | saveMenuPermission | 메뉴 권한 관리에서 해당 편집값을 저장하고 같은 대상을 다시 조회한다 → 역할·조직·사용자와 메뉴를 선택해 접근 허용 여부를 저장하고 재조회할 수 있다. |
| CMN-FR-013 | REQ-019 | CMN-FR-013-AC-01 | SCR-MENU-STRUCTURE | /admin/menu-structure | saveMenuStructure | 메뉴 구조 관리에서 해당 편집값을 저장하고 같은 대상을 다시 조회한다 → 메뉴별 부모메뉴와 계층단계를 저장하고 계층구조로 재조회할 수 있다. |
| CMN-FR-013 | REQ-020 | CMN-FR-013-AC-02 | SCR-MENU-STRUCTURE | /admin/menu-structure | saveMenuStructure | 메뉴 구조 관리에서 해당 편집값을 저장하고 같은 대상을 다시 조회한다 → 동일 계층의 표시순서를 변경하면 사용자 메뉴에 변경된 순서가 적용된다. |
| CMN-FR-014 | REQ-022 | CMN-FR-014-AC-01 | SCR-MENU-INFO | /admin/menu-info | saveMenuExecutionInfo | 메뉴 정보 관리에서 해당 편집값을 저장하고 같은 대상을 다시 조회한다 → 메뉴의 실행정보를 저장하고 동일한 값으로 재조회할 수 있다. |
| CMN-FR-014 | REQ-023 | CMN-FR-014-AC-02 | SCR-MENU-INFO | /admin/menu-info | getAccessibleMenus; 링크 이동 자체 N/A | 사용 가능한 메뉴 링크를 선택한다 → 사용 가능한 메뉴를 선택하면 연결된 화면ID와 URL의 화면이 실행된다. |
| CMN-FR-016 | REQ-025 | CMN-FR-016-AC-01 | SCR-CODE-GROUPS | /admin/code-groups | saveCodeGroup | 코드그룹 관리에서 해당 편집값을 저장하고 같은 대상을 다시 조회한다 → 그룹ID·명칭·설명·관리부서를 저장하고 재조회할 수 있다. |
| CMN-FR-016 | REQ-026 | CMN-FR-016-AC-02 | SCR-CODE-GROUPS | /admin/code-groups | listCodeGroups | 코드그룹 관리에서 식별정보·조회 조건을 선택하여 조회한다 → 서로 다른 그룹ID의 코드그룹이 독립적으로 조회된다. |
| CMN-FR-017 | REQ-028 | CMN-FR-017-AC-01 | SCR-DETAIL-CODES | /admin/detail-codes | listDetailCodes | 상세코드 관리에서 식별정보·조회 조건을 선택하여 조회한다 → 코드그룹을 선택하면 해당 그룹의 상세코드만 조회된다. |
| CMN-FR-017 | REQ-029 | CMN-FR-017-AC-02 | SCR-DETAIL-CODES | /admin/detail-codes | saveDetailCode | 상세코드 관리에서 해당 편집값을 저장하고 같은 대상을 다시 조회한다 → 상세코드의 명시 필드를 저장하고 재조회할 수 있다. |
| CMN-FR-017 | REQ-030 | CMN-FR-017-AC-03 | SCR-DETAIL-CODES | /admin/detail-codes | saveDetailCode | 상세코드 관리에서 해당 편집값을 저장하고 같은 대상을 다시 조회한다 → 정렬순서를 변경하면 화면 선택값이 같은 순서로 제공된다. |
| CMN-FR-017 | REQ-031 | CMN-FR-017-AC-04 | SCR-DETAIL-CODES | /admin/detail-codes | listDetailCodes | 상세코드 관리에서 식별정보·조회 조건을 선택하여 조회한다 → 상위코드를 지정한 코드는 계층관계로 식별할 수 있다. |

## Component Map

| component | 책임 | screen |
| --- | --- | --- |
| AppShell | sidebar/header·현재 업무·3단계 menu 렌더링 | 공통 supporting; 독립 화면 아님 |
| DataStateBoundary | loading/empty/error/permission/success·원래 동작 재시도 | 모든 primary 화면 |
| ReadonlyPersonnelTable | 조건 검색·원천 readonly·로컬 enabled 편집 | SCR-USERS |
| OrganizationPeriodEditor | 현재 관계·기간 편집·이력 하단 | SCR-ORGANIZATIONS |
| RoleDefinitionEditor | 목적·기준·범위 정의 | SCR-ROLES |
| AssignmentPeriodEditor | 다중 역할·기간·승인자·출처 | SCR-USER-ROLES |
| MenuPermissionEditor | 대상·메뉴 단일 허용 설정 | SCR-MENU-PERMISSIONS |
| MenuTreeEditor | 부모·계층·같은 부모 순서 | SCR-MENU-STRUCTURE |
| MenuExecutionEditor | 화면ID/URL·사용자 선택 링크 | SCR-MENU-INFO |
| CodeGroupEditor | 그룹 정의 | SCR-CODE-GROUPS |
| GroupedCodeTree | 그룹별 정렬·상위코드 | SCR-DETAIL-CODES |

## 레퍼런스 시각 규칙 및 UI 구현 합격 체크리스트

- [ ] React/TypeScript/Vite와 아홉 route·고유 wireframe을 유지한다.
- [ ] sidebar/header를 제안한 3단계 IA와 연결하고 도메인별 본문을 다르게 구성한다. KPI card·dashboard 화면은 만들지 않는다.
- [ ] table/form 밀도·간격·색상 토큰·모바일 누적 레이아웃은 승인된 reference 값을 구현과 검증에 공통 적용한다. 미제공 값은 OQ-UI-004이며 임의 브랜드 완료로 처리하지 않는다.
- [ ] wide 인사 목록은 행 확장 없이 하단 편집을 사용하고, 관계·이력은 아래로 스크롤, 메뉴·코드는 계층 표기를 사용한다.
- [ ] 검색·저장·취소·회수·링크·행 선택이 Screen Action Mapping과 일치한다. 확인 modal과 추가 확인 클릭은 만들지 않는다.
- [ ] 입력 중 값·validation·중복 저장 제어, 모든 loading/empty/error/permission/success를 구현한다.
- [ ] 원천 인사정보는 readonly이고 server 변경 거부는 별도 API 시험으로 검증한다.
- [ ] 그림의 API row placeholder는 seed/enum이 아니며 실제 API/DB 응답만 렌더링한다. 하드코딩 사람·조직·역할명·교번·상태값을 seed/test에 전파하지 않는다.
- [ ] 브라우저 시험은 위 trace의 ui-primary만 소유하고 supporting·API·data·infra 책임을 억지로 화면 coverage에 넣지 않는다.
- [ ] 후보 조회의 permissions 및 메뉴 계층 완전성 OQ-AUTH-002를 검토한 후 구현 준비를 승인한다.

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
| REQ-001 | covered | SCR-USERS · trace CMN-FR-001-AC-01 · 사용자 조건 검색 |
| REQ-002 | covered | SCR-USERS · trace CMN-FR-001-AC-02 · 사용자 인사정보 목록 표시 |
| REQ-003 | covered | SCR-USERS · trace CMN-FR-001-AC-03 · 사용여부·업무 역할 변경 유지 |
| REQ-004 | non-functional | 원천 인사정보 변경 거부는 contracts/openapi.yaml의 updateUserSettings 및 backend 시험 소유이며 UI-primary 배정 대상 아님. |
| REQ-005 | covered | SCR-ORGANIZATIONS · trace CMN-FR-002-AC-01 · 조직 현재 관계 조회 |
| REQ-006 | covered | SCR-ORGANIZATIONS · trace CMN-FR-002-AC-02 · 상위조직 적용기간 저장 |
| REQ-007 | covered | SCR-ORGANIZATIONS · trace CMN-FR-002-AC-03 · 변경 전 조직관계 이력 조회 |
| REQ-008 | data-constraint | 조직변경의 사용자정보 비간섭는 data-model.md Validation Rule Matrix의 table 비간섭 시험 소유이며 UI-primary 배정 대상 아님. |
| REQ-009 | covered | SCR-ROLES · trace CMN-FR-005-AC-01 · 역할 정의 저장·재조회 |
| REQ-010 | non-functional | 역할 식별정보 구분는 contracts/openapi.yaml의 saveRole 및 backend 시험 소유이며 UI-primary 배정 대상 아님. |
| REQ-011 | data-constraint | 역할 정의와 부여내역 분리는 data-model.md Validation Rule Matrix의 table 비간섭 시험 소유이며 UI-primary 배정 대상 아님. |
| REQ-012 | covered | SCR-USER-ROLES · trace CMN-FR-006-AC-01 · 다중 역할·상이한 기간 부여 |
| REQ-013 | covered | SCR-USER-ROLES · trace CMN-FR-006-AC-02 · 부여·변경·회수 승인자 기록 |
| REQ-014 | covered | SCR-USER-ROLES · trace CMN-FR-006-AC-03 · 역할 발생 출처 구분 조회 |
| REQ-015 | non-functional | 종료된 역할 현재 적용 제외는 contracts/openapi.yaml의 saveUserRoleAssignment 및 backend 시험 소유이며 UI-primary 배정 대상 아님. |
| REQ-016 | covered | SCR-MENU-PERMISSIONS · trace CMN-FR-007-AC-01 · 메뉴 접근 허용 설정 저장 |
| REQ-017 | screen-only | AppShell sidebar가 getAccessibleMenus의 허용 메뉴만 표시; 신규 독립 화면/primary 브라우저 항목 아님. |
| REQ-018 | non-functional | 비허용 URL 서버 차단는 contracts/openapi.yaml의 saveMenuPermission 및 backend 시험 소유이며 UI-primary 배정 대상 아님. |
| REQ-019 | covered | SCR-MENU-STRUCTURE · trace CMN-FR-013-AC-01 · 부모메뉴·계층 저장 |
| REQ-020 | covered | SCR-MENU-STRUCTURE · trace CMN-FR-013-AC-02 · 동일 계층 표시순서 적용 |
| REQ-021 | data-constraint | 다른 부모 아래 순서 보존는 data-model.md Validation Rule Matrix의 table 비간섭 시험 소유이며 UI-primary 배정 대상 아님. |
| REQ-022 | covered | SCR-MENU-INFO · trace CMN-FR-014-AC-01 · 메뉴 실행정보 저장 |
| REQ-023 | covered | SCR-MENU-INFO · trace CMN-FR-014-AC-02 · 사용 가능한 메뉴 화면 실행 |
| REQ-024 | data-constraint | 실행정보 변경의 권한 비간섭는 data-model.md Validation Rule Matrix의 table 비간섭 시험 소유이며 UI-primary 배정 대상 아님. |
| REQ-025 | covered | SCR-CODE-GROUPS · trace CMN-FR-016-AC-01 · 코드그룹 정의 저장 |
| REQ-026 | covered | SCR-CODE-GROUPS · trace CMN-FR-016-AC-02 · 그룹ID별 독립 조회 |
| REQ-027 | data-constraint | 그룹 수정의 상세코드 비간섭는 data-model.md Validation Rule Matrix의 table 비간섭 시험 소유이며 UI-primary 배정 대상 아님. |
| REQ-028 | covered | SCR-DETAIL-CODES · trace CMN-FR-017-AC-01 · 그룹별 상세코드 조회 |
| REQ-029 | covered | SCR-DETAIL-CODES · trace CMN-FR-017-AC-02 · 상세코드 필드 저장 |
| REQ-030 | covered | SCR-DETAIL-CODES · trace CMN-FR-017-AC-03 · 선택값 정렬순서 반영 |
| REQ-031 | covered | SCR-DETAIL-CODES · trace CMN-FR-017-AC-04 · 상위코드 계층 식별 |
| REQ-032 | non-functional | React/TypeScript/Vite 번들 선택은 frontend/package.json의 빌드 의존성 계약이며 REST 입출력이 아니다. |
| REQ-033 | non-functional | Java 17/Spring Boot 선택은 backend/pom.xml과 런타임의 컴파일 계약이다. |
| REQ-034 | non-functional | PostgreSQL 선택은 data-model.md의 매핑·DB 접속 대상이지 신규 업무 API가 아니다. |
| REQ-035 | non-functional | Docker Compose 실행 방식은 infra/docker-compose.yml의 서비스 연결 책임이다. |
| REQ-036 | non-functional | 동기 blocking MyBatis는 plan.md marker의 실행 결정으로 모든 요청 처리 내부에 적용된다. |
| REQ-037 | non-functional | BOM은 frontend/package.json/backend/pom.xml/DB 이미지 버전 검증으로 충족한다. |
| REQ-038 | non-functional | backend 디렉터리 산출 여부는 저장소 파일 검사 대상이다. |
| REQ-039 | non-functional | frontend 디렉터리 산출 여부는 저장소 파일 검사 대상이다. |
| REQ-040 | non-functional | Compose 파일 위치는 infra/docker-compose.yml 파일 존재와 config 검사로 확인한다. |
| REQ-041 | non-functional | /api/health는 quickstart의 서비스 probe이며 제품 화면으로 만들지 않는다. |
| REQ-042 | non-functional | 레퍼런스의 sidebar/header/밀도/토큰/반응형은 ui-design.md 공통 시각 설계이며 API를 만들지 않는다. |
| REQ-043 | non-functional | spec.md 기술스택 계약에서 백엔드·프론트엔드 범위를 문서화한다. |
| REQ-044 | non-functional | plan.md Project Structure와 생성 순서가 소유하는 구현 배치 계약이다. |
| REQ-045 | non-functional | tasks.md Setup/Foundational/US 단계가 backend/frontend/infra 작업을 배정한다. |
| REQ-046 | non-functional | ui-design.md 레퍼런스 구현 검수 기준은 화면 공통 스타일 책임이다. |
| REQ-047 | non-functional | plan.md의 exact marker JSON 보존은 문서 정본 검증 대상이다. |
| REQ-048 | non-functional | data-model.md Entity Registry와 tasks.md migration 작업이 스키마 기준을 충족한다. |
| REQ-049 | non-functional | quickstart.md API/UI 동시 smoke 절차와 seed fixture로 검증한다. |
| REQ-050 | non-functional | 각 build context의 .dockerignore 제외 집합을 infra 검사에서 확인한다. |

## Menu Information Architecture

메뉴 이름은 화면 목록의 menu_path를 분해한 제안값이다(OQ-UI-001). 모든 화면은 하나의 leaf로 연결되며 모달 leaf는 없다. 메뉴 외 진입 화면을 추가할 경우 OQ-UI-NNN에 근거와 접근 경로를 기록한다.

| 대메뉴 | 중메뉴 | 소메뉴(leaf) | 화면ID | route | role | grouping rationale | 기본 동작 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 공통 관리 | 인사·권한 | 사용자 관리 | SCR-USERS | /admin/users | 관리권한 확인 주체(OQ-AUTH-001) | 인사·조직·권한 구성 여정; 명명 승인 OQ-UI-001 | 조건 입력·검색 → 목록 선택 → 사용여부 저장; 업무역할은 사용자 역할 관리로 이동 |
| 공통 관리 | 인사·권한 | 조직 관리 | SCR-ORGANIZATIONS | /admin/organizations | 관리권한 확인 주체(OQ-AUTH-001) | 인사·조직·권한 구성 여정; 명명 승인 OQ-UI-001 | 조직코드 조회 → 상위조직·기간 저장 → 변경 전 관계 이력 조회 |
| 공통 관리 | 인사·권한 | 역할 관리 | SCR-ROLES | /admin/roles | 관리권한 확인 주체(OQ-AUTH-001) | 인사·조직·권한 구성 여정; 명명 승인 OQ-UI-001 | 역할 식별정보 선택 → 목적·기준·범위 정의 저장 → 같은 역할 재조회 |
| 공통 관리 | 인사·권한 | 사용자 역할 관리 | SCR-USER-ROLES | /admin/user-roles | 관리권한 확인 주체(OQ-AUTH-001) | 인사·조직·권한 구성 여정; 명명 승인 OQ-UI-001 | 사용자 조회 → 기간·승인자와 부여·변경·회수 → 부여 목록 재조회 |
| 공통 관리 | 메뉴 | 메뉴 권한 관리 | SCR-MENU-PERMISSIONS | /admin/menu-permissions | 관리권한 확인 주체(OQ-AUTH-001) | 메뉴 대상·구조·실행 여정; 명명 승인 OQ-UI-001 | 대상·메뉴 선택 → 현재 설정 조회 → 접근 허용 저장 → 같은 설정 확인 |
| 공통 관리 | 메뉴 | 메뉴 구조 관리 | SCR-MENU-STRUCTURE | /admin/menu-structure | 관리권한 확인 주체(OQ-AUTH-001) | 메뉴 대상·구조·실행 여정; 명명 승인 OQ-UI-001 | 메뉴 계층 조회 → 메뉴 선택 → 부모·계층·표시순서 저장 → 해당 부모 순서 확인 |
| 공통 관리 | 메뉴 | 메뉴 정보 관리 | SCR-MENU-INFO | /admin/menu-info | 관리권한 확인 주체(OQ-AUTH-001) | 메뉴 대상·구조·실행 여정; 명명 승인 OQ-UI-001 | 메뉴 실행정보 저장 → 사용 가능한 메뉴 링크 선택 → 연결 화면으로 이동 |
| 공통 관리 | 코드 | 코드그룹 관리 | SCR-CODE-GROUPS | /admin/code-groups | 관리권한 확인 주체(OQ-AUTH-001) | 코드그룹과 소속 상세코드 여정; 명명 승인 OQ-UI-001 | 그룹ID 조회 → 그룹 정의 저장 → 독립 그룹 값 재조회 |
| 공통 관리 | 코드 | 상세코드 관리 | SCR-DETAIL-CODES | /admin/detail-codes | 관리권한 확인 주체(OQ-AUTH-001) | 코드그룹과 소속 상세코드 여정; 명명 승인 OQ-UI-001 | 코드그룹 선택·조회 → 상세코드 순서·상위코드 저장 → 그룹별 계층·선택값 확인 |
