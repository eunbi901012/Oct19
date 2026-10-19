# Backend implementation / runner handoff

## Scope and evidence

Java 17, Spring Boot **3.3.13**, MyBatis starter **3.0.4**, Flyway **10.20.1** with the PostgreSQL
support module, and the Boot-managed PostgreSQL JDBC driver. The Boot Maven plugin repackages an executable jar.
The twenty business operations belong to `api/ManagementController.java`; public database-backed health belongs to
`api/HealthController.java`. The route/verb/operation-ID set is the exact 21-operation durable OpenAPI set.

**Awaiting runner verification.** Source and tests were written; no Java compilation, Maven execution, tests,
server, database, Docker, package manager or network call was executed by the backend worker. Tests were written
before the corresponding production implementation, but RED/GREEN has not been observed under this execution ban.
Static XML/YAML parsing, route inventory, literal JSON inspection and whitespace checks are not runtime evidence.

`schema.sql`, `db/migration/V001__foundation.sql`, `PersistenceContractTest`, the durable test contract,
Dockerfile, `.dockerignore` and Maven wrapper are owned by other workers and were not changed here.

## Configuration and readiness

Supply `DATABASE_URL` (a PostgreSQL JDBC URL), `DATABASE_USER`, and `DATABASE_PASSWORD` externally.
No credentials, accounts, roles, root records or business rows are seeded. `SERVER_PORT` optionally selects the HTTP
port. `FLYWAY_ENABLED` defaults to true; SQL initialization is always `never`. Configured databases are migrated
with Flyway. Without `DATABASE_URL`, the process is designed to start with a deliberately unavailable DataSource;
no migration is attempted and health must not return UP. Configured-but-unreachable databases fail startup migration.

`GET /api/health` calls the real MyBatis `SELECT 1`. Success is the contract's 200 success/meta/data.status=UP.
Database failure uses the contract-declared 400 error envelope rather than inventing a 503 response contract.
There is no extra login, token minting, credential reset, delete, bulk or audit endpoint.

## Approval integration boundaries

Provide institution-approved Spring beans implementing:

* `security/AuthenticationAdapter`: actually verify the approved credential; return a verified `Identity`.
  Merely receiving a bearer string never authenticates anyone. The fallback returns empty, hence 401 with a useful
  OQ-AUTH-001 explanation, before any business database access or write transaction.
* `security/AccessPolicy`: require BOTH approved operation role/scope and effective menu authorization in
  `requireOperation`; implement the same effective combination/temporal rules in `mayNavigate`.
  `getAccessibleMenus` has its own read scope, not an implied management-edit permission. Every call receives
  persisted policy rows; no administrator bypass, role code, union/deny precedence or permission seed is invented.
  The fallback raises 403 POLICY_APPROVAL_REQUIRED.
* `security/TemporalPolicy`: compute current organization relations and assignment applicability, excluding ended
  roles. Approve timezone, inclusive boundaries, future starts and revoke effects first. The fallback raises 403,
  not an arbitrary current=true/false decision. Assignment `is_current` and users' `business_roles` share this policy.
* `security/BusinessPolicy`: approve organization ownership/period/overlap/cycle behavior, approver verification,
  permission-target combinations and genuinely unresolved root representations. Never automatically substitute the
  operator for `approver_id` or require approver/operator inequality without approval.

Fallback business gates affect relation writes, assignment writes and permission-target writes. Independent role,
code-group, execution-information and user-enabled saves are fully implemented behind authentication/access guards.
Known-parent menu edits and detail edits with a known group parent are implemented; unapproved root creation is
explicitly gated. Omitted detail `parent_code` preserves an existing parent. No external KORUS integration is fabricated.
These are replaceable approval boundaries, not unconditional gate-only substitutes for persistence: approved policy
beans reach real transactional MyBatis writes and readback for all nine save operations.

## Data and contract invariants

* Only `user_id` and `enabled` are writable in user settings. Each source/projection/unknown property is rejected
  with its exact `field_errors[].field`; source columns are never included in the SQL UPDATE. Missing users cannot
  be inserted through the settings operation.
* Required-property presence, explicit nullable dates, strict JSON booleans/integers/strings, actions and permission
  target selection follow the OpenAPI inputs. No enum, string-length or period-order constraint is invented.
* Assignment origins are preserved, including on SQL conflict; only a genuinely new manual assignment gets `수동`.
  All three actions persist the supplied user, role, period and approver without changing unrelated assignments.
* Organization saves append a generated internal relation ID and never delete earlier relation/period rows.
  Organization read projections require one approved current relation; ambiguous/root representation stays pending.
* Each save touches its owning table only. Detail composite keys and same-group parent checks preserve group isolation.
  Menu edits do not resequence other parents. Role/group/execution edits leave assignments/details/permissions alone.
* SQL identifiers come only from `model/Resource`; all values are bound. Optional WHERE predicates are added only
  when supplied and non-null. There are no untyped-null OR/COALESCE predicates and no joined alias ambiguity.
* Reads return database-derived results, including legitimate empty results, and explicit page/size/total.
  Role filtering occurs before pagination. Detail reads and navigation follow persisted ordering.
* Nonempty generic `filter` has no source-approved semantics and returns a field-specific 400; use the named query
  conditions. Their exact matching is implemented; broader contains/combinations/default-sort policy remains unapproved.
* Physical schema columns can be null where required nonnullable response fields cannot. Such inconsistent source
  rows return DATA_CONTRACT_ERROR rather than invented values or silently omitted fields. Root menu/detail/organization
  representation therefore remains an approval/source-data issue. Nullable dates and nullable permission targets retain
  their response keys. JDBC timestamp strings preserve `Timestamp.toInstant()` fractional precision.

## Tests for the runner

Run from `backend/` with the infra-owned wrapper, when execution is permitted:

```sh
./mvnw -Dtest=ApiFoundationTest,BusinessHttpTest,CatalogSqlTest test
./mvnw -Dtest=PostgreSqlHttpTest,PersistenceContractTest test
./mvnw package
```

The second target requires an **isolated PostgreSQL** database in `TEST_DATABASE_URL`, `TEST_DATABASE_USER`,
`TEST_DATABASE_PASSWORD`. `PostgreSqlHttpTest` deliberately skips without its URL; skipped is NOT passed.
It runs real Flyway/MyBatis/HTTP operations, commits saves, queries one saved user through an independent JDBC
connection, exercises optional SQL predicates, verifies detail omission/group noninterference, and probes actual
post-INSERT transaction rollback. Its uniquely keyed fixtures are test-only and removed after each test.
Do not point it at a production database. Existing runner-owned persistence tests have their own ownership/evidence.

`BusinessHttpTest` exercises the real owning HTTP controller and service for all twenty business operations.
Its stateful mapper fixture (not a mocked service) checks per-save success/readback, exact required/missing/null fields
read from `ClassPathResource("contracts/openapi.yaml")`, forbidden properties, 401/403 with no writes, personnel
source protection, history, assignment actions/origin/current-role projection, separate navigation scope and persisted
permission effects on both navigation and direct server denial. Immutable production policy snapshots and full
fixture table snapshots support noninterference assertions. These policies are **test doubles, not real auth proof**.
`ApiFoundationTest` checks health roundtrips/errors and rejects arbitrary/missing credentials with the production fallback.
`CatalogSqlTest` checks SQL shape only, including no source-user insert and no optional-null predicates.

Remaining runtime evidence: main/test compilation, every test result, executable-jar startup/migrations, actual approved
institutional authentication and permission combinations, period boundaries/approver validation, and all-source legacy
null/root resolution. PostgreSQL integration coverage selects representative operations; remaining per-operation
persistence/side-effect coverage uses the stateful mapper fixture until the runner expands the real-DB matrix.
Vendor obligations involving preview/browser UI equality, navigation execution, version/image checks, Compose smoke
and build-context exclusions remain frontend/infra/runner work; backend API success does not establish them.
