package org.example.oct19.security;

import java.util.List;
import java.util.Map;
import org.example.oct19.model.Resource;

/** Database snapshot supplied to approved policies; policies must not infer roles from account names. */
public record PolicyData(Map<Resource, List<Map<String, Object>>> tables) {
    /** Returns persisted rows for the requested resource. */
    public List<Map<String, Object>> rows(Resource resource) {
        return tables.get(resource);
    }
}
