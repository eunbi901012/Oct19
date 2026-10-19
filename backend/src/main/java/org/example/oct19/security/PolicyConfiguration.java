package org.example.oct19.security;

import java.util.Map;
import java.util.Optional;
import org.example.oct19.api.ApiFailure;
import org.example.oct19.model.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Safe production fallbacks, replaced only by institution-approved integration beans. */
@Configuration
public class PolicyConfiguration {
    /** Does not treat arbitrary tokens, request headers or account names as proof of identity. */
    @Bean
    @ConditionalOnMissingBean(AuthenticationAdapter.class)
    public AuthenticationAdapter authenticationAdapter() {
        return request -> Optional.empty();
    }

    /** Fails closed until both operation scopes and effective-menu combination policy are approved. */
    @Bean
    @ConditionalOnMissingBean(AccessPolicy.class)
    public AccessPolicy accessPolicy() {
        return new AccessPolicy() {
            /** {@inheritDoc} */
            @Override
            public void requireOperation(Identity identity, String operationId, PolicyData data) {
                throw ApiFailure.pending("OQ-AUTH-001/OQ-AUTH-002 (" + operationId + ")");
            }

            /** {@inheritDoc} */
            @Override
            public boolean mayNavigate(Identity identity, String menuId, PolicyData data) {
                throw ApiFailure.pending("OQ-AUTH-001/OQ-UI-003 effective menu availability");
            }
        };
    }

    /** Leaves current-role and organization-current calculations explicitly approval-dependent. */
    @Bean
    @ConditionalOnMissingBean(TemporalPolicy.class)
    public TemporalPolicy temporalPolicy() {
        return new TemporalPolicy() {
            /** {@inheritDoc} */
            @Override
            public boolean isCurrentAssignment(Map<String, Object> assignment) {
                throw ApiFailure.pending("OQ-ORG-001/OQ-ROLE-001 current role boundaries");
            }

            /** {@inheritDoc} */
            @Override
            public boolean isCurrentRelation(Map<String, Object> relation) {
                throw ApiFailure.pending("OQ-ORG-001 current organization boundaries");
            }
        };
    }

    /** Gates unapproved institutional branches only; independent definition saves remain functional. */
    @Bean
    @ConditionalOnMissingBean(BusinessPolicy.class)
    public BusinessPolicy businessPolicy() {
        return (identity, resource, proposed, data) -> {
            if (resource == Resource.RELATIONS) {
                throw ApiFailure.pending("OQ-ORG-001 ownership, overlap and cycle policy");
            }
            if (resource == Resource.ASSIGNMENTS) {
                throw ApiFailure.pending("OQ-ROLE-001 approver validation and revocation policy");
            }
            if (resource == Resource.PERMISSIONS) {
                throw ApiFailure.pending("OQ-AUTH-001 selected-target combination policy");
            }
            if (resource == Resource.MENUS) {
                boolean knownParent = data.rows(Resource.MENUS).stream()
                        .anyMatch(row -> proposed.get("parent_menu_id").equals(row.get("menu_id")));
                if (!knownParent) {
                    throw ApiFailure.pending("OQ-UI-001 root-menu representation");
                }
            }
            if (resource == Resource.DETAILS && !proposed.containsKey("parent_code")) {
                boolean exists = data.rows(Resource.DETAILS).stream()
                        .anyMatch(row -> proposed.get("group_id").equals(row.get("group_id"))
                                && proposed.get("code").equals(row.get("code")));
                if (!exists) {
                    throw ApiFailure.pending("OQ-UI-002 root-code representation (response requires parent_code)");
                }
            }
        };
    }
}
