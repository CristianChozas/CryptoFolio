package com.cryptofolio.backend.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Map;

@Schema(name = "PortfolioSummaryResponse", description = "Resumen financiero actual de un portfolio.")
public class PortfolioSummaryResponse {

    @Schema(description = "Informacion base del portfolio.")
    private final PortfolioResponse portfolio;

    @Schema(description = "Balance actual por simbolo de cripto.")
    private final Map<String, BigDecimal> balance;

    @Schema(description = "Ganancia o perdida total en moneda fiat.", example = "1520.35")
    private final BigDecimal profitLossAmount;

    @Schema(description = "Moneda del profit/loss.", example = "USD")
    private final String profitLossCurrency;

    @Schema(description = "Retorno sobre la inversion en porcentaje.", example = "12.75")
    private final BigDecimal roiPercentage;

    public PortfolioSummaryResponse(
            PortfolioResponse portfolio,
            Map<String, BigDecimal> balance,
            BigDecimal profitLossAmount,
            String profitLossCurrency,
            BigDecimal roiPercentage) {
        this.portfolio = portfolio;
        this.balance = balance == null ? null : Map.copyOf(balance);
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
