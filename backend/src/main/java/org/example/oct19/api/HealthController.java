package org.example.oct19.api;

import java.util.Map;
import org.example.oct19.mapper.CatalogMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public readiness handler; it never reports UP based only on process existence. */
@RestController
public class HealthController {
    private final CatalogMapper mapper;

    /** Wires the same PostgreSQL/MyBatis boundary used by business operations. */
    public HealthController(CatalogMapper mapper) {
        this.mapper = mapper;
    }

    /** Returns contract health only after a successful selected-database query. */
    @GetMapping("/api/health")
    public Map<String, Object> getHealth() {
        if (mapper.ping() != 1) {
            throw new ApiFailure(400, "DATABASE_NOT_READY", "Database readiness check failed", null);
        }
        return Envelope.success(Map.of("status", "UP"));
    }
}
