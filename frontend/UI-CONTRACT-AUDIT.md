# UI 사용자 기능 계약 느슨한 점검

## 범위와 checklist

`.aiops-spec/ui-design.md`의 9개 화면, action/binding/state와 실제 page, 공유 엔진,
API client/types 및 backend controller/service의 연결을 대조한다. ASCII 위치·간격·컬럼 순서는
판정 대상에서 제외한다. backend 및 staged spec은 변경하지 않는다.

| 화면 | 확인할 사용자 계약 | 소스 관찰 |
| --- | --- | --- |
| 사용자 | 7개 검색 조건, 인사 readonly, user_id/enabled만 저장, 역할 문맥 이동 | 연결됨 |
| 조직 | 조직 원천 readonly, 상위·기간 저장, 독립 이력 조회 | 연결됨; 기간/관계 정책 gate 유지 |
| 역할 | saved role_id, 목적·기준·범위, 취소 | 연결됨; 식별자 잠금 유지 |
| 사용자 역할 | 사용자 문맥, 별도 승인자, 부여·변경·회수, origin/is_current 제외 | 연결됨; 승인/기간/회수 정책 gate 유지 |
| 메뉴 권한 | 기존 permission_id, 신규 UUID, 미설정≠불허, 대상 null | 기존 결과 미선택 상태에서 신규 폼이 열리는 drift 보강 대상 |
| 메뉴 구조 | saved menu_id, 부모·정수 계층/순서, 서버 순서 | 연결됨; 루트/순환 정책 미정 |
| 메뉴 정보 | saved menu_id, 실행정보 저장, 허용 메뉴 링크 | 연결됨; URL 정책 gap 유지 |
| 코드그룹 | saved group_id, 정의 편집·취소 | 연결됨 |
| 상세코드 | group/code identity, 그룹 후보, 순서·부모 변경 | 연결됨; 미변경 부모 생략은 이미 구현됨 |

## 최소 보강 계획

1. GET ApiError의 field_errors를 검색 필드에 표시한다. 편집 필드 오류와 별도 상태로 관리하여
   검색/저장에 같은 field 이름이 있어도 오류가 잘못 표시되지 않게 한다.
2. 메뉴 권한 결과가 있을 때는 기존 행을 선택해야 편집하도록 한다. 신규 폼은 성공한 조회의
   전체 건수가 0일 때만 제공한다. 동일 설정 판정/조합 정책은 새로 만들지 않는다.
3. 회귀 테스트를 먼저 실행하여 실제 실패를 확인한 뒤 공유 엔진만 최소 수정한다.

## 남은 gap / 변경하지 않는 경계

- 저장 후 현재 검색/페이지에서 제외되는 행은 선택이 사라질 수 있다. 사용자 enabled 변경,
  메뉴 부모/순서 변경 등이 해당한다. 저장 대상의 별도 확인 조회/목록 밖 상세 유지 UX는 미보강이다.
- 메뉴 접근 재조회가 실패하면 App이 fail-closed로 페이지를 숨겨 편집 문맥이 사라진다.
  이전 접근 판정을 재사용해 보호 내용을 보여주는 우회는 추가하지 않는다.
- 설계 binding의 일부 식별자는 표시·편집으로 쓰였지만 실제 저장 identity와 lifecycle 의미가
  분리되지 않았다. 기존 saved-ID 잠금을 해제하거나 upsert API만 보고 생성 CTA를 추가하지 않는다.
- 권한 대상 조합·동일성·우선순위, 후보 API 조회 권한 범위는 OQ-AUTH-001/002 미확정이다.
  0건 조회가 정책상 신규 저장 허용을 보증하지 않으며 실제 저장은 서버 검증을 따른다.
- 조직 현재관계/기간, 역할 승인·회수, 운영 인증·권한 adapter는 정책 bean 승인이 필요하다.
- 메뉴 루트/순환, 상세코드 부모 해제 표현, 외부/미등록 실행 URL은 미확정이다.
  sidebar의 고정 route 이동은 응답 URL의 query/fragment를 보존하지 않는다.
- 실제 DB/기관 인증/브라우저 acceptance는 이 문서의 소스 점검과 구분한다.

## 실제 보강 및 검증

- `ResourcePage.tsx`: 조회 오류용 filterErrors를 저장 오류와 분리하고 해당 컨트롤에
  field 메시지, aria-invalid, aria-describedby를 연결했다. 조회 재시도 시 오류를 초기화하고
  검색 입력은 유지한다. 저장 이후 재조회 오류도 편집 필드 오류로 오인하지 않는다.
- 메뉴 권한: 기존 결과 미선택 상태에는 신규 폼을 열지 않으며, 신규 저장 함수에도
  성공한 조회/전체 0건 guard를 둔다. 기존 행 선택 후 saved permission_id 경로는 유지한다.
- 테스트 4건: 검색 필드 오류/GET 재시도, 기존 설정 선택·saved ID 저장, 빈 페이지이지만
  total>0인 경계, 실제 0건의 UUID 재시도·null 대상·저장 오류 분리를 확인한다.

명령은 `frontend/`에서 실행했다. 기존 node_modules를 사용했고 설치·lockfile 변경은 없다.

1. RED: `npm test -- tests/unit/ui-contract.test.tsx --maxWorkers=1 --minWorkers=1`
   → 3 failed / 1 passed. 실제 누락된 검색 aria-invalid와 불필요한 신규 저장 폼에서 실패했다.
2. 최소 수정 후 같은 명령 → 4 passed.
3. 첫 `npm test` → 48 passed / 1 failed. 동일 소스로
   `npm test -- --maxWorkers=1 --minWorkers=1` → 같은 48 passed / 1 failed.
   기존 미설정 권한 fixture가 data=[]와 total=1을 함께 반환하고 있었다.
   해당 경우만 total=0으로 보정했다. 테스트를 skip하거나 assertion을 약화하지 않았다.
4. 최종 `npm test` → 6 files / 49 tests passed.
5. `npm run typecheck` → 통과. 최종 `npm run build` → TypeScript 및 Vite production build 통과.
6. 루트 `git diff --check` → 통과. frontend 전체가 기존부터 untracked이므로 이 명령만으로
   변경 소스가 검증된 것은 아니다. 이번 작성/수정한 네 파일을 별도 검사하여 trailing whitespace 0,
   각 파일 최대 줄 길이 120 이하를 확인했다.

React Router v7 future flag 경고는 남는다. 브라우저 E2E, 실제 DB/기관 인증·정책 검증은
실행하지 않았다. 테스트의 fetch 응답은 계약 fixture이며 운영 API 실행 증거가 아니다.
gap을 pipeline 실패 조건으로 추가하지 않았다.

## 이번 변경 파일

- `frontend/src/components/ResourcePage.tsx`
- `frontend/tests/unit/ui-contract.test.tsx` (신규)
- `frontend/tests/unit/app.test.tsx` (빈 권한 fixture의 total만 보정)
- `frontend/UI-CONTRACT-AUDIT.md` (신규)

빌드 결과는 기존 ignored `frontend/dist/`에 재생성되었다.
backend/API/schema/infra/staged spec 및 기존의 다른 변경 파일은 수정하지 않았다.
