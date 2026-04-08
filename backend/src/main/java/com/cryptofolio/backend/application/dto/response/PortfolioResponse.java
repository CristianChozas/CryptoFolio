package com.cryptofolio.backend.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "PortfolioResponse", description = "Representacion resumida de un portfolio.")
public class PortfolioResponse {

    @Schema(description = "Identificador del portfolio.", example = "10")
    private final Long id;

    @Schema(description = "Nombre del portfolio.", example = "Main Portfolio")
    private final String name;

    @Schema(description = "Descripcion del portfolio.", example = "Inversion de largo plazo")
    private final String description;

    @Schema(description = "Fecha de creacion del portfolio.", example = "2026-04-07T10:05:00Z")
    private final Instant createdAt;

    public PortfolioResponse(Long id, String name, String description, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
