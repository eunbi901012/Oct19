package org.example.oct19.security;

import java.util.Map;
import org.example.oct19.model.Resource;

/** Approval boundary for unresolved organization, approver, permission-target and hierarchy policies only. */
public interface BusinessPolicy {
    /** Validates ownership, temporal/hierarchy decisions or approver identity without substituting the operator. */
    void approve(Identity identity, Resource resource, Map<String, Object> proposed, PolicyData data);
}
