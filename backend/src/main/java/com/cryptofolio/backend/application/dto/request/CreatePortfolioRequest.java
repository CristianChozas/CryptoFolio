package com.cryptofolio.backend.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "CreatePortfolioRequest", description = "Payload para crear o actualizar un portfolio.")
public record CreatePortfolioRequest(
        @Schema(description = "Nombre del portfolio.", example = "Long Term BTC")
        @NotBlank(message = "name cannot be blank")
        @Size(max = 100, message = "name must not exceed 100 characters")
        String name,

        @Schema(description = "Descripcion opcional del portfolio.", example = "Compras a largo plazo")
        @Size(max = 255, message = "description must not exceed 255 characters")
        String description) {
}
