package com.cryptofolio.backend.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AddTransactionRequest(
        @NotNull(message = "portfolioId cannot be null")
        @Positive(message = "portfolioId must be greater than zero")
        Long portfolioId,

        @NotBlank(message = "crypto cannot be blank")
        @Pattern(regexp = "^[A-Z0-9]{2,10}$", message = "crypto must contain 2-10 uppercase letters or numbers")
        String crypto,

        @NotBlank(message = "type cannot be blank")
        @Pattern(regexp = "BUY|SELL", message = "type must be BUY or SELL")
        String type,

        @NotNull(message = "amount cannot be null")
        @Positive(message = "amount must be greater than zero")
        BigDecimal amount,

        @NotNull(message = "pricePerUnit cannot be null")
        @Positive(message = "pricePerUnit must be greater than zero")
        BigDecimal pricePerUnit) {
}
