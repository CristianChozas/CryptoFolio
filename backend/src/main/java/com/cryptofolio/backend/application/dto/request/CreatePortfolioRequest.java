package com.cryptofolio.backend.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePortfolioRequest(
        @NotBlank(message = "name cannot be blank")
        @Size(max = 100, message = "name must not exceed 100 characters")
        String name,

        @Size(max = 255, message = "description must not exceed 255 characters")
        String description) {
}
