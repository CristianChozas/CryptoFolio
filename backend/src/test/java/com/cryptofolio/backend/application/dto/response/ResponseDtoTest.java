package com.cryptofolio.backend.application.dto.response;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseDtoTest {

    @Test
    void givenPortfolioSummaryResponse_whenExposingBalance_thenReturnsUnmodifiableCopy() {
        Map<String, BigDecimal> balance = Map.of("BTC", new BigDecimal("0.5"));
        PortfolioResponse portfolio = new PortfolioResponse(7L, "Main Portfolio", "Long term",
                Instant.parse("2026-04-05T10:00:00Z"));

        PortfolioSummaryResponse response = new PortfolioSummaryResponse(
                portfolio,
                balance,
                new BigDecimal("2500.00"),
                "USD",
                new BigDecimal("12.50"));

        assertThat(response.getPortfolio()).isSameAs(portfolio);
        assertThat(response.getBalance()).containsEntry("BTC", new BigDecimal("0.5"));
        assertThat(response.getBalance()).isUnmodifiable();
    }
}
