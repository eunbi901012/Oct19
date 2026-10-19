package org.example.oct19.mapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.example.oct19.model.Resource;

/** Whitelisted PostgreSQL SQL provider; values are always bound, never interpolated. */
public class CatalogSql {
    /** Adds optional predicates dynamically rather than binding untyped null predicates. */
    @SuppressWarnings("unchecked")
    public String select(Map<String, Object> parameters) {
        Resource resource = (Resource) parameters.get("resource");
        Map<String, Object> filters = (Map<String, Object>) parameters.get("filters");
        StringBuilder sql = new StringBuilder("SELECT\n    ")
                .append(String.join(",\n    ", resource.columns())).append("\nFROM ").append(resource.table());
        List<String> predicates = resource.columns().stream()
                .filter(column -> filters.get(column) != null)
                .map(column -> column + " = #{filters." + column + "}").toList();
        if (!predicates.isEmpty()) {
            sql.append("\nWHERE ").append(String.join("\n    AND ", predicates));
        }
        String ordering = switch (resource) {
            case MENUS -> "parent_menu_id, display_order, menu_id";
            case DETAILS -> "group_id, sort_order, code";
            case RELATIONS -> "organization_code, valid_from, relation_id";
            default -> String.join(", ", resource.keys());
        };
        return sql.append("\nORDER BY ").append(ordering).toString();
    }

    /** Performs atomic PostgreSQL upserts on real primary keys, preserving omitted fields and assignment origin. */
    @SuppressWarnings("unchecked")
    public String save(Map<String, Object> parameters) {
        Resource resource = (Resource) parameters.get("resource");
        Map<String, Object> row = (Map<String, Object>) parameters.get("row");
        if (resource == Resource.USERS) {
            return """
                    UPDATE users
                    SET enabled = #{row.enabled,jdbcType=BOOLEAN}
                    WHERE user_id = #{row.user_id,jdbcType=VARCHAR}
                    """;
        }
        List<String> columns = resource.columns().stream().filter(row::containsKey).toList();
        String values = columns.stream().map(column -> binding(column)).collect(Collectors.joining(",\n    "));
        List<String> editable = columns.stream().filter(column -> !resource.keys().contains(column))
                .filter(column -> resource != Resource.ASSIGNMENTS || !column.equals("origin")).toList();
        String conflict = editable.isEmpty() ? "DO NOTHING" : "DO UPDATE SET\n    " + editable.stream()
                .map(column -> column + " = EXCLUDED." + column).collect(Collectors.joining(",\n    "));
        return "INSERT INTO " + resource.table() + " (\n    " + String.join(",\n    ", columns)
                + "\n) VALUES (\n    " + values + "\n)\nON CONFLICT ("
                + String.join(", ", resource.keys()) + ") " + conflict;
    }

    private static String binding(String column) {
        String type = switch (column) {
            case "valid_from", "valid_to", "retired_on" -> "DATE";
            case "enabled", "allowed" -> "BOOLEAN";
            case "level", "display_order", "sort_order" -> "INTEGER";
            default -> "VARCHAR";
        };
        return "#{row." + column + ",jdbcType=" + type + "}";
    }
}
