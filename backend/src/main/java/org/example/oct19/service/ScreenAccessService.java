package org.example.oct19.service;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.example.oct19.api.ApiFailure;
import org.example.oct19.mapper.CatalogMapper;
import org.example.oct19.model.Resource;
import org.example.oct19.security.AccessPolicy;
import org.example.oct19.security.AuthenticationAdapter;
import org.example.oct19.security.PolicyData;
import org.springframework.stereotype.Service;

/** Authorizes direct screen URLs before nginx serves the SPA, reusing operation and effective-menu policies. */
@Service
public class ScreenAccessService {
    private static final Map<String, String> OPERATIONS = Map.ofEntries(
            Map.entry("/admin/users", "searchUsers"),
            Map.entry("/admin/organizations", "getOrganizationRelations"),
            Map.entry("/admin/roles", "listRoles"),
            Map.entry("/admin/user-roles", "listUserRoleAssignments"),
            Map.entry("/admin/menu-permissions", "listMenuPermissions"),
            Map.entry("/admin/menu-structure", "getMenuStructure"),
            Map.entry("/admin/menu-info", "listMenuExecutionInfo"),
            Map.entry("/admin/code-groups", "listCodeGroups"),
            Map.entry("/admin/detail-codes", "listDetailCodes"));
    private final CatalogMapper mapper;
    private final AuthenticationAdapter authentication;
    private final AccessPolicy access;

    /** Uses the same approved adapters and real mapper as management operations; no bypass credential is accepted. */
    public ScreenAccessService(CatalogMapper mapper, AuthenticationAdapter authentication, AccessPolicy access) {
        this.mapper = mapper;
        this.authentication = authentication;
        this.access = access;
    }

    /** Requires the screen's operation scope and unique persisted menu's effective permission, without writes. */
    public void requireScreen(HttpServletRequest request) {
        var identity = authentication.authenticate(request).orElseThrow(() -> new ApiFailure(
                401, "AUTHENTICATION_REQUIRED", "Valid institution-approved authentication is required", null));
        String path = request.getRequestURI();
        String operation = OPERATIONS.get(path);
        if (operation == null) {
            throw new ApiFailure(403, "MENU_FORBIDDEN", "Screen is not an authorized management route", null);
        }
        Map<Resource, List<Map<String, Object>>> tables = new EnumMap<>(Resource.class);
        for (Resource resource : Resource.values()) {
            tables.put(resource, mapper.select(resource, Map.of()).stream()
                    .map(row -> Collections.unmodifiableMap(new LinkedHashMap<>(row))).toList());
        }
        PolicyData data = new PolicyData(Collections.unmodifiableMap(tables));
        access.requireOperation(identity, operation, data);
        var executions = data.rows(Resource.EXECUTION).stream()
                .filter(row -> matchesPath(path, row.get("url"))).toList();
        if (executions.size() > 1) {
            throw ApiFailure.pending("OQ-AUTH-001 ambiguous screen-to-menu mapping");
        }
        if (executions.isEmpty()) {
            throw new ApiFailure(403, "MENU_FORBIDDEN", "No authorized menu is linked to this screen", null);
        }
        String menuId = (String) executions.get(0).get("menu_id");
        boolean persistedMenu = data.rows(Resource.MENUS).stream()
                .anyMatch(row -> Objects.equals(menuId, row.get("menu_id")));
        if (!persistedMenu || !access.mayNavigate(identity, menuId, data)) {
            throw new ApiFailure(403, "MENU_FORBIDDEN", "Effective menu permission denies this screen", null);
        }
    }

    private static boolean matchesPath(String path, Object value) {
        if (!(value instanceof String url)) {
            return false;
        }
        try {
            URI uri = URI.create(url);
            // Query/fragment identify a view, not a new screen. External/authority URLs do not authorize local paths.
            return !uri.isAbsolute() && uri.getRawAuthority() == null && path.equals(uri.getRawPath());
        } catch (IllegalArgumentException invalidUrl) {
            return false;
        }
    }
}
