package com.cryptofolio.backend.application.dto.response;

import java.math.BigDecimal;
import java.util.Map;

public class PortfolioSummaryResponse {

    private final PortfolioResponse portfolio;
    private final Map<String, BigDecimal> balance;
    private final BigDecimal profitLossAmount;
    private final String profitLossCurrency;
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
