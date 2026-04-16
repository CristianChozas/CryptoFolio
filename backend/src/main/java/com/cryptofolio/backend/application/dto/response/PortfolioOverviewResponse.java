package com.cryptofolio.backend.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(name = "PortfolioOverviewResponse", description = "Vista agregada de portfolios, contenido y operaciones recientes del usuario autenticado.")
public class PortfolioOverviewResponse {

    @Schema(description = "Numero total de portfolios del usuario.", example = "2")
    private final int portfolioCount;

    @Schema(description = "Valor total estimado de todos los portfolios.", example = "24860.42")
    private final BigDecimal totalCurrentValue;

    @Schema(description = "Moneda del valor total estimado.", example = "USD")
    private final String totalCurrentValueCurrency;

    @Schema(description = "Profit/loss agregado de todos los portfolios.", example = "3284.10")
    private final BigDecimal totalProfitLossAmount;

    @Schema(description = "Moneda del profit/loss agregado.", example = "USD")
    private final String totalProfitLossCurrency;

    @Schema(description = "Listado resumido de portfolios con contenido y metricas actuales.")
    private final List<PortfolioOverviewItemResponse> portfolios;

    @Schema(description = "Operaciones recientes de todos los portfolios.")
    private final List<PortfolioOverviewOperationResponse> recentOperations;

    public PortfolioOverviewResponse(
            int portfolioCount,
            BigDecimal totalCurrentValue,
            String totalCurrentValueCurrency,
            BigDecimal totalProfitLossAmount,
            String totalProfitLossCurrency,
            List<PortfolioOverviewItemResponse> portfolios,
            List<PortfolioOverviewOperationResponse> recentOperations) {
        this.portfolioCount = portfolioCount;
        this.totalCurrentValue = totalCurrentValue;
        this.totalCurrentValueCurrency = totalCurrentValueCurrency;
        this.totalProfitLossAmount = totalProfitLossAmount;
        this.totalProfitLossCurrency = totalProfitLossCurrency;
        this.portfolios = portfolios == null ? List.of() : List.copyOf(portfolios);
        this.recentOperations = recentOperations == null ? List.of() : List.copyOf(recentOperations);
    }

    public int getPortfolioCount() {
        return portfolioCount;
    }

    public BigDecimal getTotalCurrentValue() {
        return totalCurrentValue;
    }

    public String getTotalCurrentValueCurrency() {
        return totalCurrentValueCurrency;
    }

    public BigDecimal getTotalProfitLossAmount() {
        return totalProfitLossAmount;
    }

    public String getTotalProfitLossCurrency() {
        return totalProfitLossCurrency;
    }

    public List<PortfolioOverviewItemResponse> getPortfolios() {
        return portfolios;
    }

    public List<PortfolioOverviewOperationResponse> getRecentOperations() {
        return recentOperations;
    }
}
