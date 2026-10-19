#!/usr/bin/env bash
# Runner-owned static contract check; Python 3 + PyYAML are prerequisites.
set -euo pipefail
ROOT="${STACK_ROOT:-$(cd "$(dirname "$0")/../.." && pwd)}"
python3 - "$ROOT" <<'PY'
import json
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path
import yaml

root = Path(sys.argv[1])
def require(condition, message):
    if not condition:
        raise SystemExit('FAIL: ' + message)

def read(name):
    path = root / name
    require(path.is_file(), 'missing ' + name)
    return path.read_text()

compose_text = read('infra/docker-compose.yml')
services = yaml.safe_load(compose_text)['services']
require(set(services) == {'backend', 'frontend', 'database'}, 'service inventory')
for name, port in [('backend', '8080:8080'), ('frontend', '5173:80')]:
    service = services[name]
    require(service.get('image') and service.get('pull_policy') == 'build', name + ' image/build policy')
    require(service['build']['context'] == '../' + name, name + ' static build context')
    require(service['ports'] == [port], name + ' explicit published port')
    require(service.get('healthcheck', {}).get('test'), name + ' healthcheck')
require(re.match(r'^postgres:16(?:\.|-)', services['database']['image']), 'PostgreSQL 16')
require(not services['database'].get('ports'), 'database must not publish ports')
require('${POSTGRES_PASSWORD:?' in compose_text, 'mandatory external database password')
require('pg_isready' in str(services['database']['healthcheck']), 'database readiness')
require('/api/health' in str(services['backend']['healthcheck']), 'backend health endpoint')
for name in ('backend', 'frontend'):
    patterns = set(read(name + '/.dockerignore').splitlines())
    for excluded in ('node_modules', '.git', 'dist', 'build', '.next', 'coverage', 'target'):
        require(excluded in patterns, name + ' context exclusion ' + excluded)
    require('tests' not in patterns and 'src' not in patterns, name + ' retains build inputs')

pom = ET.fromstring(read('backend/pom.xml'))
ns = {'m': 'http://maven.apache.org/POM/4.0.0'}
require(pom.findtext('m:properties/m:java.version', namespaces=ns) == '17', 'Java 17')
require(re.fullmatch(r'3\.3\.\d+', pom.findtext('m:parent/m:version', namespaces=ns) or ''), 'Boot 3.3')
require('spring-boot-maven-plugin' in read('backend/pom.xml'), 'executable Boot jar plugin')
package = json.loads(read('frontend/package.json'))
for name, major in [('react', '18'), ('react-dom', '18'), ('vite', '5')]:
    version = {**package.get('dependencies', {}), **package.get('devDependencies', {})}.get(name, '')
    require(re.fullmatch(major + r'\.\d+\.\d+', version), name + ' exact version')
require(str(package.get('engines', {}).get('node', '')).startswith('20'), 'Node 20 engine')
lock = json.loads(read('frontend/package-lock.json'))
require(lock.get('lockfileVersion', 0) >= 2 and lock.get('packages'), 'materialized npm lockfile')
for name in ('build', 'test', 'test:e2e'):
    require(name in package.get('scripts', {}), 'frontend script ' + name)
backend = read('backend/Dockerfile')
frontend = read('frontend/Dockerfile')
require('maven:3.9.9-eclipse-temurin-17 AS build' in backend, 'JDK Maven build stage')
require('eclipse-temurin:17-jre' in backend and 'target/*.jar' in backend, 'JRE and jar artifact')
require('app.jar' in backend and 'java", "-jar"' in backend, 'executable jar runtime')
require('node:20.' in frontend and ' AS build' in frontend, 'Node20 named build stage')
require('npm ci' in frontend and 'COPY . .' in frontend, 'locked install and complete build context')
require('COPY --from=build /app/dist' in frontend, 'nginx dist-only application runtime')
require('proxy_pass http://backend:8080;' in read('frontend/nginx.conf'), 'preserve API URI')
print('PASS: static stack contract (not runtime evidence)')
PY
