package org.example.oct19;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.DriverManager;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.example.oct19.security.AccessPolicy;
import org.example.oct19.security.AuthenticationAdapter;
import org.example.oct19.security.BusinessPolicy;
import org.example.oct19.security.Identity;
import org.example.oct19.security.TemporalPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Real PostgreSQL committed HTTP save/readback probes; policy doubles are not approved institutional auth proof. */
@EnabledIfEnvironmentVariable(named = "TEST_DATABASE_URL", matches = ".+")
@SpringBootTest(properties = {
        "DATABASE_URL=${TEST_DATABASE_URL}",
        "DATABASE_USER=${TEST_DATABASE_USER}",
        "DATABASE_PASSWORD=${TEST_DATABASE_PASSWORD}",
        "spring.flyway.enabled=true"
})
@AutoConfigureMockMvc
class PostgreSqlHttpTest {
    @Autowired MockMvc http;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @MockBean AuthenticationAdapter authentication;
    @MockBean AccessPolicy access;
    @MockBean TemporalPolicy temporal;
    @MockBean BusinessPolicy business;
    String id;

    @BeforeEach
    void isolatedApprovedFixture() {
        id = "http-" + UUID.randomUUID();
        when(authentication.authenticate(any())).thenReturn(Optional.of(new Identity("test-only-approved-subject")));
        when(temporal.isCurrentAssignment(any())).thenReturn(true);
        when(temporal.isCurrentRelation(any())).thenReturn(true);
        jdbc.update("""
                INSERT INTO users (
                    user_id, employee_number, person_name, organization_code, job_grade,
                    employment_status, enabled, position
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, id, id, "원천", id, "직급", "재직", true, "보직");
        jdbc.update("""
                INSERT INTO code_groups (
                    group_id, name, description, managing_department
                ) VALUES (?, ?, ?, ?)
                """, id, "원본", "설명", "부서");
        jdbc.update("""
                INSERT INTO detail_codes (
                    group_id, code, sort_order, parent_code
                ) VALUES (?, ?, ?, ?)
                """, id, "P", 0, "P");
    }

    @AfterEach
    void removeOnlyThisTestsCommittedRows() {
        jdbc.update("DELETE FROM detail_codes WHERE group_id = ? AND code <> ?", id, "P");
        jdbc.update("DELETE FROM detail_codes WHERE group_id = ?", id);
        jdbc.update("DELETE FROM code_groups WHERE group_id = ?", id);
        jdbc.update("DELETE FROM roles WHERE role_id = ?", id);
        jdbc.update("DELETE FROM users WHERE user_id = ?", id);
    }

    @Test
    void userSaveCommitsAndIndependentConnectionPreservesSourceFields() throws Exception {
        http.perform(put("/api/users").contentType("application/json")
                        .content(json.writeValueAsString(Map.of("user_id", id, "enabled", false))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.enabled").value(false));
        try (var connection = DriverManager.getConnection(System.getenv("TEST_DATABASE_URL"),
                System.getenv("TEST_DATABASE_USER"), System.getenv("TEST_DATABASE_PASSWORD"));
                var statement = connection.prepareStatement(
                        "SELECT enabled, employee_number, person_name, position FROM users WHERE user_id = ?")) {
            statement.setString(1, id);
            try (var result = statement.executeQuery()) {
                assertTrue(result.next());
                assertFalse(result.getBoolean("enabled"));
                assertEquals(id, result.getString("employee_number"));
                assertEquals("원천", result.getString("person_name"));
                assertEquals("보직", result.getString("position"));
            }
        }
        http.perform(get("/api/users").param("page", "1").param("size", "10").param("employee_number", id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].enabled").value(false));
    }

    @Test
    void roleSaveAndOptionalPredicatesRunOnSelectedEngine() throws Exception {
        http.perform(put("/api/roles").contentType("application/json").content(json.writeValueAsString(Map.of(
                        "role_id", id, "purpose", "목적", "grant_criteria", "기준", "default_data_scope", "범위"))))
                .andExpect(status().isOk());
        http.perform(get("/api/roles").param("page", "1").param("size", "10").param("role_id", id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].purpose").value("목적"));
        // No optional query conditions: exercises the dynamic SQL branch on PostgreSQL, not H2.
        http.perform(get("/api/roles").param("page", "1").param("size", "10"))
                .andExpect(status().isOk());
        assertEquals("목적", jdbc.queryForObject("SELECT purpose FROM roles WHERE role_id = ?", String.class, id));
    }

    @Test
    void detailParentOmissionAndGroupNoninterferenceArePersisted() throws Exception {
        http.perform(put("/api/detail-codes").contentType("application/json").content(json.writeValueAsString(Map.of(
                        "group_id", id, "code", "C", "sort_order", 2, "parent_code", "P"))))
                .andExpect(status().isOk());
        http.perform(put("/api/detail-codes").contentType("application/json").content(json.writeValueAsString(Map.of(
                        "group_id", id, "code", "C", "sort_order", 1))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.parent_code").value("P"));
        http.perform(put("/api/code-groups").contentType("application/json").content(json.writeValueAsString(Map.of(
                        "group_id", id, "name", "수정", "description", "설명", "managing_department", "부서"))))
                .andExpect(status().isOk());
        http.perform(get("/api/detail-codes").param("page", "1").param("size", "10").param("group_id", id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data[1].code").value("C"))
                .andExpect(jsonPath("$.data[1].sort_order").value(1));
        assertEquals("P", jdbc.queryForObject(
                "SELECT parent_code FROM detail_codes WHERE group_id = ? AND code = ?", String.class, id, "C"));
    }

    @Test
    void failureAfterActualInsertRollsBackWholeHttpTransaction() throws Exception {
        // Test-only policy permits the unresolved root input so response validation fails AFTER the real INSERT.
        http.perform(put("/api/detail-codes").contentType("application/json").content(json.writeValueAsString(Map.of(
                        "group_id", id, "code", "ROLLBACK", "sort_order", 5))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DATA_CONTRACT_ERROR"));
        try (var connection = DriverManager.getConnection(System.getenv("TEST_DATABASE_URL"),
                System.getenv("TEST_DATABASE_USER"), System.getenv("TEST_DATABASE_PASSWORD"));
                var statement = connection.prepareStatement(
                        "SELECT count(*) FROM detail_codes WHERE group_id = ? AND code = ?")) {
            statement.setString(1, id);
            statement.setString(2, "ROLLBACK");
            try (var result = statement.executeQuery()) {
                assertTrue(result.next());
                assertEquals(0, result.getInt(1));
            }
        }
    }

    @Test
    void invalidParentRollsBackWithoutPartialDetailInsert() throws Exception {
        int count = jdbc.queryForObject("SELECT count(*) FROM detail_codes WHERE group_id = ?", Integer.class, id);
        http.perform(put("/api/detail-codes").contentType("application/json").content(json.writeValueAsString(Map.of(
                        "group_id", id, "code", "C", "sort_order", 2, "parent_code", "ABSENT"))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.field_errors[0].field").value("parent_code"));
        assertEquals(count, jdbc.queryForObject(
                "SELECT count(*) FROM detail_codes WHERE group_id = ?", Integer.class, id));
    }
}
