package org.example.oct19;

import java.sql.Connection;
import java.sql.DriverManager;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import static org.junit.jupiter.api.Assertions.*;

/** Runner-owned PostgreSQL schema and rollback verification; requires TEST_DATABASE_* environment variables. */
class PersistenceContractTest {
    @Test
    void schemaIsIdempotentAndWritesRollback() throws Exception {
        String url = System.getenv("TEST_DATABASE_URL");
        org.junit.jupiter.api.Assumptions.assumeTrue(url != null, "Requires isolated PostgreSQL runner database");
        try (Connection connection = DriverManager.getConnection(
                url, System.getenv("TEST_DATABASE_USER"), System.getenv("TEST_DATABASE_PASSWORD"))) {
            connection.setAutoCommit(false);
            try {
                String schema = "contract_" + java.util.UUID.randomUUID().toString().replace("-", "");
                try (var statement = connection.createStatement()) {
                    statement.execute("CREATE SCHEMA " + schema);
                    statement.execute("SET LOCAL search_path TO " + schema);
                }
                ScriptUtils.executeSqlScript(connection, new ClassPathResource("schema.sql"));
                ScriptUtils.executeSqlScript(connection, new ClassPathResource("schema.sql"));
                JdbcTemplate jdbc = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
                assertEquals(10, jdbc.queryForObject(
                        "SELECT count(*) FROM information_schema.tables "
                        + "WHERE table_schema = current_schema() AND table_name IN "
                        + "('users','organizations','roles','user_role_assignments','menu_permissions',"
                        + "'menus','menu_execution_info','code_groups','detail_codes','organization_relations')",
                        Integer.class));
                assertEquals(10, jdbc.queryForObject(
                        "SELECT count(*) FROM information_schema.table_constraints "
                        + "WHERE table_schema = current_schema() AND constraint_type = 'PRIMARY KEY'",
                        Integer.class));
                assertEquals(13, jdbc.queryForObject(
                        "SELECT count(*) FROM information_schema.table_constraints "
                        + "WHERE table_schema = current_schema() AND constraint_type = 'FOREIGN KEY'",
                        Integer.class));
                var beforeWrite = connection.setSavepoint("before_write");
                String id = java.util.UUID.randomUUID().toString();
                jdbc.update("INSERT INTO roles(role_id, purpose) VALUES (?, ?)", id, "검증");
                assertEquals("검증", jdbc.queryForObject(
                        "SELECT purpose FROM roles WHERE role_id = ?", String.class, id));
                connection.rollback(beforeWrite);
                assertEquals(0, jdbc.queryForObject(
                        "SELECT count(*) FROM roles WHERE role_id = ?", Integer.class, id));
                assertEquals(0, jdbc.queryForObject(
                        "SELECT count(*) FROM information_schema.columns "
                        + "WHERE table_schema = current_schema() "
                        + "AND column_name IN ('business_roles','is_current')", Integer.class));
                jdbc.update("INSERT INTO code_groups(group_id) VALUES (?)", id);
                jdbc.update("INSERT INTO detail_codes(group_id, code) VALUES (?, ?)", id, "parent");
                jdbc.update("INSERT INTO detail_codes(group_id, code, parent_code) VALUES (?, ?, ?)",
                        id, "child", "parent");
                var beforeInvalid = connection.setSavepoint("before_invalid");
                var duplicate = assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
                        () -> jdbc.update("INSERT INTO detail_codes(group_id, code) VALUES (?, ?)", id, "child"));
                assertNotNull(duplicate.getCause());
                connection.rollback(beforeInvalid);
                jdbc.update("INSERT INTO code_groups(group_id) VALUES (?)", id + "_other");
                var beforeOrphan = connection.setSavepoint("before_orphan");
                assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
                        () -> jdbc.update(
                                "INSERT INTO detail_codes(group_id, code, parent_code) VALUES (?, ?, ?)",
                                id + "_other", "child", "parent"));
                connection.rollback(beforeOrphan);
                var beforeMissingUser = connection.setSavepoint("before_missing_user");
                assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
                        () -> jdbc.update(
                                "INSERT INTO user_role_assignments(assignment_id, user_id) VALUES (?, ?)", id, id));
                connection.rollback(beforeMissingUser);
                assertFalse(connection.getAutoCommit());
            } finally {
                connection.rollback();
            }
        }
    }
}
