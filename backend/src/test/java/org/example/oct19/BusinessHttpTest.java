package org.example.oct19;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.example.oct19.mapper.CatalogMapper;
import org.example.oct19.model.Resource;
import org.example.oct19.security.AccessPolicy;
import org.example.oct19.security.AuthenticationAdapter;
import org.example.oct19.security.BusinessPolicy;
import org.example.oct19.security.Identity;
import org.example.oct19.security.TemporalPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Real owning HTTP handlers and services with a stateful persistence-boundary fixture, not authentication proof. */
@SpringBootTest(properties = "spring.flyway.enabled=false")
@AutoConfigureMockMvc
class BusinessHttpTest {
    @Autowired MockMvc http;
    @Autowired ObjectMapper json;
    @MockBean CatalogMapper mapper;
    @MockBean AuthenticationAdapter authentication;
    @MockBean AccessPolicy access;
    @MockBean TemporalPolicy temporal;
    @MockBean BusinessPolicy business;
    @MockBean org.springframework.transaction.PlatformTransactionManager transactions;
    final Map<Resource, List<Map<String, Object>>> rows = new LinkedHashMap<>();
    int writes;

    @BeforeEach
    void fixture() {
        for (Resource resource : Resource.values()) {
            rows.put(resource, new ArrayList<>());
        }
        when(authentication.authenticate(any())).thenReturn(Optional.of(new Identity("fixture-operator")));
        when(temporal.isCurrentAssignment(any())).thenReturn(true);
        when(temporal.isCurrentRelation(any())).thenReturn(true);
        when(access.mayNavigate(any(), any(), any())).thenReturn(true);
        when(mapper.select(any(), anyMap())).thenAnswer(call -> {
            Resource resource = call.getArgument(0);
            Map<String, Object> filters = call.getArgument(1);
            return rows.get(resource).stream()
                    .filter(row -> filters.entrySet().stream().allMatch(e -> e.getValue().equals(row.get(e.getKey()))))
                    .map(LinkedHashMap::new).map(row -> (Map<String, Object>) row).toList();
        });
        when(mapper.save(any(), anyMap())).thenAnswer(call -> {
            Resource resource = call.getArgument(0);
            Map<String, Object> input = call.getArgument(1);
            List<Map<String, Object>> table = rows.get(resource);
            Map<String, Object> row = table.stream().filter(existing -> resource.keys().stream()
                    .allMatch(key -> input.get(key).equals(existing.get(key)))).findFirst().orElse(null);
            if (row == null) {
                row = new LinkedHashMap<>();
                for (String column : resource.columns()) {
                    row.put(column, null);
                }
                table.add(row);
            }
            row.putAll(input);
            writes++;
            return 1;
        });
        seed(Resource.USERS, "user_id", "U", "employee_number", "001", "person_name", "원천",
                "organization_code", "O", "job_grade", "G", "employment_status", "재직",
                "enabled", true, "position", "보직", "retired_on", null, "last_synced_at", null);
        seed(Resource.ORGANIZATIONS, "organization_code", "O", "organization_type", "부서");
        seed(Resource.ORGANIZATIONS, "organization_code", "P", "organization_type", "부서");
        seed(Resource.RELATIONS, "relation_id", "INITIAL", "organization_code", "O", "parent_code", "P",
                "valid_from", null, "valid_to", null);
        seed(Resource.ROLES, "role_id", "R", "purpose", "기존", "grant_criteria", "기준",
                "default_data_scope", "범위");
        seed(Resource.MENUS, "menu_id", "M", "parent_menu_id", "P", "level", 2, "display_order", 1);
        seed(Resource.MENUS, "menu_id", "P", "parent_menu_id", "P", "level", 1, "display_order", 0);
        seed(Resource.EXECUTION, "menu_id", "M", "screen_id", "SCR", "url", "/admin/users");
        seed(Resource.GROUPS, "group_id", "G", "name", "기존", "description", "설명",
                "managing_department", "부서");
        seed(Resource.DETAILS, "group_id", "G", "code", "P", "sort_order", 0, "parent_code", "P");
    }

    static List<String> readPaths() {
        return List.of("/api/users", "/api/organizations", "/api/roles", "/api/user-role-assignments",
                "/api/menu-permissions", "/api/menu-structure", "/api/menu-execution-info",
                "/api/code-groups", "/api/detail-codes", "/api/organization-relation-history");
    }

    @ParameterizedTest
    @MethodSource("readPaths")
    void eachReadHandlerReturnsEnvelopeAndExactPagination(String path) throws Exception {
        http.perform(get(path).param("page", "1").param("size", "10")
                        .param("group_id", "G").param("organization_code", "O"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.meta.request_id").isString())
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.size").value(10));
    }

    @ParameterizedTest
    @MethodSource("readPaths")
    void readsRejectMissingAndInvalidPagination(String path) throws Exception {
        http.perform(get(path)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field_errors[0].field").value("page"));
        http.perform(get(path).param("page", "0").param("size", "10"))
                .andExpect(status().isBadRequest());
        assertEquals(0, writes);
    }

    @Test
    void searchUsersAndUpdateUserSettingsPreserveAllSourceFields() throws Exception {
        Map<String, Object> before = new LinkedHashMap<>(rows.get(Resource.USERS).get(0));
        http.perform(put("/api/users").contentType("application/json")
                        .content("{\"user_id\":\"U\",\"enabled\":false}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.enabled").value(false));
        http.perform(get("/api/users").param("page", "1").param("size", "10")
                        .param("employee_number", "001").param("enabled", "false"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].employee_number").value("001"))
                .andExpect(jsonPath("$.data[0].enabled").value(false))
                .andExpect(jsonPath("$.data[0].business_roles").isArray());
        before.put("enabled", false);
        assertEquals(before, rows.get(Resource.USERS).get(0));
    }

    @Test
    void updateUserRejectsEverySourcePropertyWithoutAnyWrites() throws Exception {
        for (String field : List.of("employee_number", "person_name", "organization_code", "job_grade",
                "employment_status", "position", "retired_on", "last_synced_at", "business_roles")) {
            Map<Resource, List<Map<String, Object>>> before = snapshot();
            http.perform(put("/api/users").contentType("application/json")
                            .content(json.writeValueAsString(Map.of("user_id", "U", "enabled", false, field, "X"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.field_errors[0].field").value(field));
            assertEquals(before, snapshot());
            assertEquals(0, writes);
        }
    }

    @Test
    void saveRoleReadbackDoesNotChangeAssignments() throws Exception {
        saveAndRead("/api/roles", "{\"role_id\":\"R\",\"purpose\":\"수정\","
                + "\"grant_criteria\":\"기준\",\"default_data_scope\":\"범위\"}", Resource.ROLES,
                "purpose", "수정");
    }

    @Test
    void saveCodeGroupReadbackDoesNotChangeDetails() throws Exception {
        saveAndRead("/api/code-groups", "{\"group_id\":\"G\",\"name\":\"수정\","
                + "\"description\":\"설명\",\"managing_department\":\"부서\"}", Resource.GROUPS,
                "name", "수정");
    }

    @Test
    void saveExecutionReadbackDoesNotChangePermissionsOrHierarchy() throws Exception {
        saveAndRead("/api/menu-execution-info", "{\"menu_id\":\"M\",\"screen_id\":\"NEW\","
                + "\"url\":\"/new\"}", Resource.EXECUTION, "screen_id", "NEW");
    }

    @Test
    void saveMenuStructureReadbackPreservesOtherParents() throws Exception {
        saveAndRead("/api/menu-structure", "{\"menu_id\":\"M\",\"parent_menu_id\":\"P\","
                + "\"level\":2,\"display_order\":8}", Resource.MENUS, "display_order", 8);
        assertEquals(0, rows.get(Resource.MENUS).get(1).get("display_order"));
    }

    @Test
    void saveDetailCodeReadbackKeepsGroupsIsolatedAndOptionalParent() throws Exception {
        saveAndRead("/api/detail-codes", "{\"group_id\":\"G\",\"code\":\"C\","
                + "\"sort_order\":2,\"parent_code\":\"P\"}", Resource.DETAILS, "sort_order", 2);
        http.perform(put("/api/detail-codes").contentType("application/json")
                        .content("{\"group_id\":\"G\",\"code\":\"C\",\"sort_order\":4}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.parent_code").value("P"));
        http.perform(get("/api/detail-codes").param("page", "1").param("size", "10").param("group_id", "OTHER"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void saveMenuPermissionReadbackIncludesNullableTargets() throws Exception {
        saveAndRead("/api/menu-permissions", "{\"permission_id\":\"Q\",\"user_id\":\"U\","
                + "\"menu_id\":\"M\",\"allowed\":false}", Resource.PERMISSIONS, "allowed", false);
        http.perform(get("/api/menu-permissions").param("page", "1").param("size", "10").param("menu_id", "M"))
                .andExpect(jsonPath("$.data[0].role_id").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.data[0].organization_code").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void saveAssignmentsSupportsMultipleRolesAndEveryActionPreservingOrigin() throws Exception {
        for (String action : List.of("부여", "변경", "회수")) {
            http.perform(put("/api/user-role-assignments").contentType("application/json")
                            .content(assignment("A", "R", action)))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.data.action").value(action))
                    .andExpect(jsonPath("$.data.approver_id").value("U"))
                    .andExpect(jsonPath("$.data.origin").value("수동"));
        }
        seed(Resource.ROLES, "role_id", "R2", "purpose", "둘", "grant_criteria", "기준",
                "default_data_scope", "범위");
        http.perform(put("/api/user-role-assignments").contentType("application/json")
                        .content(assignment("B", "R2", "부여"))).andExpect(status().isOk());
        when(temporal.isCurrentAssignment(argThat(row -> row != null && "A".equals(row.get("assignment_id")))))
                .thenReturn(false);
        http.perform(get("/api/user-role-assignments").param("page", "1").param("size", "10").param("user_id", "U"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].is_current").value(false));
        http.perform(get("/api/users").param("page", "1").param("size", "10"))
                .andExpect(jsonPath("$.data[0].business_roles[0]").value("R2"));
    }

    @Test
    void saveOrganizationAppendsHistoryAndDoesNotChangePeopleOrAssignments() throws Exception {
        seed(Resource.ORGANIZATIONS, "organization_code", "P2", "organization_type", "부서");
        Map<Resource, List<Map<String, Object>>> before = snapshot();
        String body = "{\"organization_code\":\"O\",\"parent_code\":\"P2\","
                + "\"valid_from\":null,\"valid_to\":null}";
        http.perform(put("/api/organizations").contentType("application/json").content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.parent_code").value("P2"));
        http.perform(put("/api/organizations").contentType("application/json").content(body))
                .andExpect(status().isOk());
        http.perform(get("/api/organization-relation-history").param("page", "1").param("size", "10")
                        .param("organization_code", "O"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(3));
        // Explicit test-only current-row decision; this does not approve a real period-boundary policy.
        when(temporal.isCurrentRelation(any())).thenAnswer(call -> {
            Map<String, Object> relation = call.getArgument(0);
            List<Map<String, Object>> history = rows.get(Resource.RELATIONS);
            return history.get(history.size() - 1).get("relation_id").equals(relation.get("relation_id"));
        });
        http.perform(get("/api/organizations").param("page", "1").param("size", "10")
                        .param("organization_code", "O"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].parent_code").value("P2"));
        assertEquals(before.get(Resource.USERS), rows.get(Resource.USERS));
        assertEquals(before.get(Resource.ASSIGNMENTS), rows.get(Resource.ASSIGNMENTS));
    }

    @Test
    @DisplayName("state deny: menu_permissions.allowed changes navigation and the direct server guard")
    void persistedDenialFeedsBothNavigationAndDirectServerGuard() throws Exception {
        // Approved test-only mapping: this single fixture permission controls SCR's users operation.
        when(access.mayNavigate(any(), anyString(), any())).thenAnswer(call -> {
            String menu = call.getArgument(1);
            org.example.oct19.security.PolicyData data = call.getArgument(2);
            return menu.equals("M") && data.rows(Resource.PERMISSIONS).stream()
                    .anyMatch(row -> "M".equals(row.get("menu_id")) && Boolean.TRUE.equals(row.get("allowed")));
        });
        doAnswer(call -> {
            String operation = call.getArgument(1);
            org.example.oct19.security.PolicyData data = call.getArgument(2);
            if (operation.equals("searchUsers") && data.rows(Resource.PERMISSIONS).stream()
                    .anyMatch(row -> "M".equals(row.get("menu_id")) && Boolean.FALSE.equals(row.get("allowed")))) {
                throw new org.example.oct19.api.ApiFailure(403, "ACCESS_DENIED", "fixture denial", null);
            }
            return null;
        }).when(access).requireOperation(any(), anyString(), any());
        http.perform(put("/api/menu-permissions").contentType("application/json")
                        .content("{\"permission_id\":\"Q\",\"menu_id\":\"M\",\"user_id\":\"U\",\"allowed\":true}"))
                .andExpect(status().isOk());
        http.perform(get("/api/accessible-menus")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.menus[0].menu_id").value("M"));
        http.perform(put("/api/menu-permissions").contentType("application/json")
                        .content("{\"permission_id\":\"Q\",\"menu_id\":\"M\",\"user_id\":\"U\",\"allowed\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.permission_id").value("Q"))
                .andExpect(jsonPath("$.data.allowed").value(false));
        assertEquals(false, rows.get(Resource.PERMISSIONS).get(0).get("allowed"),
                "menu_permissions must persist the requested deny setting");
        http.perform(get("/api/menu-permissions").param("page", "1").param("size", "10")
                        .param("menu_id", "M"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].permission_id").value("Q"))
                .andExpect(jsonPath("$.data[0].allowed").value(false));
        Map<Resource, List<Map<String, Object>>> beforeDeniedRead = snapshot();
        int beforeWrites = writes;
        http.perform(get("/api/accessible-menus")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.menus").isEmpty());
        http.perform(get("/api/users").param("page", "1").param("size", "10"))
                .andExpect(status().isForbidden());
        http.perform(get("/admin/users"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        assertEquals(beforeDeniedRead, snapshot());
        assertEquals(beforeWrites, writes);
    }

    @Test
    void searchUsersHonorsEachNamedConditionBeforePagination() throws Exception {
        seed(Resource.USERS, "user_id", "OTHER", "employee_number", "002", "person_name", "다른",
                "organization_code", "OTHER", "job_grade", "OTHER", "employment_status", "퇴직",
                "enabled", false, "position", "다른", "retired_on", null, "last_synced_at", null);
        seed(Resource.ASSIGNMENTS, "assignment_id", "A", "user_id", "U", "role_id", "R",
                "valid_from", null, "valid_to", null, "approver_id", "U", "origin", "수동", "action", "부여");
        Map<String, String> conditions = Map.of("employee_number", "001", "person_name", "원천",
                "organization_code", "O", "job_grade", "G", "employment_status", "재직",
                "business_roles", "R", "enabled", "true");
        for (var condition : conditions.entrySet()) {
            http.perform(get("/api/users").param("page", "1").param("size", "1")
                            .param(condition.getKey(), condition.getValue()))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.pagination.total").value(1))
                    .andExpect(jsonPath("$.data[0].user_id").value("U"));
        }
    }

    @Test
    void accessibleMenusUsesSamePolicyAndExecutionData() throws Exception {
        when(access.mayNavigate(any(), eq("P"), any())).thenReturn(false);
        http.perform(get("/api/accessible-menus")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.menus.length()").value(1))
                .andExpect(jsonPath("$.data.menus[0].menu_id").value("M"))
                .andExpect(jsonPath("$.data.menus[0].screen_id").value("SCR"));
    }

    static List<String> writePaths() {
        return readPaths().subList(0, 9);
    }

    @ParameterizedTest
    @MethodSource("writePaths")
    void everySaveRejectsMissingAndNullAndForbiddenFieldsWithoutWrites(String path) throws Exception {
        Map<Resource, List<Map<String, Object>>> before = snapshot();
        http.perform(put(path).contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.field_errors[0].field").isString());
        http.perform(put(path).contentType("application/json").content("null"))
                .andExpect(status().isBadRequest());
        http.perform(put(path).contentType("application/json").content("{\"unowned\":true}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.field_errors[0].field").value("unowned"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @ParameterizedTest
    @MethodSource("writePaths")
    void everySaveRejectsUnauthenticatedAndDeniedRequestsWithoutWrites(String path) throws Exception {
        when(authentication.authenticate(any())).thenReturn(Optional.empty());
        http.perform(put(path).contentType("application/json").content("{}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.success").value(false));
        when(authentication.authenticate(any())).thenReturn(Optional.of(new Identity("fixture-operator")));
        doThrow(new org.example.oct19.api.ApiFailure(403, "ACCESS_DENIED", "denied", null))
                .when(access).requireOperation(any(), anyString(), any());
        http.perform(put(path).contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
        assertEquals(0, writes);
    }

    @Test
    void exactTypeAndRequiredErrorsHaveNoWrites() throws Exception {
        http.perform(put("/api/menu-permissions").contentType("application/json")
                        .content("{\"permission_id\":\"Q\",\"menu_id\":\"M\",\"user_id\":\"U\",\"allowed\":\"true\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.field_errors[0].field").value("allowed"));
        http.perform(put("/api/menu-structure").contentType("application/json")
                        .content("{\"menu_id\":\"M\",\"parent_menu_id\":\"P\",\"level\":2,\"display_order\":1.5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field_errors[0].field").value("display_order"));
        http.perform(put("/api/detail-codes").contentType("application/json")
                        .content("{\"group_id\":\"G\",\"code\":\"C\",\"sort_order\":\"2\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.field_errors[0].field").value("sort_order"));
        http.perform(put("/api/menu-execution-info").contentType("application/json")
                        .content("{\"menu_id\":\"M\",\"url\":\"/x\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.field_errors[0].field").value("screen_id"));
        assertEquals(0, writes);
    }

    @Test
    void durableContractIsLoadedFromClasspath() throws Exception {
        try (var input = new ClassPathResource("contracts/openapi.yaml").getInputStream()) {
            assertTrue(new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8)
                    .contains("operationId: saveUserRoleAssignment"));
        }
    }

    @ParameterizedTest
    @MethodSource("writePaths")
    @SuppressWarnings("unchecked")
    void everyRequiredPropertyRejectsOmissionAndNonnullableNull(String path) throws Exception {
        Map<String, Object> contract;
        try (var input = new ClassPathResource("contracts/openapi.yaml").getInputStream()) {
            contract = new org.yaml.snakeyaml.Yaml().load(input);
        }
        Map<String, Object> paths = (Map<String, Object>) contract.get("paths");
        Map<String, Object> operation = (Map<String, Object>) ((Map<String, Object>) paths.get(path)).get("put");
        Map<String, Object> request = (Map<String, Object>) operation.get("requestBody");
        Map<String, Object> media = (Map<String, Object>) ((Map<String, Object>) request.get("content"))
                .get("application/json");
        String reference = (String) ((Map<String, Object>) media.get("schema")).get("$ref");
        Map<String, Object> schemas = (Map<String, Object>) ((Map<String, Object>) contract.get("components"))
                .get("schemas");
        String schemaName = reference.substring(reference.lastIndexOf('/') + 1);
        Map<String, Object> schema = (Map<String, Object>) schemas.get(schemaName);
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        for (String field : (List<String>) schema.get("required")) {
            var body = (com.fasterxml.jackson.databind.node.ObjectNode) json.readTree(validBody(path));
            body.remove(field);
            http.perform(put(path).contentType("application/json").content(json.writeValueAsString(body)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.field_errors[0].field").value(field));
            assertEquals(0, writes);
            Map<String, Object> property = (Map<String, Object>) properties.get(field);
            if (!Boolean.TRUE.equals(property.get("nullable"))) {
                body = (com.fasterxml.jackson.databind.node.ObjectNode) json.readTree(validBody(path));
                body.putNull(field);
                http.perform(put(path).contentType("application/json").content(json.writeValueAsString(body)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.field_errors[0].field").value(field));
                assertEquals(0, writes);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("readPaths")
    void everyReadChecksAuthenticationAndOperationScope(String path) throws Exception {
        Map<Resource, List<Map<String, Object>>> before = snapshot();
        when(authentication.authenticate(any())).thenReturn(Optional.empty());
        http.perform(get(path).param("page", "1").param("size", "10"))
                .andExpect(status().isUnauthorized());
        when(authentication.authenticate(any())).thenReturn(Optional.of(new Identity("fixture-operator")));
        doThrow(new org.example.oct19.api.ApiFailure(403, "ACCESS_DENIED", "denied", null))
                .when(access).requireOperation(any(), anyString(), any());
        http.perform(get(path).param("page", "1").param("size", "10"))
                .andExpect(status().isForbidden());
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    void navigationRequiresItsOwnApprovedReadScope() throws Exception {
        doThrow(new org.example.oct19.api.ApiFailure(403, "ACCESS_DENIED", "denied", null))
                .when(access).requireOperation(any(), eq("getAccessibleMenus"), any());
        http.perform(get("/api/accessible-menus")).andExpect(status().isForbidden());
        when(authentication.authenticate(any())).thenReturn(Optional.empty());
        http.perform(get("/api/accessible-menus")).andExpect(status().isUnauthorized());
        assertEquals(0, writes);
    }

    @Test
    void temporalApprovalFailureHappensBeforeAssignmentWrite() throws Exception {
        when(temporal.isCurrentAssignment(any())).thenThrow(
                org.example.oct19.api.ApiFailure.pending("fixture temporal approval missing"));
        http.perform(put("/api/user-role-assignments").contentType("application/json")
                        .content(assignment("A", "R", "부여")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POLICY_APPROVAL_REQUIRED"));
        assertEquals(0, writes);
        assertTrue(rows.get(Resource.ASSIGNMENTS).isEmpty());
    }

    @Test
    void positionBasedOriginIsNeverOverwrittenByManualChange() throws Exception {
        seed(Resource.ASSIGNMENTS, "assignment_id", "SOURCE", "user_id", "U", "role_id", "R",
                "valid_from", null, "valid_to", null, "approver_id", "U", "origin", "보직 기반", "action", "부여");
        http.perform(put("/api/user-role-assignments").contentType("application/json")
                        .content(assignment("SOURCE", "R", "변경")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.origin").value("보직 기반"));
        assertEquals("보직 기반", rows.get(Resource.ASSIGNMENTS).get(0).get("origin"));
    }

    @Test
    void literalUsersReadContractAndValidation() throws Exception {
        var before = snapshot();
        http.perform(get("/api/users").param("page", "1").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.meta.request_id").isString())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.size").value(10))
                .andExpect(jsonPath("$.data[0].user_id").value("U"));
        http.perform(get("/api/users").param("page", "0").param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.field_errors[0].field").value("page"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    void literalOrganizationsReadContractAndValidation() throws Exception {
        var before = snapshot();
        http.perform(get("/api/organizations").param("page", "1").param("size", "10").param("organization_code", "O"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.meta.request_id").isString())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.size").value(10))
                .andExpect(jsonPath("$.data[0].organization_code").value("O"));
        http.perform(get("/api/organizations").param("page", "0").param("size", "10").param("organization_code", "O"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.field_errors[0].field").value("page"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    void literalRolesReadContractAndValidation() throws Exception {
        var before = snapshot();
        http.perform(get("/api/roles").param("page", "1").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.meta.request_id").isString())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.size").value(10))
                .andExpect(jsonPath("$.data[0].role_id").value("R"));
        http.perform(get("/api/roles").param("page", "0").param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.field_errors[0].field").value("page"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    void literalAssignmentsReadContractAndValidation() throws Exception {
        var before = snapshot();
        http.perform(get("/api/user-role-assignments").param("page", "1").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.meta.request_id").isString())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.size").value(10));
        http.perform(get("/api/user-role-assignments").param("page", "0").param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.field_errors[0].field").value("page"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    void literalPermissionsReadContractAndValidation() throws Exception {
        var before = snapshot();
        http.perform(get("/api/menu-permissions").param("page", "1").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.meta.request_id").isString())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.size").value(10));
        http.perform(get("/api/menu-permissions").param("page", "0").param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.field_errors[0].field").value("page"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    void literalMenuStructureReadContractAndValidation() throws Exception {
        var before = snapshot();
        http.perform(get("/api/menu-structure").param("page", "1").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.meta.request_id").isString())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.size").value(10))
                .andExpect(jsonPath("$.data[0].menu_id").value("M"));
        http.perform(get("/api/menu-structure").param("page", "0").param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.field_errors[0].field").value("page"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    void literalExecutionReadContractAndValidation() throws Exception {
        var before = snapshot();
        http.perform(get("/api/menu-execution-info").param("page", "1").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.meta.request_id").isString())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.size").value(10))
                .andExpect(jsonPath("$.data[0].screen_id").value("SCR"));
        http.perform(get("/api/menu-execution-info").param("page", "0").param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.field_errors[0].field").value("page"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    void literalCodeGroupsReadContractAndValidation() throws Exception {
        var before = snapshot();
        http.perform(get("/api/code-groups").param("page", "1").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.meta.request_id").isString())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.size").value(10))
                .andExpect(jsonPath("$.data[0].group_id").value("G"));
        http.perform(get("/api/code-groups").param("page", "0").param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.field_errors[0].field").value("page"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    void literalDetailCodesReadContractAndValidation() throws Exception {
        var before = snapshot();
        http.perform(get("/api/detail-codes").param("page", "1").param("size", "10").param("group_id", "G"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.meta.request_id").isString())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.size").value(10))
                .andExpect(jsonPath("$.data[0].code").value("P"));
        http.perform(get("/api/detail-codes").param("page", "0").param("size", "10").param("group_id", "G"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.field_errors[0].field").value("page"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    void literalRelationHistoryReadContractAndValidation() throws Exception {
        var before = snapshot();
        http.perform(get("/api/organization-relation-history").param("page", "1").param("size", "10")
                        .param("organization_code", "O"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.meta.request_id").isString())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.pagination.page").value(1))
                .andExpect(jsonPath("$.pagination.size").value(10))
                .andExpect(jsonPath("$.data[0].relation_id").value("INITIAL"));
        http.perform(get("/api/organization-relation-history").param("page", "0").param("size", "10")
                        .param("organization_code", "O"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.field_errors[0].field").value("page"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    void literalUsersAuthDenialHasNoSideEffects() throws Exception {
        var before = snapshot();
        when(authentication.authenticate(any())).thenReturn(Optional.empty());
        http.perform(put("/api/users").contentType("application/json").content("{\"user_id\":\"U\",\"enabled\":false}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
        when(authentication.authenticate(any())).thenReturn(Optional.of(new Identity("fixture-operator")));
        doThrow(new org.example.oct19.api.ApiFailure(403, "ACCESS_DENIED", "denied", null))
                .when(access).requireOperation(any(), anyString(), any());
        http.perform(put("/api/users").contentType("application/json").content("{\"user_id\":\"U\",\"enabled\":false}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    void literalUsersValidationHasNoSideEffects() throws Exception {
        var before = snapshot();
        http.perform(put("/api/users").contentType("application/json")
                        .content("{\"user_id\":\"U\",\"enabled\":\"false\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.field_errors[0].field").value("enabled"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    @DisplayName("side-effect: users.enabled changes while personnel source fields remain unchanged")
    void literalUsersHappyBusinessSideEffectReadback() throws Exception {
        seedNonemptyRelatedRows();
        var before = snapshot();
        http.perform(put("/api/users").contentType("application/json").content("{\"user_id\":\"U\",\"enabled\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.meta.request_id").isString())
                .andExpect(jsonPath("$.data.enabled").value(false));
        http.perform(get("/api/users").param("page", "1").param("size", "10").param("employee_number", "001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].enabled").value(false));
        assertEquals(1, writes);
        assertOwnedWriteAndNoninterference(before, Resource.USERS);
        var expectedUser = new LinkedHashMap<>(before.get(Resource.USERS).get(0));
        expectedUser.put("enabled", false);
        assertEquals(expectedUser, rows.get(Resource.USERS).get(0),
                "users must change only enabled, preserving every personnel source field");
    }

    @Test
    void literalOrganizationsAuthDenialHasNoSideEffects() throws Exception {
        var before = snapshot();
        when(authentication.authenticate(any())).thenReturn(Optional.empty());
        http.perform(put("/api/organizations").contentType("application/json").content(validBody("/api/organizations")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
        when(authentication.authenticate(any())).thenReturn(Optional.of(new Identity("fixture-operator")));
        doThrow(new org.example.oct19.api.ApiFailure(403, "ACCESS_DENIED", "denied", null))
                .when(access).requireOperation(any(), anyString(), any());
        http.perform(put("/api/organizations").contentType("application/json").content(validBody("/api/organizations")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    void literalOrganizationsValidationHasNoSideEffects() throws Exception {
        var before = snapshot();
        http.perform(put("/api/organizations").contentType("application/json")
                        .content("{\"parent_code\":\"P\",\"valid_from\":null,\"valid_to\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.field_errors[0].field").value("organization_code"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    @DisplayName("side-effect: organization_relations appends history without changing users or assignments")
    void literalOrganizationsHappyBusinessSideEffectReadback() throws Exception {
        seedNonemptyRelatedRows();
        var before = snapshot();
        http.perform(put("/api/organizations").contentType("application/json").content(validBody("/api/organizations")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.meta.request_id").isString())
                .andExpect(jsonPath("$.data.parent_code").value("P"));
        when(temporal.isCurrentRelation(any())).thenAnswer(call -> {
            Map<String, Object> relation = call.getArgument(0);
            var history = rows.get(Resource.RELATIONS);
            return history.get(history.size() - 1).get("relation_id").equals(relation.get("relation_id"));
        });
        http.perform(get("/api/organizations").param("page", "1").param("size", "10").param("organization_code", "O"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].parent_code").value("P"));
        assertEquals(1, writes);
        assertOwnedWriteAndNoninterference(before, Resource.RELATIONS);
        assertEquals(before.get(Resource.RELATIONS).size() + 1, rows.get(Resource.RELATIONS).size(),
                "organization_relations must append exactly one relation");
        assertEquals(before.get(Resource.RELATIONS),
                rows.get(Resource.RELATIONS).subList(0, before.get(Resource.RELATIONS).size()),
                "organization_relations must retain all previous history rows");
        assertEquals(before.get(Resource.ASSIGNMENTS), rows.get(Resource.ASSIGNMENTS),
                "user_role_assignments must not change when organization relations change");
    }

    @Test
    void literalRolesAuthDenialHasNoSideEffects() throws Exception {
        var before = snapshot();
        when(authentication.authenticate(any())).thenReturn(Optional.empty());
        http.perform(put("/api/roles").contentType("application/json").content(validBody("/api/roles")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
        when(authentication.authenticate(any())).thenReturn(Optional.of(new Identity("fixture-operator")));
        doThrow(new org.example.oct19.api.ApiFailure(403, "ACCESS_DENIED", "denied", null))
                .when(access).requireOperation(any(), anyString(), any());
        http.perform(put("/api/roles").contentType("application/json").content(validBody("/api/roles")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    void literalRolesValidationHasNoSideEffects() throws Exception {
        var before = snapshot();
        http.perform(put("/api/roles").contentType("application/json")
                        .content("{\"purpose\":\"목적\",\"grant_criteria\":\"기준\",\"default_data_scope\":\"범위\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.field_errors[0].field").value("role_id"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    @DisplayName("side-effect: roles definitions change without modifying user_role_assignments")
    void literalRolesHappyBusinessSideEffectReadback() throws Exception {
        seedNonemptyRelatedRows();
        var before = snapshot();
        http.perform(put("/api/roles").contentType("application/json").content(validBody("/api/roles")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.meta.request_id").isString())
                .andExpect(jsonPath("$.data.purpose").value("목적"));
        http.perform(get("/api/roles").param("page", "1").param("size", "10").param("role_id", "R"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].purpose").value("목적"));
        assertEquals(1, writes);
        assertOwnedWriteAndNoninterference(before, Resource.ROLES);
        assertEquals("목적", rows.get(Resource.ROLES).get(0).get("purpose"), "roles must persist purpose");
        assertEquals(before.get(Resource.ASSIGNMENTS), rows.get(Resource.ASSIGNMENTS),
                "user_role_assignments targets, roles, periods and origin must remain unchanged");
    }

    @Test
    void literalAssignmentsAuthDenialHasNoSideEffects() throws Exception {
        var before = snapshot();
        when(authentication.authenticate(any())).thenReturn(Optional.empty());
        http.perform(put("/api/user-role-assignments").contentType("application/json")
                        .content(validBody("/api/user-role-assignments")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
        when(authentication.authenticate(any())).thenReturn(Optional.of(new Identity("fixture-operator")));
        doThrow(new org.example.oct19.api.ApiFailure(403, "ACCESS_DENIED", "denied", null))
                .when(access).requireOperation(any(), anyString(), any());
        http.perform(put("/api/user-role-assignments").contentType("application/json")
                        .content(validBody("/api/user-role-assignments")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    void literalAssignmentsValidationHasNoSideEffects() throws Exception {
        var before = snapshot();
        http.perform(put("/api/user-role-assignments").contentType("application/json")
                        .content("{\"assignment_id\":\"A\",\"user_id\":\"U\",\"role_id\":\"R\","
                                + "\"valid_from\":\"2000-01-01\",\"valid_to\":\"2001-01-01\",\"action\":\"부여\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.field_errors[0].field").value("approver_id"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    @DisplayName("side-effect: user_role_assignments persists target, role, period, approver, action and origin")
    void literalAssignmentsHappyBusinessSideEffectReadback() throws Exception {
        seedNonemptyRelatedRows();
        var before = snapshot();
        http.perform(put("/api/user-role-assignments").contentType("application/json")
                        .content(validBody("/api/user-role-assignments")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.meta.request_id").isString())
                .andExpect(jsonPath("$.data.action").value("부여"));
        http.perform(get("/api/user-role-assignments").param("page", "1").param("size", "10").param("user_id", "U"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[1].assignment_id").value("A"))
                .andExpect(jsonPath("$.data[1].action").value("부여"))
                .andExpect(jsonPath("$.data[1].origin").value("수동"))
                .andExpect(jsonPath("$.data[1].approver_id").value("U"));
        assertEquals(1, writes);
        assertOwnedWriteAndNoninterference(before, Resource.ASSIGNMENTS);
        assertEquals(before.get(Resource.ASSIGNMENTS).get(0), rows.get(Resource.ASSIGNMENTS).get(0));
        var expectedAssignment = new LinkedHashMap<String, Object>();
        expectedAssignment.put("assignment_id", "A");
        expectedAssignment.put("user_id", "U");
        expectedAssignment.put("role_id", "R");
        expectedAssignment.put("valid_from", "2000-01-01");
        expectedAssignment.put("valid_to", "2001-01-01");
        expectedAssignment.put("approver_id", "U");
        expectedAssignment.put("action", "부여");
        expectedAssignment.put("origin", "수동");
        assertEquals(expectedAssignment, rows.get(Resource.ASSIGNMENTS).get(1),
                "user_role_assignments must persist every supplied assignment field and manual origin");
    }

    @Test
    void literalPermissionsAuthDenialHasNoSideEffects() throws Exception {
        var before = snapshot();
        when(authentication.authenticate(any())).thenReturn(Optional.empty());
        http.perform(put("/api/menu-permissions").contentType("application/json")
                        .content(validBody("/api/menu-permissions")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
        when(authentication.authenticate(any())).thenReturn(Optional.of(new Identity("fixture-operator")));
        doThrow(new org.example.oct19.api.ApiFailure(403, "ACCESS_DENIED", "denied", null))
                .when(access).requireOperation(any(), anyString(), any());
        http.perform(put("/api/menu-permissions").contentType("application/json")
                        .content(validBody("/api/menu-permissions")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    void literalPermissionsValidationHasNoSideEffects() throws Exception {
        var before = snapshot();
        http.perform(put("/api/menu-permissions").contentType("application/json")
                        .content("{\"permission_id\":\"Q\",\"menu_id\":\"M\",\"user_id\":\"U\",\"allowed\":\"true\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.field_errors[0].field").value("allowed"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    @DisplayName("side-effect: menu_permissions stores selected target, menu and allowed setting")
    void literalPermissionsHappyBusinessSideEffectReadback() throws Exception {
        seedNonemptyRelatedRows();
        var before = snapshot();
        http.perform(put("/api/menu-permissions").contentType("application/json")
                        .content(validBody("/api/menu-permissions")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.meta.request_id").isString())
                .andExpect(jsonPath("$.data.allowed").value(true));
        http.perform(get("/api/menu-permissions").param("page", "1").param("size", "10").param("menu_id", "M"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[1].allowed").value(true));
        assertEquals(1, writes);
        assertOwnedWriteAndNoninterference(before, Resource.PERMISSIONS);
        var permission = rows.get(Resource.PERMISSIONS).get(1);
        assertEquals("Q", permission.get("permission_id"), "menu_permissions must preserve the selected key");
        assertEquals("M", permission.get("menu_id"), "menu_permissions must persist the selected menu");
        assertEquals("U", permission.get("user_id"), "menu_permissions must persist the selected target");
        assertEquals(true, permission.get("allowed"), "menu_permissions must persist allowed");
    }

    @Test
    void literalMenuStructureAuthDenialHasNoSideEffects() throws Exception {
        var before = snapshot();
        when(authentication.authenticate(any())).thenReturn(Optional.empty());
        http.perform(put("/api/menu-structure").contentType("application/json")
                        .content(validBody("/api/menu-structure")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
        when(authentication.authenticate(any())).thenReturn(Optional.of(new Identity("fixture-operator")));
        doThrow(new org.example.oct19.api.ApiFailure(403, "ACCESS_DENIED", "denied", null))
                .when(access).requireOperation(any(), anyString(), any());
        http.perform(put("/api/menu-structure").contentType("application/json")
                        .content(validBody("/api/menu-structure")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    void literalMenuStructureValidationHasNoSideEffects() throws Exception {
        var before = snapshot();
        http.perform(put("/api/menu-structure").contentType("application/json")
                        .content("{\"menu_id\":\"M\",\"parent_menu_id\":\"P\",\"level\":2,\"display_order\":1.5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.field_errors[0].field").value("display_order"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    @DisplayName("side-effect: menus hierarchy changes without resequencing other parents")
    void literalMenuStructureHappyBusinessSideEffectReadback() throws Exception {
        seedNonemptyRelatedRows();
        seed(Resource.MENUS, "menu_id", "OTHER", "parent_menu_id", "OTHER", "level", 1, "display_order", 7);
        var before = snapshot();
        http.perform(put("/api/menu-structure").contentType("application/json")
                        .content(validBody("/api/menu-structure")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.meta.request_id").isString())
                .andExpect(jsonPath("$.data.display_order").value(3));
        http.perform(get("/api/menu-structure").param("page", "1").param("size", "10").param("parent_menu_id", "P"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].display_order").value(3));
        assertEquals(1, writes);
        assertOwnedWriteAndNoninterference(before, Resource.MENUS);
        assertEquals(before.get(Resource.MENUS).get(1), rows.get(Resource.MENUS).get(1));
        assertEquals(before.get(Resource.MENUS).get(2), rows.get(Resource.MENUS).get(2));
        assertEquals(Map.of("menu_id", "M", "parent_menu_id", "P", "level", 2, "display_order", 3),
                rows.get(Resource.MENUS).get(0), "menus must persist the requested hierarchy fields");
    }

    @Test
    void literalExecutionAuthDenialHasNoSideEffects() throws Exception {
        var before = snapshot();
        when(authentication.authenticate(any())).thenReturn(Optional.empty());
        http.perform(put("/api/menu-execution-info").contentType("application/json")
                        .content(validBody("/api/menu-execution-info")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
        when(authentication.authenticate(any())).thenReturn(Optional.of(new Identity("fixture-operator")));
        doThrow(new org.example.oct19.api.ApiFailure(403, "ACCESS_DENIED", "denied", null))
                .when(access).requireOperation(any(), anyString(), any());
        http.perform(put("/api/menu-execution-info").contentType("application/json")
                        .content(validBody("/api/menu-execution-info")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    void literalExecutionValidationHasNoSideEffects() throws Exception {
        var before = snapshot();
        http.perform(put("/api/menu-execution-info").contentType("application/json")
                        .content("{\"menu_id\":\"M\",\"url\":\"/x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.field_errors[0].field").value("screen_id"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    @DisplayName("side-effect: menu_execution_info changes while menu_permissions remains unchanged")
    void literalExecutionHappyBusinessSideEffectReadback() throws Exception {
        seedNonemptyRelatedRows();
        var before = snapshot();
        http.perform(put("/api/menu-execution-info").contentType("application/json")
                        .content(validBody("/api/menu-execution-info")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.meta.request_id").isString())
                .andExpect(jsonPath("$.data.screen_id").value("SCR"));
        http.perform(get("/api/menu-execution-info").param("page", "1").param("size", "10").param("menu_id", "M"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].menu_id").value("M"))
                .andExpect(jsonPath("$.data[0].screen_id").value("SCR"))
                .andExpect(jsonPath("$.data[0].url").value("/x"));
        assertEquals(1, writes);
        assertOwnedWriteAndNoninterference(before, Resource.EXECUTION);
        assertEquals(Map.of("menu_id", "M", "screen_id", "SCR", "url", "/x"),
                rows.get(Resource.EXECUTION).get(0), "menu_execution_info must persist screen_id and url");
        assertEquals(before.get(Resource.PERMISSIONS), rows.get(Resource.PERMISSIONS),
                "menu_permissions allowed values and targets must remain unchanged");
    }

    @Test
    void literalCodeGroupsAuthDenialHasNoSideEffects() throws Exception {
        var before = snapshot();
        when(authentication.authenticate(any())).thenReturn(Optional.empty());
        http.perform(put("/api/code-groups").contentType("application/json").content(validBody("/api/code-groups")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
        when(authentication.authenticate(any())).thenReturn(Optional.of(new Identity("fixture-operator")));
        doThrow(new org.example.oct19.api.ApiFailure(403, "ACCESS_DENIED", "denied", null))
                .when(access).requireOperation(any(), anyString(), any());
        http.perform(put("/api/code-groups").contentType("application/json").content(validBody("/api/code-groups")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    void literalCodeGroupsValidationHasNoSideEffects() throws Exception {
        var before = snapshot();
        http.perform(put("/api/code-groups").contentType("application/json")
                        .content("{\"name\":\"명칭\",\"description\":\"설명\",\"managing_department\":\"부서\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.field_errors[0].field").value("group_id"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    @DisplayName("side-effect: code_groups changes without modifying detail_codes")
    void literalCodeGroupsHappyBusinessSideEffectReadback() throws Exception {
        seedNonemptyRelatedRows();
        var before = snapshot();
        http.perform(put("/api/code-groups").contentType("application/json").content(validBody("/api/code-groups")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.meta.request_id").isString())
                .andExpect(jsonPath("$.data.name").value("명칭"));
        http.perform(get("/api/code-groups").param("page", "1").param("size", "10").param("group_id", "G"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("명칭"));
        assertEquals(1, writes);
        assertOwnedWriteAndNoninterference(before, Resource.GROUPS);
        assertEquals(Map.of("group_id", "G", "name", "명칭", "description", "설명",
                        "managing_department", "부서"), rows.get(Resource.GROUPS).get(0),
                "code_groups must persist all editable definition fields");
        assertEquals(before.get(Resource.DETAILS), rows.get(Resource.DETAILS),
                "detail_codes code, sort_order and parent_code must remain unchanged");
    }

    @Test
    void literalDetailCodesAuthDenialHasNoSideEffects() throws Exception {
        var before = snapshot();
        when(authentication.authenticate(any())).thenReturn(Optional.empty());
        http.perform(put("/api/detail-codes").contentType("application/json").content(validBody("/api/detail-codes")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
        when(authentication.authenticate(any())).thenReturn(Optional.of(new Identity("fixture-operator")));
        doThrow(new org.example.oct19.api.ApiFailure(403, "ACCESS_DENIED", "denied", null))
                .when(access).requireOperation(any(), anyString(), any());
        http.perform(put("/api/detail-codes").contentType("application/json").content(validBody("/api/detail-codes")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    void literalDetailCodesValidationHasNoSideEffects() throws Exception {
        var before = snapshot();
        http.perform(put("/api/detail-codes").contentType("application/json")
                        .content("{\"group_id\":\"G\",\"code\":\"C\",\"sort_order\":\"2\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.field_errors[0].field").value("sort_order"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    void literalDetailCodesHappyBusinessSideEffectReadback() throws Exception {
        seedNonemptyRelatedRows();
        seed(Resource.GROUPS, "group_id", "OTHER", "name", "다른 그룹", "description", "설명",
                "managing_department", "부서");
        seed(Resource.DETAILS, "group_id", "OTHER", "code", "J", "sort_order", 9, "parent_code", "J");
        var before = snapshot();
        http.perform(put("/api/detail-codes").contentType("application/json").content(validBody("/api/detail-codes")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.meta.request_id").isString())
                .andExpect(jsonPath("$.data.sort_order").value(2));
        http.perform(get("/api/detail-codes").param("page", "1").param("size", "10").param("group_id", "G"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[*].group_id").value(org.hamcrest.Matchers.everyItem(
                        org.hamcrest.Matchers.is("G"))))
                .andExpect(jsonPath("$.data[1].group_id").value("G"))
                .andExpect(jsonPath("$.data[1].code").value("C"))
                .andExpect(jsonPath("$.data[1].sort_order").value(2))
                .andExpect(jsonPath("$.data[1].parent_code").value("P"));
        assertEquals(1, writes);
        assertOwnedWriteAndNoninterference(before, Resource.DETAILS);
        assertEquals(before.get(Resource.DETAILS).get(1), rows.get(Resource.DETAILS).get(1));
    }

    @Test
    void usersScreenContractAllowsThenDeniesWithoutWrites() throws Exception {
        screenExecution("/admin/users", "searchUsers");
        var before = snapshot();
        http.perform(get("/admin/users"))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        when(access.mayNavigate(any(), eq("M"), any())).thenReturn(false);
        http.perform(get("/admin/users"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("MENU_FORBIDDEN"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    void organizationsScreenContractAllowsThenDeniesWithoutWrites() throws Exception {
        screenExecution("/admin/organizations", "getOrganizationRelations");
        var before = snapshot();
        http.perform(get("/admin/organizations"))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        when(access.mayNavigate(any(), eq("M"), any())).thenReturn(false);
        http.perform(get("/admin/organizations"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("MENU_FORBIDDEN"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    void rolesScreenContractAllowsThenDeniesWithoutWrites() throws Exception {
        screenExecution("/admin/roles", "listRoles");
        var before = snapshot();
        http.perform(get("/admin/roles"))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        when(access.mayNavigate(any(), eq("M"), any())).thenReturn(false);
        http.perform(get("/admin/roles"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("MENU_FORBIDDEN"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    void userRolesScreenContractAllowsThenDeniesWithoutWrites() throws Exception {
        screenExecution("/admin/user-roles", "listUserRoleAssignments");
        var before = snapshot();
        http.perform(get("/admin/user-roles"))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        when(access.mayNavigate(any(), eq("M"), any())).thenReturn(false);
        http.perform(get("/admin/user-roles"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("MENU_FORBIDDEN"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    void menuPermissionsScreenContractAllowsThenDeniesWithoutWrites() throws Exception {
        screenExecution("/admin/menu-permissions", "listMenuPermissions");
        var before = snapshot();
        http.perform(get("/admin/menu-permissions"))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        when(access.mayNavigate(any(), eq("M"), any())).thenReturn(false);
        http.perform(get("/admin/menu-permissions"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("MENU_FORBIDDEN"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    void menuStructureScreenContractAllowsThenDeniesWithoutWrites() throws Exception {
        screenExecution("/admin/menu-structure", "getMenuStructure");
        var before = snapshot();
        http.perform(get("/admin/menu-structure"))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        when(access.mayNavigate(any(), eq("M"), any())).thenReturn(false);
        http.perform(get("/admin/menu-structure"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("MENU_FORBIDDEN"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    void menuInfoScreenContractAllowsThenDeniesWithoutWrites() throws Exception {
        screenExecution("/admin/menu-info", "listMenuExecutionInfo");
        var before = snapshot();
        http.perform(get("/admin/menu-info"))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        when(access.mayNavigate(any(), eq("M"), any())).thenReturn(false);
        http.perform(get("/admin/menu-info"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("MENU_FORBIDDEN"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    void codeGroupsScreenContractAllowsThenDeniesWithoutWrites() throws Exception {
        screenExecution("/admin/code-groups", "listCodeGroups");
        var before = snapshot();
        http.perform(get("/admin/code-groups"))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        when(access.mayNavigate(any(), eq("M"), any())).thenReturn(false);
        http.perform(get("/admin/code-groups"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("MENU_FORBIDDEN"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    void detailCodesScreenContractAllowsThenDeniesWithoutWrites() throws Exception {
        screenExecution("/admin/detail-codes", "listDetailCodes");
        var before = snapshot();
        http.perform(get("/admin/detail-codes"))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        when(access.mayNavigate(any(), eq("M"), any())).thenReturn(false);
        http.perform(get("/admin/detail-codes"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("MENU_FORBIDDEN"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @Test
    void missingSynchronizedUserReturnsContractValidationErrorWithoutWrites() throws Exception {
        var before = snapshot();
        http.perform(put("/api/users").contentType("application/json")
                        .content("{\"user_id\":\"ABSENT\",\"enabled\":false}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.field_errors[0].field").value("user_id"))
                .andExpect(jsonPath("$.field_errors[0].message")
                        .value("Existing synchronized user is required"));
        assertEquals(before, snapshot());
        assertEquals(0, writes);
    }

    @SuppressWarnings("unchecked")
    private void screenExecution(String path, String operationId) throws Exception {
        Map<String, Object> contract;
        try (var input = new ClassPathResource("contracts/openapi.yaml").getInputStream()) {
            contract = new org.yaml.snakeyaml.Yaml().load(input);
        }
        var paths = (Map<String, Map<String, Map<String, Object>>>) contract.get("paths");
        assertTrue(paths.values().stream().flatMap(methods -> methods.values().stream())
                .anyMatch(operation -> operationId.equals(operation.get("operationId"))),
                "Screen scope must belong to the durable OpenAPI operation inventory");
        rows.get(Resource.EXECUTION).get(0).put("url", path);
    }

    private void seedNonemptyRelatedRows() {
        seed(Resource.ASSIGNMENTS, "assignment_id", "EXISTING", "user_id", "U", "role_id", "R",
                "valid_from", null, "valid_to", null, "approver_id", "U", "origin", "보직 기반", "action", "부여");
        seed(Resource.PERMISSIONS, "permission_id", "EXISTING", "user_id", "U", "menu_id", "M",
                "role_id", null, "organization_code", null, "allowed", false);
    }

    private void assertOwnedWriteAndNoninterference(
            Map<Resource, List<Map<String, Object>>> before, Resource owned) {
        assertNotEquals(before.get(owned), rows.get(owned), owned.table() + " must change");
        for (Resource resource : Resource.values()) {
            if (resource != owned) {
                assertEquals(before.get(resource), rows.get(resource), resource.table() + " must remain unchanged");
            }
        }
    }

    private String validBody(String path) throws Exception {
        Map<String, Object> body = switch (path) {
            case "/api/users" -> Map.of("user_id", "U", "enabled", true);
            case "/api/roles" -> Map.of("role_id", "R", "purpose", "목적",
                    "grant_criteria", "기준", "default_data_scope", "범위");
            case "/api/menu-permissions" -> Map.of("permission_id", "Q", "menu_id", "M",
                    "user_id", "U", "allowed", true);
            case "/api/menu-structure" -> Map.of("menu_id", "M", "parent_menu_id", "P",
                    "level", 2, "display_order", 3);
            case "/api/menu-execution-info" -> Map.of("menu_id", "M", "screen_id", "SCR", "url", "/x");
            case "/api/code-groups" -> Map.of("group_id", "G", "name", "명칭",
                    "description", "설명", "managing_department", "부서");
            case "/api/detail-codes" -> Map.of("group_id", "G", "code", "C", "sort_order", 2, "parent_code", "P");
            case "/api/user-role-assignments" -> null;
            case "/api/organizations" -> null;
            default -> throw new IllegalArgumentException(path);
        };
        if (path.equals("/api/user-role-assignments")) {
            return assignment("A", "R", "부여");
        }
        if (path.equals("/api/organizations")) {
            return "{\"organization_code\":\"O\",\"parent_code\":\"P\",\"valid_from\":null,\"valid_to\":null}";
        }
        return json.writeValueAsString(body);
    }

    private String assignment(String id, String role, String action) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("assignment_id", id);
        body.put("user_id", "U");
        body.put("role_id", role);
        body.put("valid_from", id.equals("B") ? "2022-01-01" : "2000-01-01");
        body.put("valid_to", id.equals("B") ? "2031-01-01" : "2001-01-01");
        body.put("approver_id", "U");
        body.put("action", action);
        return json.writeValueAsString(body);
    }

    private void saveAndRead(String path, String body, Resource owned, String field, Object expected) throws Exception {
        Map<Resource, List<Map<String, Object>>> before = snapshot();
        http.perform(put(path).contentType("application/json").content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data." + field).value(expected));
        String response = http.perform(get(path).param("page", "1").param("size", "10").param("group_id", "G"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").isArray())
                .andReturn().getResponse().getContentAsString();
        var data = json.readTree(response).get("data");
        boolean observed = false;
        for (var row : data) {
            if (row.has(field) && json.valueToTree(expected).equals(row.get(field))) {
                observed = true;
            }
        }
        assertTrue(observed, "Saved value must be observable through the owning GET handler");
        assertTrue(rows.get(owned).stream().anyMatch(row -> expected.equals(row.get(field))));
        for (Resource resource : Resource.values()) {
            if (resource != owned) {
                assertEquals(before.get(resource), rows.get(resource), "noninterference: " + resource);
            }
        }
    }

    private void seed(Resource resource, Object... pairs) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            row.put((String) pairs[i], pairs[i + 1]);
        }
        rows.get(resource).add(row);
    }

    private Map<Resource, List<Map<String, Object>>> snapshot() {
        Map<Resource, List<Map<String, Object>>> result = new LinkedHashMap<>();
        rows.forEach((resource, table) -> result.put(resource, table.stream()
                .map(row -> (Map<String, Object>) new LinkedHashMap<>(row)).toList()));
        return result;
    }
}
