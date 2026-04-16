package com.cryptofolio.backend.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(name = "PortfolioOverviewOperationResponse", description = "Operacion reciente asociada al overview de portfolios.")
public class PortfolioOverviewOperationResponse {

    @Schema(description = "Identificador de la transaccion.", example = "77")
    private final Long id;

    @Schema(description = "Identificador del portfolio.", example = "10")
    private final Long portfolioId;

    @Schema(description = "Nombre del portfolio.", example = "Main Portfolio")
    private final String portfolioName;

    @Schema(description = "Simbolo de la criptomoneda.", example = "BTC")
    private final String crypto;

    @Schema(description = "Tipo de operacion.", example = "BUY")
    private final String type;

    @Schema(description = "Cantidad operada.", example = "0.25000000")
    private final BigDecimal amount;

    @Schema(description = "Precio unitario en USD.", example = "65000.00")
    private final BigDecimal pricePerUnit;

    @Schema(description = "Fecha y hora de la operacion.", example = "2026-04-07T10:15:00Z")
    private final Instant timestamp;

    public PortfolioOverviewOperationResponse(
            Long id,
            Long portfolioId,
            String portfolioName,
            String crypto,
            String type,
            BigDecimal amount,
            BigDecimal pricePerUnit,
            Instant timestamp) {
        this.id = id;
        this.portfolioId = portfolioId;
        this.portfolioName = portfolioName;
        this.crypto = crypto;
        this.type = type;
        this.amount = amount;
        this.pricePerUnit = pricePerUnit;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public Long getPortfolioId() {
        return portfolioId;
    }

    public String getPortfolioName() {
        return portfolioName;
    }

    public String getCrypto() {
        return crypto;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getPricePerUnit() {
        return pricePerUnit;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
