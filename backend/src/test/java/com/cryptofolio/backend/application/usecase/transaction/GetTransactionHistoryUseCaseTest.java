package com.cryptofolio.backend.application.usecase.transaction;

import com.cryptofolio.backend.application.dto.response.TransactionResponse;
import com.cryptofolio.backend.application.mapper.TransactionMapper;
import com.cryptofolio.backend.application.port.out.PortfolioRepository;
import com.cryptofolio.backend.application.port.out.TransactionRepository;
import com.cryptofolio.backend.domain.exception.PortfolioNotFoundException;
import com.cryptofolio.backend.domain.exception.UnauthorizedPortfolioAccessException;
import com.cryptofolio.backend.domain.model.Portfolio;
import com.cryptofolio.backend.domain.model.Transaction;
import com.cryptofolio.backend.domain.valueobject.TransactionType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetTransactionHistoryUseCaseTest {

    private final PortfolioRepository portfolioRepository = mock(PortfolioRepository.class);
    private final TransactionRepository transactionRepository = mock(TransactionRepository.class);
    private final TransactionMapper transactionMapper = Mappers.getMapper(TransactionMapper.class);

    private final GetTransactionHistoryUseCase useCase = new GetTransactionHistoryUseCase(
            portfolioRepository,
            transactionRepository,
            transactionMapper);

    @Test
    void shouldReturnTransactionsOrderedByMostRecentFirst() {
        Portfolio portfolio = new Portfolio(15L, "Main Portfolio", "Long term", 7L,
                Instant.parse("2026-04-05T17:00:00Z"));
        Transaction older = new Transaction(99L, 15L, "BTC", TransactionType.BUY,
                new BigDecimal("0.10000000"), new BigDecimal("65000.00"), Instant.parse("2026-04-05T18:00:00Z"));
        Transaction newer = new Transaction(100L, 15L, "ETH", TransactionType.BUY,
                new BigDecimal("1.00000000"), new BigDecimal("3000.00"), Instant.parse("2026-04-06T18:00:00Z"));

        when(portfolioRepository.findById(15L)).thenReturn(Optional.of(portfolio));
        when(transactionRepository.findByPortfolioId(15L)).thenReturn(List.of(older, newer));

        List<TransactionResponse> response = useCase.execute(7L, 15L);

        assertThat(response).hasSize(2);
        assertThat(response)
                .extracting(TransactionResponse::getId)
                .containsExactly(100L, 99L);
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
        Portfolio portfolio = new Portfolio(15L, "Main Portfolio", "Long term", 99L,
                Instant.parse("2026-04-05T17:00:00Z"));

        when(portfolioRepository.findById(15L)).thenReturn(Optional.of(portfolio));

        assertThatThrownBy(() -> useCase.execute(7L, 15L))
                .isInstanceOf(UnauthorizedPortfolioAccessException.class)
                .hasMessage("User 7 is not authorized to access portfolio: 15");
    }
}
