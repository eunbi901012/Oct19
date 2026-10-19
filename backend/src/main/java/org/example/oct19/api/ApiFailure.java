package org.example.oct19.api;

/** Expected contract failure with an HTTP status and an optional exact input-field location. */
public class ApiFailure extends RuntimeException {
    private final int status;
    private final String code;
    private final String field;

    /** Creates an expected failure; messages must never contain credentials or SQL internals. */
    public ApiFailure(int status, String code, String message, String field) {
        super(message);
        this.status = status;
        this.code = code;
        this.field = field;
    }

    /** Returns the declared HTTP status. */
    public int status() {
        return status;
    }

    /** Returns the stable machine-readable error code. */
    public String code() {
        return code;
    }

    /** Returns the rejected wire field, if applicable. */
    public String field() {
        return field;
    }

    /** Creates a field-specific validation failure. */
    public static ApiFailure invalid(String field, String message) {
        return new ApiFailure(400, "VALIDATION_ERROR", message, field);
    }

    /** Blocks an approval-dependent branch without guessing institutional business policy. */
    public static ApiFailure pending(String question) {
        return new ApiFailure(403, "POLICY_APPROVAL_REQUIRED", "Approved policy required: " + question, null);
    }
}
