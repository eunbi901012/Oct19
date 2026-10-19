# 화면별 계약/API 점검

## 근거와 경계

수정 전 `.aiops-spec/ui-design.md` 전체(9개 wireframe·action·binding·상태·OQ),
`data-model.md`, `contracts/openapi.yaml`, 9개 page, API client/types,
`ManagementController`, `CatalogService`, `InputValidator`, `PolicyConfiguration`를 대조했다.
기존 9 route와 공유 ResourcePage/App/CSS/API는 수정하지 않는다.
아래는 소스 계약 점검이며 실제 DB/기관 인증 acceptance 완료를 뜻하지 않는다.

## 화면별 짧은 checklist

| 화면 / route | 확인한 계약 / 유지할 사항 | 판정 |
| --- | --- | --- |
| 사용자 `/admin/users` | GET/PUT `/api/users`, 7개 검색 조건·10개 인사 열, saved user_id+enabled만 저장, KORUS readonly, 역할 관리 문맥 링크 | page 계약 일치; 취소·권한 숨김은 공유 엔진 위임 |
| 조직 `/admin/organizations` | GET/PUT `/api/organizations`, 조직 원천 readonly, 상위·기간 묶음 저장, 별도 GET `/api/organization-relation-history` | page 계약 일치; 기간/소유권 gate 유지 |
| 역할 `/admin/roles` | GET/PUT `/api/roles`, 역할 ID 잠금, 목적·기준·범위 정의 및 명시적 빈 문자열 | page 계약 일치 |
| 사용자 역할 `/admin/user-roles` | GET/PUT `/api/user-role-assignments`, 문맥 user_id, 별도 승인자, 부여 UUID/변경·회수 saved ID, origin/is_current 제외 | page 계약 일치; 승인·회수 gate 유지 |
| 메뉴 권한 `/admin/menu-permissions` | GET/PUT `/api/menu-permissions`, 대상 null·최소 한 대상, boolean allowed, saved ID/신규 UUID, 미설정≠불허 | page 계약 일치; 조합 정책 gate 유지 |
| 메뉴 구조 `/admin/menu-structure` | GET/PUT `/api/menu-structure`, saved menu_id, 부모·정수 계층/순서, 서버 순서 | page 계약 일치; 루트/순환 정책 미정 |
| 메뉴 정보 `/admin/menu-info` | GET/PUT `/api/menu-execution-info`, saved menu_id, screen_id/url, 별도 accessible-menus 탐색 | page 계약 일치; 동일 출처 안전 링크·서버 판정 유지 |
| 코드그룹 `/admin/code-groups` | GET/PUT `/api/code-groups`, group_id 잠금·정의만 저장, 설명 명시적 clear | page 계약 일치 |
| 상세코드 `/admin/detail-codes` | GET/PUT `/api/detail-codes`, group_id 필수·복합 identity 잠금, 서버 순서·그룹 후보, optional parent_code | 결함 발견: 미변경 상위코드까지 항상 전송. 특히 기존 루트의 빈 문자열이 backend 참조 검증에 걸려 정렬순서만 저장해도 실패 가능 |

## 좁은 수정 계획 (regression 먼저)

상세코드에서 `parent_code`가 선택 응답과 같은 경우만 생략해 backend의 기존 값 보존 경로를 사용한다.
변경한 상위코드(빈 문자열 포함)는 그대로 전송한다. 빈 문자열을 임의 루트 정책으로 해석하거나
null로 치환하지 않는다. 취소 후 baseline, readonly identity, 정수 payload를 regression으로 확인한다.

## 정책·계약 갭 (프론트에서 승인하거나 우회하지 않음)

- 기관 인증/권한 adapter 미설정 시 production fallback은 401 또는 policy-pending으로 닫힌다.
  UI 계약 테스트 성공은 실제 로그인·DB 쓰기 성공이 아니다.
- 조직 현재관계/기간·사용자 현재역할 계산, 조직관계 저장, 역할 부여·변경·회수,
  권한 대상 조합은 승인된 정책 bean이 필요하다(OQ-AUTH/ORG/ROLE).
- 상세코드 `parent_code` 생략은 기존 부모 보존으로 실제 지원된다. 명시적 빈 부모의 해제는
  참조 검증상 빈 code가 없는 경우 거절된다. root-code 신규 표현/해제 정책은 미확정이다.
- 메뉴 루트 부모·순환, 조직 이력의 현재/변경 전 구분, 메뉴 후보 조회 범위,
  상세코드 추가 필드와 시각 레퍼런스 승인은 해당 OQ 해결이 필요하다.
- 실행 route/method 및 snake_case DTO는 controller/client와 OpenAPI 간 일치한다.
  일반 `filter`는 backend가 미승인으로 거절하므로 기존 명명 query만 유지한다.

## 실행 기록

- node_modules 준비 확인 후 설치 없이 테스트 실행.
- RED: `npm test -- tests/unit/pages-audit.test.tsx --maxWorkers=1 --minWorkers=1`
  → fixture selector 보정 후 **2 failed / 2 passed**. 미변경 `parent_code`의 불필요 전송을 실제 assertion diff로 확인.
- GREEN: 같은 명령 → **4 passed** (루트 순서 변경, 실제 부모 변경, 취소 복원,
  명시적 해제의 서버 오류 보존). fetch stub은 CatalogService의 참조 검증/생략 보존 계약을 재현한 것이며 실제 backend 실행 결과가 아니다.
- 전체 `npm test -- --maxWorkers=1 --minWorkers=1` → **43 passed / 2 failed (45)**.
  실패는 소유 범위 밖 기존 `app.test.tsx`의 역할/그룹ID label이 filter와 editor에 중복 매칭되는 selector 문제다.
  해당 테스트/공유 컴포넌트는 수정하지 않았다. 신규 audit 4건은 전체 실행에서도 통과했다.
- `npx tsc --noEmit` → exit 2. 기존 `app.test.tsx`, `states.test.tsx`, `ux.test.tsx`의
  ByRoleOptions에 지원되지 않는 `exact` 옵션 오류가 남는다. 신규 audit의 같은 옵션은 제거하여
  재검사 출력에 audit/page 관련 오류가 없음을 확인했다. 전체 typecheck 성공으로 보고하지 않는다.
- React Router v7 future flag warning은 남아 있으며 이 작업 범위에서 변경하지 않는다.
