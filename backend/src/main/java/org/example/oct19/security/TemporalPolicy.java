package org.example.oct19.security;

import java.util.Map;

/** Approved period-boundary and revocation-effect policy; no timezone or inclusive-boundary default is guessed. */
public interface TemporalPolicy {
    /** Computes current assignment status, excluding already-ended roles under the approved clock policy. */
    boolean isCurrentAssignment(Map<String, Object> assignment);

    /** Computes whether an organization relation is current under the approved period policy. */
    boolean isCurrentRelation(Map<String, Object> relation);
}
