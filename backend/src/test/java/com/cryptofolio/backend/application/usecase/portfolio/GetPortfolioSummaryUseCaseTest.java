package com.cryptofolio.backend.application.usecase.portfolio;

import com.cryptofolio.backend.application.dto.response.PortfolioSummaryResponse;
import com.cryptofolio.backend.application.mapper.PortfolioMapper;
import com.cryptofolio.backend.application.port.out.CryptoPriceProvider;
import com.cryptofolio.backend.application.port.out.PortfolioRepository;
import com.cryptofolio.backend.application.port.out.TransactionRepository;
import com.cryptofolio.backend.domain.exception.PortfolioNotFoundException;
import com.cryptofolio.backend.domain.exception.UnauthorizedPortfolioAccessException;
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
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetPortfolioSummaryUseCaseTest {

    private final PortfolioRepository portfolioRepository = mock(PortfolioRepository.class);
    private final TransactionRepository transactionRepository = mock(TransactionRepository.class);
    private final CryptoPriceProvider cryptoPriceProvider = mock(CryptoPriceProvider.class);
    private final PortfolioCalculator portfolioCalculator = new PortfolioCalculator();
    private final PortfolioMapper portfolioMapper = Mappers.getMapper(PortfolioMapper.class);

    private final GetPortfolioSummaryUseCase useCase = new GetPortfolioSummaryUseCase(
            portfolioRepository,
            transactionRepository,
            cryptoPriceProvider,
            portfolioCalculator,
            portfolioMapper);

    @Test
    void shouldReturnPortfolioSummaryForOwner() {
        Portfolio portfolio = new Portfolio(15L, "Main Portfolio", "Long term", 7L,
                Instant.parse("2026-04-05T17:00:00Z"));
        Transaction transaction = new Transaction(99L, 15L, "BTC", TransactionType.BUY,
                new BigDecimal("0.10000000"), new BigDecimal("65000.00"), Instant.parse("2026-04-05T18:00:00Z"));

        when(portfolioRepository.findById(15L)).thenReturn(Optional.of(portfolio));
        when(transactionRepository.findByPortfolioId(15L)).thenReturn(List.of(transaction));
        when(cryptoPriceProvider.getCurrentPrice(Crypto.from("BTC"))).thenReturn(new BigDecimal("70000.00"));

        PortfolioSummaryResponse response = useCase.execute(7L, 15L);

        assertThat(response.getPortfolio().getId()).isEqualTo(15L);
        assertThat(response.getBalance()).containsEntry("BTC", new BigDecimal("0.10000000"));
        assertThat(response.getProfitLossAmount()).isEqualByComparingTo("500.00");
        assertThat(response.getProfitLossCurrency()).isEqualTo("USD");
        assertThat(response.getRoiPercentage()).isEqualByComparingTo("7.69");

        verify(cryptoPriceProvider).getCurrentPrice(Crypto.from("BTC"));
    }

    @Test
    void shouldRejectMissingPortfolio() {
        when(portfolioRepository.findById(15L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(7L, 15L))
                .isInstanceOf(PortfolioNotFoundException.class)
                .hasMessage("Portfolio not found with id: 15");
    }

    @Test
    void shouldRejectPortfolioOwnedByAnotherUser() {
        Portfolio portfolio = new Portfolio(15L, "Main Portfolio", "Long term", 42L,
                Instant.parse("2026-04-05T17:00:00Z"));

        when(portfolioRepository.findById(15L)).thenReturn(Optional.of(portfolio));

        assertThatThrownBy(() -> useCase.execute(7L, 15L))
                .isInstanceOf(UnauthorizedPortfolioAccessException.class)
                .hasMessage("User 7 is not authorized to access portfolio: 15");
    }

    @Test
    void shouldReturnZeroSummaryWhenPortfolioHasNoTransactions() {
        Portfolio portfolio = new Portfolio(15L, "Main Portfolio", "Long term", 7L,
                Instant.parse("2026-04-05T17:00:00Z"));

        when(portfolioRepository.findById(15L)).thenReturn(Optional.of(portfolio));
        when(transactionRepository.findByPortfolioId(15L)).thenReturn(List.of());

        PortfolioSummaryResponse response = useCase.execute(7L, 15L);

        assertThat(response.getPortfolio().getId()).isEqualTo(15L);
        assertThat(response.getBalance()).isEqualTo(Map.of());
        assertThat(response.getProfitLossAmount()).isEqualByComparingTo("0.00");
        assertThat(response.getProfitLossCurrency()).isEqualTo("USD");
        assertThat(response.getRoiPercentage()).isEqualByComparingTo("0");
        verify(cryptoPriceProvider, never()).getCurrentPrice(org.mockito.ArgumentMatchers.any());
    }
}
