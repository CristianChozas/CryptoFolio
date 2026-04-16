package com.cryptofolio.backend.application.usecase.portfolio;

import com.cryptofolio.backend.application.dto.response.PortfolioOverviewResponse;
import com.cryptofolio.backend.application.mapper.PortfolioMapper;
import com.cryptofolio.backend.application.port.out.CryptoPriceProvider;
import com.cryptofolio.backend.application.port.out.PortfolioRepository;
import com.cryptofolio.backend.application.port.out.TransactionRepository;
import com.cryptofolio.backend.domain.model.Portfolio;
import com.cryptofolio.backend.domain.model.Transaction;
import com.cryptofolio.backend.domain.service.PortfolioCalculator;
import com.cryptofolio.backend.domain.valueobject.Crypto;
import com.cryptofolio.backend.domain.valueobject.TransactionType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetPortfolioOverviewUseCaseTest {

    private final PortfolioRepository portfolioRepository = mock(PortfolioRepository.class);
    private final TransactionRepository transactionRepository = mock(TransactionRepository.class);
    private final CryptoPriceProvider cryptoPriceProvider = mock(CryptoPriceProvider.class);
    private final PortfolioCalculator portfolioCalculator = new PortfolioCalculator();
    private final PortfolioMapper portfolioMapper = Mappers.getMapper(PortfolioMapper.class);

    private final GetPortfolioOverviewUseCase useCase = new GetPortfolioOverviewUseCase(
            portfolioRepository,
            transactionRepository,
            cryptoPriceProvider,
            portfolioCalculator,
            portfolioMapper);

    @Test
    void shouldReturnAggregatedOverviewForUser() {
        Portfolio main = new Portfolio(15L, "Main Portfolio", "Long term", 7L, Instant.parse("2026-04-05T17:00:00Z"));
        Portfolio trading = new Portfolio(16L, "Trading", "Swing", 7L, Instant.parse("2026-04-06T17:00:00Z"));

        Transaction buyBtc = new Transaction(99L, 15L, "BTC", TransactionType.BUY,
                new BigDecimal("0.10000000"), new BigDecimal("65000.00"), Instant.parse("2026-04-05T18:00:00Z"));
        Transaction buyEth = new Transaction(100L, 16L, "ETH", TransactionType.BUY,
                new BigDecimal("2.00000000"), new BigDecimal("3000.00"), Instant.parse("2026-04-06T19:00:00Z"));

        when(portfolioRepository.findByUserId(7L)).thenReturn(List.of(main, trading));
        when(transactionRepository.findByPortfolioId(15L)).thenReturn(List.of(buyBtc));
        when(transactionRepository.findByPortfolioId(16L)).thenReturn(List.of(buyEth));
        when(cryptoPriceProvider.getCurrentPrice(Crypto.from("BTC"))).thenReturn(new BigDecimal("70000.00"));
        when(cryptoPriceProvider.getCurrentPrice(Crypto.from("ETH"))).thenReturn(new BigDecimal("3500.00"));

        PortfolioOverviewResponse response = useCase.execute(7L);

        assertThat(response.getPortfolioCount()).isEqualTo(2);
        assertThat(response.getPortfolios()).hasSize(2);
        assertThat(response.getTotalCurrentValue()).isEqualByComparingTo("14000.00000000");
        assertThat(response.getTotalProfitLossAmount()).isEqualByComparingTo("1500.00");
        assertThat(response.getRecentOperations()).hasSize(2);
        assertThat(response.getRecentOperations().get(0).getPortfolioName()).isEqualTo("Trading");
    }

    @Test
    void shouldReturnEmptyOverviewWhenUserHasNoPortfolios() {
        when(portfolioRepository.findByUserId(7L)).thenReturn(List.of());

        PortfolioOverviewResponse response = useCase.execute(7L);

        assertThat(response.getPortfolioCount()).isZero();
        assertThat(response.getPortfolios()).isEmpty();
        assertThat(response.getRecentOperations()).isEmpty();
        assertThat(response.getTotalCurrentValue()).isEqualByComparingTo("0");
        assertThat(response.getTotalProfitLossAmount()).isEqualByComparingTo("0");
    }

    @Test
    void shouldReturnOverviewWhenPriceProviderFailsForCrypto() {
        Portfolio main = new Portfolio(15L, "Main Portfolio", "Long term", 7L, Instant.parse("2026-04-05T17:00:00Z"));
        Transaction buyBtc = new Transaction(99L, 15L, "BTC", TransactionType.BUY,
                new BigDecimal("0.10000000"), new BigDecimal("65000.00"), Instant.parse("2026-04-05T18:00:00Z"));

        when(portfolioRepository.findByUserId(7L)).thenReturn(List.of(main));
        when(transactionRepository.findByPortfolioId(15L)).thenReturn(List.of(buyBtc));
        when(cryptoPriceProvider.getCurrentPrice(Crypto.from("BTC"))).thenThrow(new IllegalStateException("price unavailable"));

        PortfolioOverviewResponse response = useCase.execute(7L);

        assertThat(response.getPortfolioCount()).isEqualTo(1);
        assertThat(response.getPortfolios()).hasSize(1);
        assertThat(response.getRecentOperations()).hasSize(1);
        assertThat(response.getTotalCurrentValue()).isEqualByComparingTo("0");
    }
}
