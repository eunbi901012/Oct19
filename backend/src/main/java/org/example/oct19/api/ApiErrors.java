package org.example.oct19.api;

import java.util.List;
import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Sanitized contract error mapping; SQL and credentials never enter the HTTP response. */
@RestControllerAdvice
public class ApiErrors {
    /** Serializes expected failures, retaining exact rejected wire fields. */
    @ExceptionHandler(ApiFailure.class)
    public ResponseEntity<Map<String, Object>> expected(ApiFailure failure) {
        Map<String, Object> response = Envelope.base(false);
        response.put("code", failure.code());
        response.put("message", failure.getMessage());
        if (failure.field() != null) {
            response.put("field_errors", List.of(Map.of("field", failure.field(), "message", failure.getMessage())));
        }
        return ResponseEntity.status(failure.status()).body(response);
    }

    /** Rejects syntactically malformed JSON instead of silently coercing its content. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> malformed(HttpMessageNotReadableException failure) {
        return expected(ApiFailure.invalid("body", "A valid JSON object is required"));
    }

    /** Reports constraint failures in the declared validation family without exposing SQL. */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, Object>> database(DataAccessException failure) {
        return expected(new ApiFailure(400, "PERSISTENCE_ERROR",
                "Database operation failed; verify database readiness and referenced identifiers", null));
    }
}
