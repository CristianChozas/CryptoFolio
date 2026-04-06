package com.cryptofolio.backend.infrastructure.exception;

import java.time.Instant;
import java.util.Map;

public class ErrorResponse {

    private final Instant timestamp;
    private final int status;
    private final String message;
    private final String path;
    private final Map<String, String> errors;

    public ErrorResponse(Instant timestamp, int status, String message, String path) {
        this(timestamp, status, message, path, null);
    }

    public ErrorResponse(Instant timestamp, int status, String message, String path, Map<String, String> errors) {
        this.timestamp = timestamp;
        this.status = status;
        this.message = message;
        this.path = path;
        this.errors = errors == null ? null : Map.copyOf(errors);
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
