package org.example.oct19.api;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.example.oct19.model.Resource;
import org.example.oct19.service.CatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Owning HTTP transport for the twenty business operations; policy and persistence live in the application service. */
@RestController
public class ManagementController {
    private final CatalogService service;

    /** Wires the management application service without adding transport-level business policies. */
    public ManagementController(CatalogService service) {
        this.service = service;
    }

    /** Searches synchronized personnel and current-role projections by the named contract conditions. */
    @GetMapping("/api/users")
    public Map<String, Object> searchUsers(@RequestParam Map<String, String> query, HttpServletRequest request) {
        return service.read("searchUsers", Resource.USERS, query, request);
    }

    /** Changes only a synchronized user's local enabled setting. */
    @PutMapping("/api/users")
    public Map<String, Object> updateUserSettings(@RequestBody JsonNode body, HttpServletRequest request) {
        return service.save("updateUserSettings", Resource.USERS, body, request);
    }

    /** Returns organization types and approved current parent/child period projections. */
    @GetMapping("/api/organizations")
    public Map<String, Object> getOrganizationRelations(@RequestParam Map<String, String> query,
            HttpServletRequest request) {
        return service.read("getOrganizationRelations", Resource.ORGANIZATIONS, query, request);
    }

    /** Appends an organization relation while retaining every earlier relation and period. */
    @PutMapping("/api/organizations")
    public Map<String, Object> saveOrganizationRelation(@RequestBody JsonNode body, HttpServletRequest request) {
        return service.save("saveOrganizationRelation", Resource.RELATIONS, body, request);
    }

    /** Reads distinct role definitions independently of assignment records. */
    @GetMapping("/api/roles")
    public Map<String, Object> listRoles(@RequestParam Map<String, String> query, HttpServletRequest request) {
        return service.read("listRoles", Resource.ROLES, query, request);
    }

    /** Saves purpose, grant criteria and default scope without modifying assignments. */
    @PutMapping("/api/roles")
    public Map<String, Object> saveRole(@RequestBody JsonNode body, HttpServletRequest request) {
        return service.save("saveRole", Resource.ROLES, body, request);
    }

    /** Reads period-bearing assignments, original sources, recorded actions and approved current status. */
    @GetMapping("/api/user-role-assignments")
    public Map<String, Object> listUserRoleAssignments(@RequestParam Map<String, String> query,
            HttpServletRequest request) {
        return service.read("listUserRoleAssignments", Resource.ASSIGNMENTS, query, request);
    }

    /** Saves grant/change/revoke records with explicitly supplied approvers and preserved assignment origins. */
    @PutMapping("/api/user-role-assignments")
    public Map<String, Object> saveUserRoleAssignment(@RequestBody JsonNode body, HttpServletRequest request) {
        return service.save("saveUserRoleAssignment", Resource.ASSIGNMENTS, body, request);
    }

    /** Reads persisted target/menu permission settings, not invented effective-permission defaults. */
    @GetMapping("/api/menu-permissions")
    public Map<String, Object> listMenuPermissions(@RequestParam Map<String, String> query,
            HttpServletRequest request) {
        return service.read("listMenuPermissions", Resource.PERMISSIONS, query, request);
    }

    /** Saves the selected targets and allowed value under approved target-combination policy. */
    @PutMapping("/api/menu-permissions")
    public Map<String, Object> saveMenuPermission(@RequestBody JsonNode body, HttpServletRequest request) {
        return service.save("saveMenuPermission", Resource.PERMISSIONS, body, request);
    }

    /** Reads parent-specific hierarchy and persisted display order. */
    @GetMapping("/api/menu-structure")
    public Map<String, Object> getMenuStructure(@RequestParam Map<String, String> query, HttpServletRequest request) {
        return service.read("getMenuStructure", Resource.MENUS, query, request);
    }

    /** Updates one hierarchy row without resequencing other parents' menus. */
    @PutMapping("/api/menu-structure")
    public Map<String, Object> saveMenuStructure(@RequestBody JsonNode body, HttpServletRequest request) {
        return service.save("saveMenuStructure", Resource.MENUS, body, request);
    }

    /** Reads saved screen identifiers and URLs. */
    @GetMapping("/api/menu-execution-info")
    public Map<String, Object> listMenuExecutionInfo(@RequestParam Map<String, String> query,
            HttpServletRequest request) {
        return service.read("listMenuExecutionInfo", Resource.EXECUTION, query, request);
    }

    /** Saves screen identifiers and URLs without changing effective-permission settings. */
    @PutMapping("/api/menu-execution-info")
    public Map<String, Object> saveMenuExecutionInfo(@RequestBody JsonNode body, HttpServletRequest request) {
        return service.save("saveMenuExecutionInfo", Resource.EXECUTION, body, request);
    }

    /** Reads code groups independently by group identifier. */
    @GetMapping("/api/code-groups")
    public Map<String, Object> listCodeGroups(@RequestParam Map<String, String> query, HttpServletRequest request) {
        return service.read("listCodeGroups", Resource.GROUPS, query, request);
    }

    /** Saves a group definition without modifying its detail codes. */
    @PutMapping("/api/code-groups")
    public Map<String, Object> saveCodeGroup(@RequestBody JsonNode body, HttpServletRequest request) {
        return service.save("saveCodeGroup", Resource.GROUPS, body, request);
    }

    /** Reads only the required selected group's detail codes in persisted sort order. */
    @GetMapping("/api/detail-codes")
    public Map<String, Object> listDetailCodes(@RequestParam Map<String, String> query, HttpServletRequest request) {
        return service.read("listDetailCodes", Resource.DETAILS, query, request);
    }

    /** Saves a detail code and preserves its parent when the optional property is omitted. */
    @PutMapping("/api/detail-codes")
    public Map<String, Object> saveDetailCode(@RequestBody JsonNode body, HttpServletRequest request) {
        return service.save("saveDetailCode", Resource.DETAILS, body, request);
    }

    /** Reads retained organization relations and periods for the required organization identifier. */
    @GetMapping("/api/organization-relation-history")
    public Map<String, Object> listOrganizationRelationHistory(@RequestParam Map<String, String> query,
            HttpServletRequest request) {
        return service.read("listOrganizationRelationHistory", Resource.RELATIONS, query, request);
    }

    /** Returns the approved current subject's available navigation, separate from management-edit scope. */
    @GetMapping("/api/accessible-menus")
    public Map<String, Object> getAccessibleMenus(HttpServletRequest request) {
        return service.accessible(request);
    }
}
