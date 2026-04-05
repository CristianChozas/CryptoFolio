package com.cryptofolio.backend.application.mapper;

import com.cryptofolio.backend.application.dto.request.CreatePortfolioRequest;
import com.cryptofolio.backend.application.dto.response.PortfolioResponse;
import com.cryptofolio.backend.application.dto.response.PortfolioSummaryResponse;
import com.cryptofolio.backend.domain.model.Portfolio;
import com.cryptofolio.backend.domain.valueobject.Crypto;
import com.cryptofolio.backend.domain.valueobject.Money;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PortfolioMapperTest {

    private final PortfolioMapper mapper = Mappers.getMapper(PortfolioMapper.class);

    @Test
    void shouldMapCreatePortfolioRequestToPortfolio() {
        CreatePortfolioRequest request = new CreatePortfolioRequest("Main Portfolio", "Long term");
        Instant createdAt = Instant.parse("2026-04-05T12:00:00Z");

        Portfolio portfolio = mapper.toPortfolio(request, 42L, createdAt);

        assertThat(portfolio.getId()).isNull();
        assertThat(portfolio.getName()).isEqualTo("Main Portfolio");
        assertThat(portfolio.getDescription()).isEqualTo("Long term");
        assertThat(portfolio.getUserId()).isEqualTo(42L);
        assertThat(portfolio.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldMapPortfolioToPortfolioResponse() {
        Instant createdAt = Instant.parse("2026-04-05T12:00:00Z");
        Portfolio portfolio = new Portfolio(8L, "Main Portfolio", "Long term", 42L, createdAt);

        PortfolioResponse response = mapper.toPortfolioResponse(portfolio);

        assertThat(response.getId()).isEqualTo(8L);
        assertThat(response.getName()).isEqualTo("Main Portfolio");
        assertThat(response.getDescription()).isEqualTo("Long term");
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldMapPortfolioSummaryToResponse() {
        Portfolio portfolio = new Portfolio(8L, "Main Portfolio", "Long term", 42L,
                Instant.parse("2026-04-05T12:00:00Z"));
        Map<Crypto, BigDecimal> balance = Map.of(Crypto.from("BTC"), new BigDecimal("0.50"));
        Money profitLoss = Money.signed(new BigDecimal("2500.00"), "USD");

        PortfolioSummaryResponse response = mapper.toPortfolioSummaryResponse(
                portfolio,
                balance,
                profitLoss,
                new BigDecimal("12.50"));

        assertThat(response.getPortfolio().getId()).isEqualTo(8L);
        assertThat(response.getBalance()).containsEntry("BTC", new BigDecimal("0.50"));
        assertThat(response.getProfitLossAmount()).isEqualByComparingTo("2500.00");
        assertThat(response.getProfitLossCurrency()).isEqualTo("USD");
        assertThat(response.getRoiPercentage()).isEqualByComparingTo("12.50");
    }
}
