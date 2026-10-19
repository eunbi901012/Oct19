package org.example.oct19;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.example.oct19.api.ApiFailure;
import org.example.oct19.api.ScreenAccessController;
import org.example.oct19.mapper.CatalogMapper;
import org.example.oct19.model.Resource;
import org.example.oct19.security.AccessPolicy;
import org.example.oct19.security.AuthenticationAdapter;
import org.example.oct19.security.Identity;
import org.example.oct19.service.ScreenAccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;

/** Direct UI URLs use the same operation and effective-menu policies as API/navigation, never just client hiding. */
@WebMvcTest(ScreenAccessController.class)
@Import(ScreenAccessService.class)
class ScreenAccessContractTest {
    @Autowired
    MockMvc mvc;
    @MockBean
    CatalogMapper mapper;
    @MockBean
    AuthenticationAdapter authentication;
    @MockBean
    AccessPolicy access;

    @BeforeEach
    void persistedMenuFixture() {
        when(authentication.authenticate(any())).thenReturn(Optional.of(new Identity("screen-reader")));
        for (Resource resource : Resource.values()) {
            when(mapper.select(eq(resource), any())).thenReturn(List.of());
        }
        when(mapper.select(eq(Resource.EXECUTION), any())).thenReturn(List.of(Map.of(
                "menu_id", "users-menu", "screen_id", "SCR-USERS", "url", "/admin/users")));
        when(mapper.select(eq(Resource.MENUS), any())).thenReturn(List.of(Map.of(
                "menu_id", "users-menu", "parent_menu_id", "admin-root", "level", 2, "display_order", 1)));
    }

    @Test
    void directUrlWithoutVerifiedIdentityIs401BeforeDatabaseAccess() throws Exception {
        when(authentication.authenticate(any())).thenReturn(Optional.empty());
        mvc.perform(get("/admin/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
        verifyNoInteractions(mapper, access);
    }

    @Test
    void directUrlRequiresManagementScopeAsWellAsMenuPermission() throws Exception {
        doThrow(new ApiFailure(403, "FORBIDDEN", "Denied operation", null))
                .when(access).requireOperation(any(), eq("searchUsers"), any());
        mvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        verify(mapper, never()).save(any(), any());
    }

    @Test
    void directUrlWithDeniedEffectiveMenuIs403AndDoesNotWrite() throws Exception {
        when(access.mayNavigate(any(), eq("users-menu"), any())).thenReturn(false);
        mvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("MENU_FORBIDDEN"));
        verify(access).requireOperation(any(), eq("searchUsers"), any());
        verify(access).mayNavigate(any(), eq("users-menu"), any());
        verify(mapper, never()).save(any(), any());
    }

    @Test
    void directUrlWithApprovedScopeAndPersistedAllowedMenuAuthorizesShell() throws Exception {
        when(access.mayNavigate(any(), eq("users-menu"), any())).thenReturn(true);
        mvc.perform(get("/admin/users").queryParam("user_id", "selected-user"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(access).requireOperation(any(), eq("searchUsers"), any());
        verify(access).mayNavigate(any(), eq("users-menu"), any());
        verify(mapper, never()).save(any(), any());
    }

    @Test
    void screenOperationsExistInClasspathContractFixture() throws Exception {
        String contract;
        try (var input = new ClassPathResource("contracts/openapi.yaml").getInputStream()) {
            contract = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        assertTrue(contract.contains("openapi: 3.0.3"));
        for (String operation : List.of("searchUsers", "getOrganizationRelations", "listRoles",
                "listUserRoleAssignments", "listMenuPermissions", "getMenuStructure",
                "listMenuExecutionInfo", "listCodeGroups", "listDetailCodes")) {
            assertTrue(contract.lines().anyMatch(line -> line.strip().equals("operationId: " + operation)),
                    () -> "Missing screen operation in contracts/openapi.yaml: " + operation);
        }
        verifyNoInteractions(mapper, access);
    }

    @Test
    void organizationsWithApprovedScopeAndPersistedAllowedMenuAuthorizesShell() throws Exception {
        persistedScreen("/admin/organizations", "organizations-menu", true);
        mvc.perform(get("/admin/organizations"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(access).requireOperation(any(), eq("getOrganizationRelations"), any());
        verify(access).mayNavigate(any(), eq("organizations-menu"), any());
        verify(mapper, never()).save(any(), any());
    }

    @Test
    void organizationsWithDeniedEffectiveMenuIs403AndDoesNotWrite() throws Exception {
        persistedScreen("/admin/organizations", "organizations-menu", false);
        mvc.perform(get("/admin/organizations"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("MENU_FORBIDDEN"));
        verify(access).requireOperation(any(), eq("getOrganizationRelations"), any());
        verify(access).mayNavigate(any(), eq("organizations-menu"), any());
        verify(mapper, never()).save(any(), any());
    }

    @Test
    void rolesWithApprovedScopeAndPersistedAllowedMenuAuthorizesShell() throws Exception {
        persistedScreen("/admin/roles", "roles-menu", true);
        mvc.perform(get("/admin/roles"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(access).requireOperation(any(), eq("listRoles"), any());
        verify(access).mayNavigate(any(), eq("roles-menu"), any());
        verify(mapper, never()).save(any(), any());
    }

    @Test
    void rolesWithDeniedEffectiveMenuIs403AndDoesNotWrite() throws Exception {
        persistedScreen("/admin/roles", "roles-menu", false);
        mvc.perform(get("/admin/roles"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("MENU_FORBIDDEN"));
        verify(access).requireOperation(any(), eq("listRoles"), any());
        verify(access).mayNavigate(any(), eq("roles-menu"), any());
        verify(mapper, never()).save(any(), any());
    }

    @Test
    void userRolesWithApprovedScopeAndPersistedAllowedMenuAuthorizesShell() throws Exception {
        persistedScreen("/admin/user-roles", "user-roles-menu", true);
        mvc.perform(get("/admin/user-roles"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(access).requireOperation(any(), eq("listUserRoleAssignments"), any());
        verify(access).mayNavigate(any(), eq("user-roles-menu"), any());
        verify(mapper, never()).save(any(), any());
    }

    @Test
    void userRolesWithDeniedEffectiveMenuIs403AndDoesNotWrite() throws Exception {
        persistedScreen("/admin/user-roles", "user-roles-menu", false);
        mvc.perform(get("/admin/user-roles"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("MENU_FORBIDDEN"));
        verify(access).requireOperation(any(), eq("listUserRoleAssignments"), any());
        verify(access).mayNavigate(any(), eq("user-roles-menu"), any());
        verify(mapper, never()).save(any(), any());
    }

    @Test
    void menuPermissionsWithApprovedScopeAndPersistedAllowedMenuAuthorizesShell() throws Exception {
        persistedScreen("/admin/menu-permissions", "menu-permissions-menu", true);
        mvc.perform(get("/admin/menu-permissions"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(access).requireOperation(any(), eq("listMenuPermissions"), any());
        verify(access).mayNavigate(any(), eq("menu-permissions-menu"), any());
        verify(mapper, never()).save(any(), any());
    }

    @Test
    void menuPermissionsWithDeniedEffectiveMenuIs403AndDoesNotWrite() throws Exception {
        persistedScreen("/admin/menu-permissions", "menu-permissions-menu", false);
        mvc.perform(get("/admin/menu-permissions"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("MENU_FORBIDDEN"));
        verify(access).requireOperation(any(), eq("listMenuPermissions"), any());
        verify(access).mayNavigate(any(), eq("menu-permissions-menu"), any());
        verify(mapper, never()).save(any(), any());
    }

    @Test
    void menuStructureWithApprovedScopeAndPersistedAllowedMenuAuthorizesShell() throws Exception {
        persistedScreen("/admin/menu-structure", "menu-structure-menu", true);
        mvc.perform(get("/admin/menu-structure"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(access).requireOperation(any(), eq("getMenuStructure"), any());
        verify(access).mayNavigate(any(), eq("menu-structure-menu"), any());
        verify(mapper, never()).save(any(), any());
    }

    @Test
    void menuStructureWithDeniedEffectiveMenuIs403AndDoesNotWrite() throws Exception {
        persistedScreen("/admin/menu-structure", "menu-structure-menu", false);
        mvc.perform(get("/admin/menu-structure"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("MENU_FORBIDDEN"));
        verify(access).requireOperation(any(), eq("getMenuStructure"), any());
        verify(access).mayNavigate(any(), eq("menu-structure-menu"), any());
        verify(mapper, never()).save(any(), any());
    }

    @Test
    void menuInfoWithApprovedScopeAndPersistedAllowedMenuAuthorizesShell() throws Exception {
        persistedScreen("/admin/menu-info", "menu-info-menu", true);
        mvc.perform(get("/admin/menu-info"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(access).requireOperation(any(), eq("listMenuExecutionInfo"), any());
        verify(access).mayNavigate(any(), eq("menu-info-menu"), any());
        verify(mapper, never()).save(any(), any());
    }

    @Test
    void menuInfoWithDeniedEffectiveMenuIs403AndDoesNotWrite() throws Exception {
        persistedScreen("/admin/menu-info", "menu-info-menu", false);
        mvc.perform(get("/admin/menu-info"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("MENU_FORBIDDEN"));
        verify(access).requireOperation(any(), eq("listMenuExecutionInfo"), any());
        verify(access).mayNavigate(any(), eq("menu-info-menu"), any());
        verify(mapper, never()).save(any(), any());
    }

    @Test
    void codeGroupsWithApprovedScopeAndPersistedAllowedMenuAuthorizesShell() throws Exception {
        persistedScreen("/admin/code-groups", "code-groups-menu", true);
        mvc.perform(get("/admin/code-groups"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(access).requireOperation(any(), eq("listCodeGroups"), any());
        verify(access).mayNavigate(any(), eq("code-groups-menu"), any());
        verify(mapper, never()).save(any(), any());
    }

    @Test
    void codeGroupsWithDeniedEffectiveMenuIs403AndDoesNotWrite() throws Exception {
        persistedScreen("/admin/code-groups", "code-groups-menu", false);
        mvc.perform(get("/admin/code-groups"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("MENU_FORBIDDEN"));
        verify(access).requireOperation(any(), eq("listCodeGroups"), any());
        verify(access).mayNavigate(any(), eq("code-groups-menu"), any());
        verify(mapper, never()).save(any(), any());
    }

    @Test
    void detailCodesWithApprovedScopeAndPersistedAllowedMenuAuthorizesShell() throws Exception {
        persistedScreen("/admin/detail-codes", "detail-codes-menu", true);
        mvc.perform(get("/admin/detail-codes"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(access).requireOperation(any(), eq("listDetailCodes"), any());
        verify(access).mayNavigate(any(), eq("detail-codes-menu"), any());
        verify(mapper, never()).save(any(), any());
    }

    @Test
    void detailCodesWithDeniedEffectiveMenuIs403AndDoesNotWrite() throws Exception {
        persistedScreen("/admin/detail-codes", "detail-codes-menu", false);
        mvc.perform(get("/admin/detail-codes"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("MENU_FORBIDDEN"));
        verify(access).requireOperation(any(), eq("listDetailCodes"), any());
        verify(access).mayNavigate(any(), eq("detail-codes-menu"), any());
        verify(mapper, never()).save(any(), any());
    }

    private void persistedScreen(String path, String menuId, boolean allowed) {
        when(mapper.select(eq(Resource.EXECUTION), any())).thenReturn(List.of(Map.of(
                "menu_id", menuId, "screen_id", "SCR-" + menuId, "url", path)));
        when(mapper.select(eq(Resource.MENUS), any())).thenReturn(List.of(Map.of(
                "menu_id", menuId, "parent_menu_id", "admin-root", "level", 2, "display_order", 1)));
        when(access.mayNavigate(any(), eq(menuId), any())).thenReturn(allowed);
    }

    @Test
    void executionQueryParametersDoNotChangeTheProtectedScreenPath() throws Exception {
        when(mapper.select(eq(Resource.EXECUTION), any())).thenReturn(List.of(Map.of(
                "menu_id", "users-menu", "screen_id", "SCR-USERS", "url", "/admin/users?view=current")));
        when(access.mayNavigate(any(), eq("users-menu"), any())).thenReturn(true);
        mvc.perform(get("/admin/users").queryParam("view", "current"))
                .andExpect(status().isNoContent());
        verify(mapper, never()).save(any(), any());
    }

    @Test
    void anotherScreenCannotReuseAnUnrelatedAllowedMenu() throws Exception {
        when(access.mayNavigate(any(), eq("users-menu"), any())).thenReturn(true);
        mvc.perform(get("/admin/roles"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("MENU_FORBIDDEN"));
        verify(access).requireOperation(any(), eq("listRoles"), any());
        verify(mapper, never()).save(any(), any());
    }
}
