package org.example.oct19.security;

/** Approved operation-scope AND effective menu authorization, shared with navigation. */
public interface AccessPolicy {
    /** Requires both the approved operation role/scope and its effective menu authorization. */
    void requireOperation(Identity identity, String operationId, PolicyData data);

    /** Decides effective menu availability using the same combination and temporal rules as operation authorization. */
    boolean mayNavigate(Identity identity, String menuId, PolicyData data);
}
