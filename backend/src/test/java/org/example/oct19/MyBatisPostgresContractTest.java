package org.example.oct19;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.example.oct19.mapper.CatalogMapper;
import org.example.oct19.model.Resource;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

/** Selected-engine SQL and actual MyBatis map materialization, isolated from production schemas and policies. */
class MyBatisPostgresContractTest {
    @Test
    void dynamicFiltersNullsOrderingAndOmittedUpdatesUseActualPostgres() throws Exception {
        String url = System.getenv("TEST_DATABASE_URL");
        Assumptions.assumeTrue(url != null && !url.isBlank(), "Requires isolated PostgreSQL runner database");
        try (Connection connection = DriverManager.getConnection(
                url, System.getenv("TEST_DATABASE_USER"), System.getenv("TEST_DATABASE_PASSWORD"))) {
            connection.setAutoCommit(false);
            try {
                String schema = "mapper_" + UUID.randomUUID().toString().replace("-", "");
                try (var statement = connection.createStatement()) {
                    statement.execute("CREATE SCHEMA " + schema);
                    statement.execute("SET LOCAL search_path TO " + schema);
                }
                ScriptUtils.executeSqlScript(connection, new ClassPathResource("schema.sql"));
                var source = new SingleConnectionDataSource(connection, true);
                JdbcTemplate jdbc = new JdbcTemplate(source);
                jdbc.update("""
                        INSERT INTO users (
                            user_id, employee_number, person_name, organization_code,
                            job_grade, employment_status, enabled, position, retired_on, last_synced_at
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """, "person-a", "00007", "검증 사용자", "source-org", "source-grade",
                        "source-status", false, "source-position", null,
                        Timestamp.valueOf("2026-01-01 10:11:12.123456"));
                jdbc.update("""
                        INSERT INTO roles (
                            role_id, purpose, grant_criteria, default_data_scope
                        ) VALUES (?, ?, ?, ?)
                        """, "role-a", "이전 목적", "기준", "기본범위");
                jdbc.update("""
                        INSERT INTO user_role_assignments (
                            assignment_id, user_id, role_id, origin, action
                        ) VALUES (?, ?, ?, ?, ?)
                        """, "assignment-a", "person-a", "role-a", "보직 기반", "부여");
                jdbc.update("INSERT INTO code_groups(group_id, name) VALUES (?, ?)", "group-a", "그룹");
                jdbc.update("INSERT INTO code_groups(group_id, name) VALUES (?, ?)", "group-b", "다른 그룹");

                Configuration configuration = new Configuration(new Environment(
                        "isolated-postgresql", new JdbcTransactionFactory(), source));
                configuration.setCallSettersOnNulls(true);
                configuration.addMapper(CatalogMapper.class);
                var factory = new SqlSessionFactoryBuilder().build(configuration);
                try (var session = factory.openSession()) {
                    CatalogMapper mapper = session.getMapper(CatalogMapper.class);
                    assertEquals(1, mapper.ping());
                    var absentFilter = new LinkedHashMap<String, Object>();
                    absentFilter.put("employee_number", null);
                    var users = mapper.select(Resource.USERS, absentFilter);
                    assertEquals(1, users.size());
                    var person = users.get(0);
                    assertEquals("00007", person.get("employee_number"));
                    assertEquals(false, person.get("enabled"));
                    assertTrue(person.containsKey("retired_on"));
                    assertNull(person.get("retired_on"));
                    assertTrue(person.get("last_synced_at") instanceof Timestamp);
                    assertEquals(Timestamp.valueOf("2026-01-01 10:11:12.123456").toInstant(),
                            ((Timestamp) person.get("last_synced_at")).toInstant());
                    assertTrue(mapper.select(Resource.USERS, Map.of("enabled", false)).size() == 1);
                    assertTrue(mapper.select(Resource.USERS, Map.of("enabled", true)).isEmpty());
                    assertTrue(mapper.select(Resource.USERS, Map.of("user_id", "missing")).isEmpty());
                    assertFalse(person.containsKey("business_roles"));

                    var assignmentsBefore = jdbc.queryForList("SELECT * FROM user_role_assignments");
                    assertEquals(1, mapper.save(Resource.ROLES, Map.of(
                            "role_id", "role-a", "purpose", "새 목적", "grant_criteria", "새 기준",
                            "default_data_scope", "새 기본범위")));
                    assertEquals("새 목적", mapper.select(Resource.ROLES, Map.of("role_id", "role-a"))
                            .get(0).get("purpose"));
                    assertEquals(assignmentsBefore, jdbc.queryForList("SELECT * FROM user_role_assignments"));
                    assertEquals(1, mapper.save(Resource.ASSIGNMENTS, Map.of(
                            "assignment_id", "assignment-a", "action", "변경", "origin", "수동")));
                    var assignment = mapper.select(Resource.ASSIGNMENTS,
                            Map.of("assignment_id", "assignment-a")).get(0);
                    assertEquals("보직 기반", assignment.get("origin"));
                    assertEquals("person-a", assignment.get("user_id"));
                    assertEquals("변경", assignment.get("action"));
                    assertFalse(assignment.containsKey("is_current"));

                    assertEquals(1, mapper.save(Resource.DETAILS,
                            Map.of("group_id", "group-a", "code", "parent", "sort_order", 1)));
                    assertEquals(1, mapper.save(Resource.DETAILS, Map.of(
                            "group_id", "group-a", "code", "later", "sort_order", 3, "parent_code", "parent")));
                    assertEquals(1, mapper.save(Resource.DETAILS, Map.of(
                            "group_id", "group-a", "code", "earlier", "sort_order", 2, "parent_code", "parent")));
                    assertEquals(1, mapper.save(Resource.DETAILS,
                            Map.of("group_id", "group-b", "code", "other", "sort_order", 0)));
                    var detailRows = mapper.select(Resource.DETAILS, Map.of("group_id", "group-a"));
                    assertEquals(java.util.List.of("parent", "earlier", "later"),
                            detailRows.stream().map(row -> row.get("code")).toList());
                    assertEquals(1, mapper.save(Resource.DETAILS,
                            Map.of("group_id", "group-a", "code", "later", "sort_order", 4)));
                    assertEquals("parent", mapper.select(Resource.DETAILS,
                            Map.of("group_id", "group-a", "code", "later")).get(0).get("parent_code"));
                    var detailsBefore = jdbc.queryForList("SELECT * FROM detail_codes ORDER BY group_id, code");
                    assertEquals(1, mapper.save(Resource.GROUPS,
                            Map.of("group_id", "group-a", "name", "변경된 그룹")));
                    assertEquals(detailsBefore,
                            jdbc.queryForList("SELECT * FROM detail_codes ORDER BY group_id, code"));
                    session.rollback();
                }
            } finally {
                connection.rollback();
            }
        }
    }
}
