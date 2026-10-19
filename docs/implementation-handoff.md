# Infrastructure implementation handoff

## Scope and generation receipt

This pass authored infrastructure and durable documentation only. It did not modify backend Java, POM,
application configuration, SQL, frontend packages, application sources or application tests. Other agents
own those files. The infrastructure tests were written before Compose/Dockerfile implementation, but
neither RED nor GREEN was observed: the generation phase prohibits executing tests and runtime commands.
No task checkbox is marked complete. No commit or push was requested or performed.

The nine staged design documents were copied from `codegen-context/` to `.specify/specs/001-feature/`.
The staged originals and `.aiops-spec/` are read-only inputs, not generated deliverables. Their root
ignore rules protect against accidental staging. The durable plan retains the exact execution/persistence
marker and JSON. The original quickstart is historical design guidance, not evidence of this implementation.
Its broad suite commands and synthetic fixture expectations are NOT instructions for this generation pass.
Use the focused runner sequence below instead. The appended durable quickstart receipt clarifies this.

No Docker, build, test, npm, Maven, network or development-server command was executed in this pass.
Static file inspection and shell syntax parsing are the only validation permitted. Runtime readiness,
image existence, package resolution, migration behavior and application integration remain unverified.

## Runtime wiring

- `infra/docker-compose.yml`: `database`, `backend`, `frontend`; application images also have build and
  `pull_policy: build`; contexts are `../backend` and `../frontend`.
- PostgreSQL 16.4 uses a named data volume and publishes no host port. `pg_isready` gates backend startup.
- Inject nonempty `POSTGRES_PASSWORD` in the process environment via an approved secret mechanism.
  Do not write it into source, a committed env file, command history, logs or rendered Compose output.
  `POSTGRES_DB` and `POSTGRES_USER` default to the non-secret local proposal `oct19`.
- Backend `DatabaseConfiguration` reads `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`.
  Compose uses these exact names. Flyway applies the initial migration; SQL init is disabled.
- Backend: Maven 3.9.9/JDK17 build stage, executable Boot jar selected via `target/*.jar` to `app.jar`,
  JRE17 runtime as non-root. Maven packaging skips test execution; runner tests are separate gates.
  The glob excludes Boot's `.jar.original` by suffix, and fails if multiple `.jar` artifacts exist.
- Backend POM owner must configure Boot 3.3.x executable repackaging. No Swagger dependency is required.
- Frontend: Node20 named `build` stage, `npm ci`, complete context including tests/config, then nginx
  serves only generated dist and nginx configuration. No node_modules or source enters the runtime image.
- Frontend publishes `5173:80`, backend `8080:8080`. These bindings are not restricted to loopback;
  use only in the approved local environment, not as a production exposure/security configuration.
- nginx forwards `/api` and `/api/...` unchanged to backend:8080; API failures never fall back to SPA HTML.
- Direct `/admin/...` requests use nginx `auth_request` against the backend's existing screen route.
  `ScreenAccessService` requires the matching management-read operation AND the persisted menu's effective
  permission via the same `AuthenticationAdapter` and `AccessPolicy` as APIs/navigation. Multiple menu mappings
  remain approval-blocked, not an invented permission union. The public root shell remains available.
- Probes use installed commands: PostgreSQL `pg_isready`, backend installed `curl`, nginx Alpine `wget`.
  Backend public `/api/health` is required. Compose waits for backend health before starting frontend.
- Image patch choices are implementation pins, not resolution of OQ-TECH-001 or security currency claims.
  Image availability and patched replacements must be reviewed by the authorized runner.

## Prerequisites and blocking policy decisions

1. **Real npm lockfile**: `frontend/package-lock.json` was absent on initial inspection. This pass did
   not run npm or fabricate a lock. The frontend owner/authorized runner must materialize the actual
   dependency graph and commit the real lockfile before `npm ci`, Docker frontend build, or stack checks.
   On the approved network-enabled runner only: `(cd frontend && npm install --package-lock-only)`;
   review the resulting exact package graph before `npm ci`. Do not replace the Dockerfile with npm install.
2. **OQ-AUTH-001 / OQ-AUTH-002**: approved authentication, principal/role mapping, management scope,
   permission combinations and candidate access remain unresolved. No synthetic administrator fixture,
   token minting endpoint, shared token, bypass header or authentication backdoor is authorized.
3. **OQ-ORG-001**: ownership of organization data, inclusive/exclusive period boundaries, timezone,
   overlap/cycles, future/past changes and current-state classification need approval.
4. **OQ-ROLE-001**: approver identity/validation, approval stages, job-based mapping and revocation
   timing remain blocked. Never automatically substitute the current actor as approver.
5. **OQ-DATA-001**: KORUS source connection, initial loading and synchronization boundaries need
   approval. Source personnel fields remain read-only. No synthetic administrator or production data seed.
6. Detail-code fields, routes/design assets, deletion/retention and technical patch policies remain
   OQ-UI-001/002/003/004, OQ-DATA-002 and OQ-TECH-001. Infrastructure does not settle them.

## Runner-only focused verification (not executed here)

Start at the repository root. Review generated application code and provide approved environment first.
Do not execute the broad historical `verify` or complete frontend/e2e suites as part of this focused pass.

```sh
# Prerequisites: Python3 with PyYAML, Java17, Docker Compose supporting pull_policy build and --wait.
# After the real package-lock.json has been materialized and reviewed:
bash infra/tests/stack-contract.test.sh
# Missing-input negative check; expect nonzero, not PASS. No repository modification required.
STACK_ROOT=/nonexistent/oct19-contract-input bash infra/tests/stack-contract.test.sh
# Inject POSTGRES_PASSWORD securely first; never print the fully rendered config.
docker compose -f infra/docker-compose.yml config --quiet
RUN_COMPOSE_SMOKE=1 bash infra/tests/compose-smoke.test.sh
# Focused foundation selectors; inspect actual names before use if application owners rename them.
(cd backend && ./mvnw -Dtest=ScreenAccessContractTest test)
# Requires an isolated PostgreSQL URL, user and password in TEST_DATABASE_* environment variables:
(cd backend && ./mvnw -Dtest=PersistenceContractTest,MyBatisPostgresContractTest test)
(cd frontend && npm ci && npm run test -- --run tests/unit/app.test.tsx)
# Stop without deleting the named database volume:
docker compose -f infra/docker-compose.yml down
```

`stack-contract.test.sh` validates BOM, required files, lock materialization, ports, contexts, exclusions,
health wiring and jar/nginx contracts. A missing required file exits nonzero. Python/PyYAML are runner
prerequisites, not vendored dependencies. `compose-smoke.test.sh` requires explicit opt-in, runs the static
contract, validates Compose silently, builds/starts services with bounded readiness, checks direct and
proxied health envelopes, public shell HTML, protected API/direct-screen unauthenticated 401,
and a real users-table SQL query.
It retains containers/data for inspection and intentionally does NOT claim authenticated write/readback,
synthetic fixture loading, nine-story browser acceptance or policy completion. These need approved
identity/source/fixture paths and separately scoped runner evidence. A running health endpoint alone
cannot prove migration correctness, business acceptance or authorization policy.

The custom `backend/mvnw` is a jar-free POSIX launcher, not a fabricated official wrapper binary. On first
explicit invocation it downloads pinned Maven 3.9.9 and its SHA512 from the official Maven Central HTTPS
URL, verifies the checksum, extracts into the user's Maven cache and forwards arguments. It requires
curl, tar, sha512sum, GNU mv and Java17; offline invocation needs an already populated cache. Remote
checksum verification detects download corruption but is not an independently pinned trust anchor.
Docker packaging uses the Maven build image directly and does not depend on this bootstrap.

## Observed static inspection receipt

- `bash -n` parsed both infrastructure scripts successfully; neither script was executed.
- `sh -n backend/mvnw` parsed the launcher successfully; no bootstrap/download occurred.
- Python AST parsing of both embedded Python blocks succeeded without executing their checks.
- Eight durable documents are byte-identical copies of their staged inputs, including `plan.md`.
  The durable quickstart differs only by the appended generation receipt and its linking text.
- Initial and final static inventory found no `frontend/package-lock.json`; materialization is pending.
- Authored Dockerfiles, launcher, nginx configuration, handoff and infrastructure scripts have no
  lines over 120 characters. Staged document formatting was preserved, not reformatted.
- A local copy-command quoting typo initially produced a Python SyntaxError before doing any work;
  the corrected copy completed. It was not a failed build/test or runtime attempt.

## Evidence bookkeeping

Record sanitized command, selected tests, exit code, failures/retries, pending policies and actual
observations in the durable quickstart after authorized runner execution. Never paste tokens, database
passwords, rendered Compose environment or authenticated request headers. Only then consider task
checkbox updates. This pass contributes artifacts and a static receipt, not a runtime acceptance result.
