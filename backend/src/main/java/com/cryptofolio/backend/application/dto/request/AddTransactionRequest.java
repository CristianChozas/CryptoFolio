package com.cryptofolio.backend.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(name = "AddTransactionRequest", description = "Payload para registrar una compra o venta de cripto.")
public record AddTransactionRequest(
        @Schema(description = "Identificador del portfolio al que pertenece la transaccion.", example = "10")
        @NotNull(message = "portfolioId cannot be null")
        @Positive(message = "portfolioId must be greater than zero")
        Long portfolioId,

        @Schema(description = "Codigo de la criptomoneda.", example = "BTC")
        @NotBlank(message = "crypto cannot be blank")
        @Pattern(regexp = "^[A-Z0-9]{2,10}$", message = "crypto must contain 2-10 uppercase letters or numbers")
        String crypto,

        @Schema(description = "Tipo de transaccion.", example = "BUY", allowableValues = {"BUY", "SELL"})
        @NotBlank(message = "type cannot be blank")
        @Pattern(regexp = "BUY|SELL", message = "type must be BUY or SELL")
        String type,

        @Schema(description = "Cantidad de cripto operada.", example = "0.25000000")
        @NotNull(message = "amount cannot be null")
        @Positive(message = "amount must be greater than zero")
        BigDecimal amount,

        @Schema(description = "Precio unitario en USD al momento de la transaccion.", example = "65000.00")
        @NotNull(message = "pricePerUnit cannot be null")
        @Positive(message = "pricePerUnit must be greater than zero")
        BigDecimal pricePerUnit) {
}
