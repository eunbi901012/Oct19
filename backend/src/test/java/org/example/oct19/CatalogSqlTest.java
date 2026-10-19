package org.example.oct19;

import java.util.LinkedHashMap;
import java.util.Map;
import org.example.oct19.mapper.CatalogSql;
import org.example.oct19.model.Resource;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Provider assertions are SQL-shape checks only, not PostgreSQL execution evidence. */
class CatalogSqlTest {
    @Test
    void optionalNullPredicatesAreAbsentAndUserSettingCannotInsertSourceRows() {
        CatalogSql provider = new CatalogSql();
        Map<String, Object> filters = new LinkedHashMap<>();
        filters.put("organization_code", null);
        String select = provider.select(Map.of("resource", Resource.USERS, "filters", filters));
        assertFalse(select.contains("WHERE"));
        assertFalse(select.contains("IS NULL"));
        filters.put("organization_code", "O");
        select = provider.select(Map.of("resource", Resource.USERS, "filters", filters));
        assertTrue(select.contains("organization_code = #{filters.organization_code}"));
        String save = provider.save(Map.of("resource", Resource.USERS,
                "row", Map.of("user_id", "U", "enabled", false)));
        assertTrue(save.startsWith("UPDATE users"));
        assertFalse(save.contains("INSERT"));
        assertFalse(save.contains("person_name"));
    }

    @Test
    void omittedOptionalParentAndExistingOriginAreNotOverwritten() {
        CatalogSql provider = new CatalogSql();
        String detail = provider.save(Map.of("resource", Resource.DETAILS,
                "row", Map.of("group_id", "G", "code", "C", "sort_order", 4)));
        assertFalse(detail.contains("parent_code"));
        assertTrue(detail.contains("ON CONFLICT (group_id, code)"));
        String assignment = provider.save(Map.of("resource", Resource.ASSIGNMENTS,
                "row", Map.of("assignment_id", "A", "origin", "수동", "action", "변경")));
        assertFalse(assignment.substring(assignment.indexOf("DO UPDATE")).contains("origin"));
    }
}
