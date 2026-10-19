-- Authoritative schema; Flyway V001 is a byte-identical initial migration.
-- Unapproved lifecycle, root and temporal policies deliberately have no defaults or checks.

CREATE TABLE IF NOT EXISTS users (
    user_id text PRIMARY KEY,
    employee_number text,
    person_name text,
    organization_code text,
    job_grade text,
    employment_status text,
    enabled boolean,
    position text,
    retired_on date,
    last_synced_at timestamptz
);
COMMENT ON TABLE users IS 'KORUS 사용자 원천과 로컬 사용여부를 보존한다.';

CREATE TABLE IF NOT EXISTS organizations (
    organization_code text PRIMARY KEY,
    organization_type text
);
COMMENT ON TABLE organizations IS '조직 원천 식별정보와 구분을 보존한다.';

CREATE TABLE IF NOT EXISTS roles (
    role_id text PRIMARY KEY,
    purpose text,
    grant_criteria text,
    default_data_scope text
);
COMMENT ON TABLE roles IS '역할의 목적과 부여 기준 및 기본 범위를 정의한다.';

CREATE TABLE IF NOT EXISTS user_role_assignments (
    assignment_id text PRIMARY KEY,
    user_id text REFERENCES users(user_id),
    role_id text REFERENCES roles(role_id),
    valid_from date,
    valid_to date,
    approver_id text REFERENCES users(user_id),
    origin text,
    action text
);
COMMENT ON TABLE user_role_assignments IS '사용자별 역할 부여와 기간 및 승인자를 기록한다.';

CREATE TABLE IF NOT EXISTS menus (
    menu_id text PRIMARY KEY,
    parent_menu_id text REFERENCES menus(menu_id),
    level integer,
    display_order integer
);
COMMENT ON TABLE menus IS '메뉴 계층과 부모별 표시순서를 보존한다.';

CREATE TABLE IF NOT EXISTS menu_permissions (
    permission_id text PRIMARY KEY,
    role_id text REFERENCES roles(role_id),
    organization_code text REFERENCES organizations(organization_code),
    user_id text REFERENCES users(user_id),
    menu_id text REFERENCES menus(menu_id),
    allowed boolean
);
COMMENT ON TABLE menu_permissions IS '메뉴의 역할·조직·사용자별 접근 설정을 보존한다.';

CREATE TABLE IF NOT EXISTS menu_execution_info (
    menu_id text PRIMARY KEY REFERENCES menus(menu_id),
    screen_id text,
    url text
);
COMMENT ON TABLE menu_execution_info IS '메뉴에 연결된 화면과 실행 URL을 보존한다.';

CREATE TABLE IF NOT EXISTS code_groups (
    group_id text PRIMARY KEY,
    name text,
    description text,
    managing_department text
);
COMMENT ON TABLE code_groups IS '공통코드 그룹 정의를 관리한다.';

CREATE TABLE IF NOT EXISTS detail_codes (
    group_id text REFERENCES code_groups(group_id),
    code text,
    sort_order integer,
    parent_code text,
    PRIMARY KEY (group_id, code),
    FOREIGN KEY (group_id, parent_code) REFERENCES detail_codes(group_id, code)
);
COMMENT ON TABLE detail_codes IS '그룹별 상세코드와 그룹 내부 계층을 관리한다.';

CREATE TABLE IF NOT EXISTS organization_relations (
    relation_id text PRIMARY KEY,
    organization_code text REFERENCES organizations(organization_code),
    parent_code text REFERENCES organizations(organization_code),
    valid_from date,
    valid_to date
);
COMMENT ON TABLE organization_relations IS '조직 상위관계와 적용기간을 삭제 없이 보존한다.';

COMMENT ON COLUMN users.employment_status IS 'KORUS 원천값; 허용 코드 승인 대기(OQ-DATA-001).';
COMMENT ON COLUMN user_role_assignments.origin IS '보직 기반:보직 기반|수동:수동';
COMMENT ON COLUMN user_role_assignments.action IS '부여:부여|변경:변경|회수:회수';

CREATE INDEX IF NOT EXISTS idx_users_employee
    ON users (employee_number);
CREATE INDEX IF NOT EXISTS idx_assignments_user
    ON user_role_assignments (user_id, valid_from, valid_to);
CREATE INDEX IF NOT EXISTS idx_permissions_menu
    ON menu_permissions (menu_id);
CREATE INDEX IF NOT EXISTS idx_menus_parent_order
    ON menus (parent_menu_id, display_order);
CREATE INDEX IF NOT EXISTS idx_codes_group_order
    ON detail_codes (group_id, sort_order);
CREATE INDEX IF NOT EXISTS idx_relations_period
    ON organization_relations (organization_code, valid_from, valid_to);
