package com.cryptofolio.backend.infrastructure.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(name = "ErrorResponse", description = "Respuesta estandar para errores de la API.")
public class ErrorResponse {

    @Schema(description = "Momento en que se produjo el error.", example = "2026-04-07T10:15:00Z")
    private final Instant timestamp;

    @Schema(description = "Codigo HTTP del error.", example = "400")
    private final int status;

    @Schema(description = "Mensaje principal del error.", example = "Validation failed")
    private final String message;

    @Schema(description = "Ruta HTTP que produjo el error.", example = "/api/v1/auth/register")
    private final String path;

    @Schema(description = "Detalle de errores de validacion por campo, cuando aplica.")
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
