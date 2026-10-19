# 공통 관리 애플리케이션

Java 17 / Spring Boot 3.3 / MyBatis / PostgreSQL 16 백엔드와
React 18 / TypeScript / Vite 5 프런트엔드로 구성한다.

- `backend/`: 관리 API, 차단형 MyBatis 영속성, 정책 adapter와 JUnit/MockMvc 계약 테스트
- `frontend/`: 한국어 관리 화면 9개, 서버 제공 메뉴 탐색, 상대 `/api` 클라이언트
- `infra/docker-compose.yml`: DB 내부 통신, backend/frontend 명시 포트, healthcheck
- `backend/DATABASE-CONTRACT.md`: SQL 정본, 미승인 정책, 전용 PostgreSQL 검증 전제
- `docs/implementation-handoff.md`: runner 실행 절차와 승인·검증 대기 항목

인증 방식과 실제 역할 코드, 메뉴 권한 결합, 기간 경계 및 승인자 검증은 승인 대기다.
실제 기관 adapter를 연결하기 전에는 보호 작업을 허용하지 않으며 가상 관리자·토큰을 제공하지 않는다.
KORUS 원천 인사정보는 편집하지 않고 로컬 사용자 설정은 `enabled`만 변경한다.
사용자 역할 부여는 별도의 API와 화면에서 처리한다.

생성 세션에서는 패키지 매니저·빌드·시험·컨테이너를 실행하지 않았다.
`frontend/package-lock.json`은 runner가 npm으로 실제 생성해야 하며 임의로 작성하지 않는다.
실행 성공 또는 업무 합격 판정은 runner의 빌드·DB·HTTP·브라우저 검증 후에만 가능하다.
