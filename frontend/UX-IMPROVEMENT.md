# UI 개선 계약 체크리스트

## 범위 및 근거

- `.aiops-spec/ui-design.md` 화면 목록과 기능 계약, `default-ui-style.md`의 Civic Blue 시각 규칙 적용.
- 기존 React Router, 순수 CSS, React 18/TypeScript/Vite를 유지한다. 새 UI 라이브러리를 추가하지 않는다.
- backend controller/service와 frontend client/type에 있는 GET/PUT만 사용한다. DELETE 계약은 없으므로
  삭제 버튼을 발명하지 않는다. 회수는 사용자 역할 PUT 명령이다.
- 원천 인사정보, 저장 식별자, 역할 출처 및 현재 적용 판정은 응답 기반 readonly다.

## 화면별 구현 전 체크리스트

| route | 목적 / archetype | CTA / payload / 필드 | 검증·성공·제약 |
| --- | --- | --- | --- |
| `/admin/users` | 검색·목록·설정 | 7개 조건 검색, enabled 저장, 역할 화면 이동 | user_id 유지, 인사정보 제외, 재조회·취소 |
| `/admin/organizations` | 관계·기간 form | 조직 조회, parent_code·valid_from·valid_to 저장, 독립 이력 조회 | 조직 identity 유지, 관계·이력 분리, 기간정책 서버 위임 |
| `/admin/roles` | 정의 editor | role_id 조회, purpose·grant_criteria·default_data_scope 저장 | 기존 role_id 잠금, 정의만 저장·재조회 |
| `/admin/user-roles` | 기간별 부여 form | user_id 조회, 역할·기간·approver_id로 부여/변경/회수 | assignment_id 유지, 승인자 자동입력 금지, 출처 제외 |
| `/admin/menu-permissions` | 단일 대상 permission | 대상·menu_id 조회, allowed 저장 | 대상 최소 하나, 미설정/false 구분, 접근결정 재조회 |
| `/admin/menu-structure` | hierarchy editor | parent_menu_id 조회, 부모·level·display_order 저장 | menu_id 잠금, 정수 검증, 서버 순서·접근결정 재조회 |
| `/admin/menu-info` | 실행정보 editor | menu_id 조회, screen_id·url 저장, 허용 링크 이동 | menu_id 잠금, 안전한 서버 URL만 실행, 접근결정 재조회 |
| `/admin/code-groups` | 검색·정의 editor | group_id 조회, name·description·managing_department 저장 | group_id 잠금, 그룹만 저장, 상세코드 이동 |
| `/admin/detail-codes` | 그룹별 hierarchy editor | group_id 조회, sort_order·parent_code 저장 | 기존 복합키 잠금, 그룹별 후보·서버 순서, 재조회 |

## 공통 보완 목표

- [x] Civic Blue OKLCH 토큰, 밝은 sidebar, sticky header, 제목 accent band, 충분한 여백.
- [x] 도메인별 SVG 아이콘, 상태 badge, 식별자 typography, 입력 focus/transition/reduced-motion.
- [x] skeleton loading과 안내가 있는 empty state, permission/error/success의 시각적 구분.
- [x] 모바일 메뉴의 aria-expanded, 닫기·Escape·배경 닫기, route 이동 시 닫기.
- [x] 검색·선택·취소·저장 및 실패 후 draft 유지, 401/403 fail-closed 보존.
- [x] 회귀 테스트·타입검사·빌드 실행 결과를 최종 기록한다.

## 정책 경계

인증·권한 우선순위, 기간 중복/순환, 승인 단계, 삭제·보관 정책은 frontend에서 확정하지 않는다.
실서비스 인증/DB fixture 없이는 실제 backend 연동 E2E 완료로 보고하지 않는다.

## 구현 대조 및 최종 검증

- 9개 route는 이미 구현되어 있어 새 route/메뉴나 삭제 API를 추가하지 않았다.
- 공통 스타일 개선을 모든 화면에 적용하고 기존 관계 form·독립 이력·계층 화면 구조를 유지했다.
- 상세코드 미변경 `parent_code`를 update에서 생략하도록 수정했다. 기존 부모 보존은
  `CatalogService`의 optional 참조 검증·병합 경로에 근거한다. 변경·취소·명시적 해제 회귀 4건 통과.
- `npm test -- tests/unit/ux.test.tsx`: 신규 UX 3건 통과.
- 초기 전체 검사에서 기존 테스트의 미지원 ByRoleOptions `exact`와 중복 label 조회가 발견되었다.
  role/name 기반 정확한 selector로 보정했으며 production 계약은 테스트에 맞춰 바꾸지 않았다.
- 최종 `npm test`: 5개 파일, 45개 테스트 통과. React Router v7 future flag warning만 남는다.
- 최종 `npm run typecheck`: exit 0.
- 최종 `npm run build`: exit 0, Vite production bundle 생성.
- 환경: Node v22.21.1 / npm 10.9.4. package.json의 Node 20.x와 다르므로 CI의 Node 20 재검증은 필요하다.
- 9개 `/admin/*` 경로 HTTP 확인: 모두 200 및 Vite HTML shell 반환.
  이 검사는 SPA fallback 확인이며 실제 브라우저 render/인증/API 성공 증거는 아니다.
- 브라우저 도구는 Chrome 부재로 실패했다. `npx playwright install chromium` 대안 이후에도
  libnss3 등 host library 부족으로 Chromium launch가 실패했다. 시스템 패키지는 변경하지 않았다.
  따라서 시각 검증, 실제 기관 인증·DB 기반 E2E는 미수행이다.
- `git diff --check` 통과. frontend가 기존부터 untracked였으므로 해당 명령만으로 신규 파일은
  검증되지 않는다. 별도 변경 소스의 trailing whitespace/120자 줄 길이 검사도 수행했다.
  SVG 단일 path 문자열만 120자 예외에 해당한다.

## 변경 파일

- `frontend/src/App.tsx`
- `frontend/src/main.tsx`
- `frontend/src/components/Icon.tsx` (신규)
- `frontend/src/components/primitives.tsx`
- `frontend/src/components/ResourcePage.tsx`
- `frontend/src/pages/DetailCodePage.tsx`
- `frontend/src/styles/tokens.css`
- `frontend/src/styles/admin.css` (신규 시각 레이어)
- `frontend/tests/unit/app.test.tsx`
- `frontend/tests/unit/states.test.tsx`
- `frontend/tests/unit/ux.test.tsx` (신규)
- `frontend/tests/unit/pages-audit.test.tsx` (신규)
- `frontend/UX-IMPROVEMENT.md` (신규)
- `frontend/PAGE-AUDIT.md` (신규)
- `frontend/package-lock.json` (npm 설치로 생성한 실제 lockfile)
