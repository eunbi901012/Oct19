package org.example.oct19.api;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Exact top-level response envelopes with non-business request tracking metadata. */
public final class Envelope {
    private Envelope() {
    }

    /** Wraps business data without adding undocumented business properties. */
    public static Map<String, Object> success(Object data) {
        Map<String, Object> response = base(true);
        response.put("data", data);
        return response;
    }

    /** Creates the shared success/error envelope metadata. */
    public static Map<String, Object> base(boolean success) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", success);
        response.put("meta", Map.of("request_id", UUID.randomUUID().toString()));
        return response;
    }
}
