package org.example.oct19.api;

import jakarta.servlet.http.HttpServletRequest;
import org.example.oct19.service.ScreenAccessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Authorizes the nine existing UI URLs for nginx auth_request; it does not introduce a public login or bypass API. */
@RestController
public class ScreenAccessController {
    private final ScreenAccessService service;

    /** Connects UI route authorization to the same policy and persistence boundaries used by business APIs. */
    public ScreenAccessController(ScreenAccessService service) {
        this.service = service;
    }

    /** Protects the user screen using its persisted execution entry and approved operation scope. */
    @GetMapping("/admin/users")
    public ResponseEntity<Void> authorizeUsers(HttpServletRequest request) {
        return authorizeScreen(request);
    }

    /** Protects the organization screen without exposing protected business data. */
    @GetMapping("/admin/organizations")
    public ResponseEntity<Void> authorizeOrganizations(HttpServletRequest request) {
        return authorizeScreen(request);
    }

    /** Protects role definitions independently of role assignment editing. */
    @GetMapping("/admin/roles")
    public ResponseEntity<Void> authorizeRoles(HttpServletRequest request) {
        return authorizeScreen(request);
    }

    /** Protects the user-role assignment screen through the shared server policy. */
    @GetMapping("/admin/user-roles")
    public ResponseEntity<Void> authorizeUserRoles(HttpServletRequest request) {
        return authorizeScreen(request);
    }

    /** Protects permission administration without introducing permission precedence rules. */
    @GetMapping("/admin/menu-permissions")
    public ResponseEntity<Void> authorizeMenuPermissions(HttpServletRequest request) {
        return authorizeScreen(request);
    }

    /** Protects hierarchy administration with its owning operation scope. */
    @GetMapping("/admin/menu-structure")
    public ResponseEntity<Void> authorizeMenuStructure(HttpServletRequest request) {
        return authorizeScreen(request);
    }

    /** Protects execution information administration with its persisted menu permission. */
    @GetMapping("/admin/menu-info")
    public ResponseEntity<Void> authorizeMenuInfo(HttpServletRequest request) {
        return authorizeScreen(request);
    }

    /** Protects code group administration through the existing policy service. */
    @GetMapping("/admin/code-groups")
    public ResponseEntity<Void> authorizeCodeGroups(HttpServletRequest request) {
        return authorizeScreen(request);
    }

    /** Protects detail code administration without reusing another screen's authorization. */
    @GetMapping("/admin/detail-codes")
    public ResponseEntity<Void> authorizeDetailCodes(HttpServletRequest request) {
        return authorizeScreen(request);
    }

    /** Retains the existing Java entry point; only the literal handlers above register HTTP routes. */
    public ResponseEntity<Void> authorizeScreen(HttpServletRequest request) {
        service.requireScreen(request);
        return ResponseEntity.noContent().build();
    }
}
