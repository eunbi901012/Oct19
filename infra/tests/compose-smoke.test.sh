#!/usr/bin/env bash
# Explicit runner opt-in: creates containers and retains the database volume.
set -euo pipefail
set +x
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"
: "${RUN_COMPOSE_SMOKE:?Set RUN_COMPOSE_SMOKE=1 after reviewing the handoff}"
[[ "$RUN_COMPOSE_SMOKE" == 1 ]] || exit 2
: "${POSTGRES_PASSWORD:?Inject database password without printing it}"
bash infra/tests/stack-contract.test.sh
compose=(docker compose -f infra/docker-compose.yml)
# Never print rendered config: it contains interpolated credentials.
"${compose[@]}" config --quiet
"${compose[@]}" up -d --build --wait --wait-timeout 180
python3 <<'PY'
import json
import urllib.error
import urllib.request

def health(url):
    with urllib.request.urlopen(url, timeout=10) as response:
        body = json.load(response)
        assert response.status == 200
        assert body['success'] is True and isinstance(body['meta'], dict)
        assert body['data']['status'] == 'UP'

health('http://localhost:8080/api/health')
health('http://localhost:5173/api/health')
with urllib.request.urlopen('http://localhost:5173/', timeout=10) as response:
    assert response.status == 200 and 'text/html' in response.headers['Content-Type']
try:
    urllib.request.urlopen('http://localhost:5173/admin/users', timeout=10)
except urllib.error.HTTPError as error:
    assert error.code == 401, 'Direct protected screen URL must be authenticated on the server'
else:
    raise SystemExit('FAIL: unauthenticated direct screen URL was accepted')
try:
    urllib.request.urlopen('http://localhost:5173/api/users', timeout=10)
except urllib.error.HTTPError as error:
    assert error.code == 401, 'Unauthenticated API must return 401'
else:
    raise SystemExit('FAIL: unauthenticated protected API was accepted')
print('PASS: health, public shell, API proxy and direct screen URL unauthenticated rejection')
PY
# Query application tables, not just pg_isready: confirms schema initialization.
"${compose[@]}" exec -T database sh -ec \
  'psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Atc '\''SELECT count(*) FROM users;'\''' \
  >/dev/null
printf '%s\n' 'PASS: PostgreSQL users table query; no fixture or authenticated-write claim'
printf '%s\n' 'PENDING: approved authentication, fixtures, protected save/requery and browser acceptance'
printf '%s\n' 'Containers retained. Stop with: docker compose -f infra/docker-compose.yml down'
