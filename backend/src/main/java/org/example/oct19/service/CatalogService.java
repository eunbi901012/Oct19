package org.example.oct19.service;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.example.oct19.api.ApiFailure;
import org.example.oct19.api.Envelope;
import org.example.oct19.api.InputValidator;
import org.example.oct19.mapper.CatalogMapper;
import org.example.oct19.model.Resource;
import org.example.oct19.security.AccessPolicy;
import org.example.oct19.security.AuthenticationAdapter;
import org.example.oct19.security.BusinessPolicy;
import org.example.oct19.security.Identity;
import org.example.oct19.security.PolicyData;
import org.example.oct19.security.TemporalPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Transactional application service with write ownership, strict validation and explicit approval boundaries. */
@Service
public class CatalogService {
    private final CatalogMapper mapper;
    private final AuthenticationAdapter authentication;
    private final AccessPolicy access;
    private final TemporalPolicy temporal;
    private final BusinessPolicy business;
    private final InputValidator validator;
    private final TransactionTemplate transaction;

    /** Wires approved policies independently from blocking persistence and HTTP validation. */
    public CatalogService(CatalogMapper mapper, AuthenticationAdapter authentication, AccessPolicy access,
            TemporalPolicy temporal, BusinessPolicy business, InputValidator validator,
            PlatformTransactionManager transactions) {
        this.mapper = mapper;
        this.authentication = authentication;
        this.access = access;
        this.temporal = temporal;
        this.business = business;
        this.validator = validator;
        this.transaction = new TransactionTemplate(transactions);
    }

    /** Reads a contract page; filters precede pagination and role projections use approved temporal policy. */
    public Map<String, Object> read(String operation, Resource resource, Map<String, String> query,
            HttpServletRequest request) {
        Identity identity = authenticate(request);
        PolicyData data = snapshot();
        access.requireOperation(identity, operation, data);
        int page = positive(query.get("page"), "page");
        int size = positive(query.get("size"), "size");
        if (resource == Resource.DETAILS) {
            requireQuery(query, "group_id");
        }
        if (resource == Resource.RELATIONS) {
            requireQuery(query, "organization_code");
        }
        if (query.containsKey("filter") && !query.get("filter").isEmpty()) {
            throw ApiFailure.invalid("filter",
                    "Generic filter semantics require approval; use named contract conditions");
        }
        Map<String, Object> filters = filters(resource, query);
        List<Map<String, Object>> selected = mapper.select(resource, filters);
        List<Map<String, Object>> projected = new ArrayList<>();
        for (Map<String, Object> row : selected) {
            Map<String, Object> result = project(resource, row, data);
            if (resource == Resource.USERS && query.containsKey("business_roles")) {
                List<?> roles = (List<?>) result.get("business_roles");
                if (!roles.contains(query.get("business_roles"))) {
                    continue;
                }
            }
            projected.add(result);
        }
        long start = ((long) page - 1) * size;
        int from = (int) Math.min(start, projected.size());
        int to = (int) Math.min(start + size, projected.size());
        Map<String, Object> response = Envelope.success(projected.subList(from, to));
        response.put("pagination", Map.of("page", page, "size", size, "total", projected.size()));
        return response;
    }

    /** Persists exactly the owning resource, preserving source columns, other tables and omitted optional fields. */
    public Map<String, Object> save(String operation, Resource resource, JsonNode input, HttpServletRequest request) {
        Identity identity = authenticate(request);
        return transaction.execute(status -> saveAuthorized(operation, resource, input, identity));
    }

    private Map<String, Object> saveAuthorized(String operation, Resource resource, JsonNode input, Identity identity) {
        PolicyData before = snapshot();
        access.requireOperation(identity, operation, before);
        Map<String, Object> row = validator.validate(resource, input);
        Map<String, Object> existing = existing(resource, row, before);
        if (resource == Resource.USERS && existing == null) {
            throw ApiFailure.invalid("user_id", "Existing synchronized user is required");
        }
        if (resource == Resource.ASSIGNMENTS) {
            if (existing == null && !"부여".equals(row.get("action"))) {
                throw ApiFailure.invalid("assignment_id", "Change or revoke requires an existing assignment");
            }
            row.put("origin", existing == null ? "수동" : existing.get("origin"));
        }
        business.approve(identity, resource, Collections.unmodifiableMap(row), before);
        validateReferences(resource, row, before);
        // Check policy-dependent calculations before persistence, including fixtures without database rollback.
        if (resource == Resource.ASSIGNMENTS) {
            temporal.isCurrentAssignment(row);
        }
        if (resource == Resource.RELATIONS) {
            temporal.isCurrentRelation(row);
            row.put("relation_id", UUID.randomUUID().toString());
        }
        if (resource == Resource.USERS) {
            project(resource, existing, before);
        }
        if (mapper.save(resource, row) != 1) {
            throw ApiFailure.invalid(resource.keys().get(0), "Save did not affect exactly one record");
        }
        PolicyData after = snapshot();
        Map<String, Object> persisted = existing(resource, row, after);
        if (persisted == null) {
            throw new ApiFailure(400, "PERSISTENCE_ERROR", "Saved record could not be read back", null);
        }
        if (resource == Resource.RELATIONS) {
            // The accepted save response is Organization, not the physical relation row.
            Map<String, Object> organization = find(Resource.ORGANIZATIONS, "organization_code",
                    row.get("organization_code"), after);
            return Envelope.success(organization(organization, persisted, after));
        }
        return Envelope.success(project(resource, persisted, after));
    }

    /** Combines persisted hierarchy/execution rows, excluding denied menus using the shared effective access policy. */
    public Map<String, Object> accessible(HttpServletRequest request) {
        Identity identity = authenticate(request);
        PolicyData data = snapshot();
        access.requireOperation(identity, "getAccessibleMenus", data);
        List<Map<String, Object>> menus = new ArrayList<>();
        for (Map<String, Object> row : mapper.select(Resource.MENUS, Map.of())) {
            String menuId = (String) row.get("menu_id");
            if (!access.mayNavigate(identity, menuId, data)) {
                continue;
            }
            Map<String, Object> execution = find(Resource.EXECUTION, "menu_id", menuId, data);
            if (execution == null) {
                throw ApiFailure.pending("OQ-UI-003 navigation entry without execution information");
            }
            Map<String, Object> result = new LinkedHashMap<>();
            for (String field : List.of("menu_id", "parent_menu_id", "display_order")) {
                result.put(field, row.get(field));
            }
            result.put("screen_id", execution.get("screen_id"));
            result.put("url", execution.get("url"));
            enforceRequired(result, List.copyOf(result.keySet()), List.of());
            menus.add(result);
        }
        return Envelope.success(Map.of("menus", menus));
    }

    private Identity authenticate(HttpServletRequest request) {
        return authentication.authenticate(request).orElseThrow(() -> new ApiFailure(401, "AUTHENTICATION_REQUIRED",
                "Institution-approved authentication required: OQ-AUTH-001 adapter absent or credential invalid",
                null));
    }

    private PolicyData snapshot() {
        Map<Resource, List<Map<String, Object>>> tables = new EnumMap<>(Resource.class);
        for (Resource resource : Resource.values()) {
            tables.put(resource, mapper.select(resource, Map.of()).stream()
                    .map(row -> Collections.unmodifiableMap(new LinkedHashMap<>(row))).toList());
        }
        return new PolicyData(Collections.unmodifiableMap(tables));
    }

    private Map<String, Object> project(Resource resource, Map<String, Object> source, PolicyData data) {
        if (resource == Resource.ORGANIZATIONS) {
            List<Map<String, Object>> current = data.rows(Resource.RELATIONS).stream()
                    .filter(row -> Objects.equals(source.get("organization_code"), row.get("organization_code")))
                    .filter(temporal::isCurrentRelation).toList();
            if (current.size() != 1) {
                throw ApiFailure.pending("OQ-ORG-001 root or ambiguous current relation representation");
            }
            return organization(source, current.get(0), data);
        }
        Map<String, Object> row = new LinkedHashMap<>();
        for (String field : resource.columns()) {
            row.put(field, wire(source.get(field)));
        }
        if (resource == Resource.USERS) {
            List<String> roles = data.rows(Resource.ASSIGNMENTS).stream()
                    .filter(assignment -> Objects.equals(source.get("user_id"), assignment.get("user_id")))
                    .filter(temporal::isCurrentAssignment).map(assignment -> (String) assignment.get("role_id"))
                    .distinct().toList();
            row.put("business_roles", roles);
        }
        if (resource == Resource.ASSIGNMENTS) {
            row.put("is_current", temporal.isCurrentAssignment(source));
        }
        List<String> nullable = switch (resource) {
            case USERS -> List.of("retired_on", "last_synced_at");
            case ASSIGNMENTS, RELATIONS -> List.of("valid_from", "valid_to");
            case PERMISSIONS -> List.of("role_id", "organization_code", "user_id");
            default -> List.of();
        };
        enforceRequired(row, List.copyOf(row.keySet()), nullable);
        return row;
    }

    private Map<String, Object> organization(Map<String, Object> source,
            Map<String, Object> relation, PolicyData data) {
        Map<String, Object> row = new LinkedHashMap<>(source);
        row.put("parent_code", relation.get("parent_code"));
        row.put("valid_from", wire(relation.get("valid_from")));
        row.put("valid_to", wire(relation.get("valid_to")));
        row.put("child_codes", data.rows(Resource.RELATIONS).stream()
                .filter(child -> Objects.equals(source.get("organization_code"), child.get("parent_code")))
                .filter(temporal::isCurrentRelation).map(child -> child.get("organization_code")).distinct().toList());
        enforceRequired(row, List.of("organization_code", "organization_type", "parent_code", "child_codes",
                "valid_from", "valid_to"), List.of("valid_from", "valid_to"));
        return row;
    }

    private static void enforceRequired(Map<String, Object> row, List<String> required, List<String> nullable) {
        for (String field : required) {
            if (row.get(field) == null && !nullable.contains(field)) {
                throw new ApiFailure(400, "DATA_CONTRACT_ERROR",
                        "Persisted required field is null; source remediation or approved root policy is required",
                        field);
            }
        }
    }

    private static Object wire(Object value) {
        if (value instanceof Timestamp timestamp) {
            return timestamp.toInstant().toString();
        }
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate().toString();
        }
        return value;
    }

    private static Map<String, Object> existing(Resource resource, Map<String, Object> row, PolicyData data) {
        return data.rows(resource).stream().filter(candidate -> resource.keys().stream()
                .allMatch(key -> Objects.equals(row.get(key), candidate.get(key)))).findFirst().orElse(null);
    }

    private static Map<String, Object> find(Resource resource, String key, Object value, PolicyData data) {
        return data.rows(resource).stream().filter(row -> Objects.equals(row.get(key), value)).findFirst().orElse(null);
    }

    private static void validateReferences(Resource resource, Map<String, Object> row, PolicyData data) {
        switch (resource) {
            case ASSIGNMENTS -> {
                reference(Resource.USERS, "user_id", row.get("user_id"), data);
                reference(Resource.USERS, "user_id", row.get("approver_id"), data, "approver_id");
                reference(Resource.ROLES, "role_id", row.get("role_id"), data);
            }
            case PERMISSIONS -> {
                reference(Resource.MENUS, "menu_id", row.get("menu_id"), data);
                optionalReference(Resource.USERS, "user_id", row, data);
                optionalReference(Resource.ORGANIZATIONS, "organization_code", row, data);
                optionalReference(Resource.ROLES, "role_id", row, data);
            }
            case MENUS -> reference(Resource.MENUS, "menu_id", row.get("parent_menu_id"), data, "parent_menu_id");
            case EXECUTION -> reference(Resource.MENUS, "menu_id", row.get("menu_id"), data);
            case RELATIONS -> {
                reference(Resource.ORGANIZATIONS, "organization_code", row.get("organization_code"), data);
                reference(Resource.ORGANIZATIONS, "organization_code", row.get("parent_code"), data, "parent_code");
            }
            case DETAILS -> {
                reference(Resource.GROUPS, "group_id", row.get("group_id"), data);
                if (row.containsKey("parent_code")) {
                    boolean found = data.rows(Resource.DETAILS).stream()
                            .anyMatch(code -> Objects.equals(row.get("group_id"), code.get("group_id"))
                                    && Objects.equals(row.get("parent_code"), code.get("code")));
                    if (!found) {
                        throw ApiFailure.invalid("parent_code", "Parent code must exist in the selected group");
                    }
                }
            }
            default -> {
                // Definition changes have no reference-side mutation.
            }
        }
    }

    private static void optionalReference(Resource resource, String field, Map<String, Object> row, PolicyData data) {
        if (row.get(field) != null) {
            reference(resource, field, row.get(field), data);
        }
    }

    private static void reference(Resource resource, String key, Object value, PolicyData data) {
        reference(resource, key, value, data, key);
    }

    private static void reference(Resource resource, String key, Object value, PolicyData data, String field) {
        if (find(resource, key, value, data) == null) {
            throw ApiFailure.invalid(field, "Referenced identifier does not exist");
        }
    }

    private static Map<String, Object> filters(Resource resource, Map<String, String> query) {
        List<String> allowed = switch (resource) {
            case USERS -> List.of("employee_number", "person_name", "organization_code", "job_grade",
                    "employment_status", "enabled");
            case ORGANIZATIONS, RELATIONS -> List.of("organization_code");
            case ROLES -> List.of("role_id");
            case ASSIGNMENTS -> List.of("user_id");
            case PERMISSIONS -> List.of("role_id", "organization_code", "user_id", "menu_id");
            case MENUS -> List.of("parent_menu_id");
            case EXECUTION -> List.of("menu_id");
            case GROUPS, DETAILS -> List.of("group_id");
        };
        Map<String, Object> filters = new LinkedHashMap<>();
        for (String field : allowed) {
            if (query.containsKey(field)) {
                String value = query.get(field);
                if (field.equals("enabled")) {
                    if (!value.equals("true") && !value.equals("false")) {
                        throw ApiFailure.invalid(field, "Expected true or false");
                    }
                    filters.put(field, Boolean.valueOf(value));
                } else {
                    filters.put(field, value);
                }
            }
        }
        return filters;
    }

    private static int positive(String value, String field) {
        try {
            int number = Integer.parseInt(value);
            if (number > 0) {
                return number;
            }
        } catch (NumberFormatException failure) {
            // The same contract error applies to missing, malformed and out-of-range pagination values.
        }
        throw ApiFailure.invalid(field, "A positive integer is required");
    }

    private static void requireQuery(Map<String, String> query, String field) {
        if (!query.containsKey(field)) {
            throw ApiFailure.invalid(field, "Required query parameter is missing");
        }
    }
}
