package org.example.oct19.api;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.example.oct19.model.Resource;
import org.springframework.stereotype.Component;

/** Strict request-object validation preserving omitted versus explicit-null properties. */
@Component
public class InputValidator {
    private static final Set<String> DATES = Set.of("valid_from", "valid_to");
    private static final Set<String> INTEGERS = Set.of("level", "display_order", "sort_order");
    private static final Set<String> BOOLEANS = Set.of("enabled", "allowed");

    /** Validates an operation's exact writable field model without coercion or extra properties. */
    public Map<String, Object> validate(Resource resource, JsonNode input) {
        if (input == null || !input.isObject()) {
            throw ApiFailure.invalid("body", "A JSON object is required");
        }
        List<String> allowed = allowed(resource);
        input.fieldNames().forEachRemaining(field -> {
            if (!allowed.contains(field)) {
                String message = resource == Resource.USERS
                        ? "Source and role-projection fields are read-only; only user_id and enabled are writable"
                        : "Property is not writable for this operation";
                throw ApiFailure.invalid(field, message);
            }
        });
        List<String> required = required(resource);
        for (String field : required) {
            if (!input.has(field)) {
                throw ApiFailure.invalid(field, "Required property is missing");
            }
        }
        Map<String, Object> values = new LinkedHashMap<>();
        for (String field : allowed) {
            if (!input.has(field)) {
                continue;
            }
            JsonNode node = input.get(field);
            if (node.isNull()) {
                boolean nullable = DATES.contains(field)
                        || resource == Resource.PERMISSIONS && Set.of("role_id", "organization_code", "user_id")
                                .contains(field);
                if (!nullable) {
                    throw ApiFailure.invalid(field, "Null is not allowed");
                }
                values.put(field, null);
                continue;
            }
            if (BOOLEANS.contains(field)) {
                if (!node.isBoolean()) {
                    throw ApiFailure.invalid(field, "A boolean is required");
                }
                values.put(field, node.booleanValue());
            } else if (INTEGERS.contains(field)) {
                if (!node.isIntegralNumber() || !node.canConvertToInt()) {
                    throw ApiFailure.invalid(field, "An integer representable by the database is required");
                }
                values.put(field, node.intValue());
            } else {
                if (!node.isTextual()) {
                    throw ApiFailure.invalid(field, "A string is required");
                }
                String value = node.textValue();
                if (DATES.contains(field)) {
                    try {
                        values.put(field, LocalDate.parse(value));
                    } catch (DateTimeParseException failure) {
                        throw ApiFailure.invalid(field, "An ISO calendar date is required");
                    }
                } else {
                    values.put(field, value);
                }
            }
        }
        if (resource == Resource.ASSIGNMENTS && !Set.of("부여", "변경", "회수").contains(values.get("action"))) {
            throw ApiFailure.invalid("action", "Expected 부여, 변경 or 회수");
        }
        if (resource == Resource.PERMISSIONS) {
            boolean selected = List.of("role_id", "organization_code", "user_id").stream()
                    .anyMatch(field -> values.get(field) instanceof String value && !value.isEmpty());
            if (!selected) {
                throw ApiFailure.invalid("role_id",
                        "At least one nonempty role_id, organization_code or user_id is required");
            }
        }
        return values;
    }

    /** Returns the exact write allowlist; source fields and projections are never allowed. */
    public List<String> allowed(Resource resource) {
        String fields = switch (resource) {
            case USERS -> "user_id enabled";
            case RELATIONS -> "organization_code parent_code valid_from valid_to";
            case ASSIGNMENTS -> "assignment_id user_id role_id valid_from valid_to approver_id action";
            default -> String.join(" ", resource.columns());
        };
        return Arrays.asList(fields.split(" "));
    }

    private List<String> required(Resource resource) {
        return switch (resource) {
            case PERMISSIONS -> List.of("permission_id", "menu_id", "allowed");
            case DETAILS -> List.of("group_id", "code", "sort_order");
            default -> allowed(resource);
        };
    }
}
