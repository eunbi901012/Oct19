package org.example.oct19.model;

import java.util.Arrays;
import java.util.List;

/** Closed physical-table registry: request strings can never become SQL identifiers. */
public enum Resource {
    USERS("users", "user_id", "user_id employee_number person_name organization_code job_grade "
            + "employment_status enabled position retired_on last_synced_at"),
    ORGANIZATIONS("organizations", "organization_code", "organization_code organization_type"),
    ROLES("roles", "role_id", "role_id purpose grant_criteria default_data_scope"),
    ASSIGNMENTS("user_role_assignments", "assignment_id", "assignment_id user_id role_id valid_from valid_to "
            + "approver_id origin action"),
    MENUS("menus", "menu_id", "menu_id parent_menu_id level display_order"),
    PERMISSIONS("menu_permissions", "permission_id", "permission_id role_id organization_code user_id menu_id allowed"),
    EXECUTION("menu_execution_info", "menu_id", "menu_id screen_id url"),
    GROUPS("code_groups", "group_id", "group_id name description managing_department"),
    DETAILS("detail_codes", "group_id code", "group_id code sort_order parent_code"),
    RELATIONS("organization_relations", "relation_id", "relation_id organization_code parent_code valid_from valid_to");

    private final String table;
    private final List<String> keys;
    private final List<String> columns;

    Resource(String table, String keys, String columns) {
        this.table = table;
        this.keys = List.copyOf(Arrays.asList(keys.split(" ")));
        this.columns = List.copyOf(Arrays.asList(columns.split(" ")));
    }

    /** Returns the authoritative physical table name. */
    public String table() {
        return table;
    }

    /** Returns the physical primary key, including composite detail-code keys. */
    public List<String> keys() {
        return keys;
    }

    /** Returns only columns present in the ten-table schema. */
    public List<String> columns() {
        return columns;
    }
}
