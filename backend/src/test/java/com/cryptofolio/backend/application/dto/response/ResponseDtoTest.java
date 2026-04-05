package com.cryptofolio.backend.application.dto.response;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseDtoTest {

    @Test
    void shouldExposeAuthResponseFieldsThroughGetters() {
        AuthResponse response = new AuthResponse("jwt-token", 42L, "cristian");

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUserId()).isEqualTo(42L);
        assertThat(response.getUsername()).isEqualTo("cristian");
    }

    @Test
    void shouldExposePortfolioResponseFieldsThroughGetters() {
        Instant createdAt = Instant.parse("2026-04-05T10:00:00Z");
        PortfolioResponse response = new PortfolioResponse(7L, "Main Portfolio", "Long term", createdAt);

        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getName()).isEqualTo("Main Portfolio");
        assertThat(response.getDescription()).isEqualTo("Long term");
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldExposeTransactionResponseFieldsThroughGetters() {
        Instant timestamp = Instant.parse("2026-04-05T11:00:00Z");
        TransactionResponse response = new TransactionResponse(
                3L,
                "BTC",
                "BUY",
                new BigDecimal("0.15000000"),
                new BigDecimal("65000.00"),
                timestamp);

        assertThat(response.getId()).isEqualTo(3L);
        assertThat(response.getCrypto()).isEqualTo("BTC");
        assertThat(response.getType()).isEqualTo("BUY");
        assertThat(response.getAmount()).isEqualByComparingTo("0.15000000");
        assertThat(response.getPricePerUnit()).isEqualByComparingTo("65000.00");
        assertThat(response.getTimestamp()).isEqualTo(timestamp);
    }

    @Test
    void shouldDefensivelyCopyPortfolioSummaryBalance() {
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
        assertThat(response.getProfitLossAmount()).isEqualByComparingTo("2500.00");
        assertThat(response.getProfitLossCurrency()).isEqualTo("USD");
        assertThat(response.getRoiPercentage()).isEqualByComparingTo("12.50");
        assertThat(response.getBalance()).isUnmodifiable();
    }
}
