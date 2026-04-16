package com.cryptofolio.backend.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Map;

@Schema(name = "PortfolioOverviewItemResponse", description = "Resumen ampliado de un portfolio con su contenido actual.")
public class PortfolioOverviewItemResponse {

    @Schema(description = "Informacion base del portfolio.")
    private final PortfolioResponse portfolio;

    @Schema(description = "Contenido actual del portfolio por simbolo de cripto.")
    private final Map<String, BigDecimal> balance;

    @Schema(description = "Valor actual estimado del portfolio.", example = "18750.25")
    private final BigDecimal currentValue;

    @Schema(description = "Moneda del valor actual estimado.", example = "USD")
    private final String currentValueCurrency;

    @Schema(description = "Ganancia o perdida total del portfolio.", example = "1520.35")
    private final BigDecimal profitLossAmount;

    @Schema(description = "Moneda del profit/loss.", example = "USD")
    private final String profitLossCurrency;

    @Schema(description = "Retorno sobre la inversion en porcentaje.", example = "12.75")
    private final BigDecimal roiPercentage;

    public PortfolioOverviewItemResponse(
            PortfolioResponse portfolio,
            Map<String, BigDecimal> balance,
            BigDecimal currentValue,
            String currentValueCurrency,
            BigDecimal profitLossAmount,
            String profitLossCurrency,
            BigDecimal roiPercentage) {
        this.portfolio = portfolio;
        this.balance = balance == null ? null : Map.copyOf(balance);
        this.currentValue = currentValue;
        this.currentValueCurrency = currentValueCurrency;
        this.profitLossAmount = profitLossAmount;
        this.profitLossCurrency = profitLossCurrency;
        this.roiPercentage = roiPercentage;
    }

    public PortfolioResponse getPortfolio() {
        return portfolio;
    }

    public Map<String, BigDecimal> getBalance() {
        return balance;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public String getCurrentValueCurrency() {
        return currentValueCurrency;
    }

    public BigDecimal getProfitLossAmount() {
        return profitLossAmount;
    }

    public String getProfitLossCurrency() {
        return profitLossCurrency;
    }

    public BigDecimal getRoiPercentage() {
        return roiPercentage;
    }
}
