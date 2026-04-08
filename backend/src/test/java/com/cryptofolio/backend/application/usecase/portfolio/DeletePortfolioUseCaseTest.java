package com.cryptofolio.backend.application.usecase.portfolio;

import com.cryptofolio.backend.application.port.out.PortfolioRepository;
import com.cryptofolio.backend.application.port.out.TransactionRepository;
import com.cryptofolio.backend.domain.exception.PortfolioNotFoundException;
import com.cryptofolio.backend.domain.exception.UnauthorizedPortfolioAccessException;
import com.cryptofolio.backend.domain.model.Portfolio;
import com.cryptofolio.backend.domain.model.Transaction;
import com.cryptofolio.backend.domain.valueobject.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeletePortfolioUseCaseTest {

    private final PortfolioRepository portfolioRepository = mock(PortfolioRepository.class);
    private final TransactionRepository transactionRepository = mock(TransactionRepository.class);

    private final DeletePortfolioUseCase useCase = new DeletePortfolioUseCase(portfolioRepository, transactionRepository);

    @Test
    void shouldDeletePortfolioAndRelatedTransactionsForOwner() {
        Portfolio portfolio = new Portfolio(15L, "Main Portfolio", "Long term", 7L,
                Instant.parse("2026-04-05T17:00:00Z"));
        Transaction transaction = new Transaction(99L, 15L, "BTC", TransactionType.BUY,
                new BigDecimal("0.10000000"), new BigDecimal("65000.00"), Instant.parse("2026-04-05T18:00:00Z"));

        when(portfolioRepository.findById(15L)).thenReturn(Optional.of(portfolio));
        when(transactionRepository.findByPortfolioId(15L)).thenReturn(List.of(transaction));

        useCase.execute(7L, 15L);

        verify(transactionRepository).findByPortfolioId(15L);
        verify(transactionRepository).deleteById(99L);
        verify(portfolioRepository).deleteById(15L);
    }

    @Test
    void shouldRejectMissingPortfolio() {
        when(portfolioRepository.findById(15L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(7L, 15L))
                .isInstanceOf(PortfolioNotFoundException.class)
                .hasMessage("Portfolio not found with id: 15");

        verify(transactionRepository, never()).findByPortfolioId(15L);
        verify(portfolioRepository, never()).deleteById(15L);
    }

    @Test
    void shouldRejectPortfolioOwnedByAnotherUser() {
        Portfolio portfolio = new Portfolio(15L, "Main Portfolio", "Long term", 99L,
                Instant.parse("2026-04-05T17:00:00Z"));

        when(portfolioRepository.findById(15L)).thenReturn(Optional.of(portfolio));

        assertThatThrownBy(() -> useCase.execute(7L, 15L))
                .isInstanceOf(UnauthorizedPortfolioAccessException.class)
                .hasMessage("User 7 is not authorized to access portfolio: 15");

        verify(transactionRepository, never()).findByPortfolioId(15L);
        verify(portfolioRepository, never()).deleteById(15L);
    }

    @Test
    void shouldDeletePortfolioWhenItHasNoTransactions() {
        Portfolio portfolio = new Portfolio(15L, "Main Portfolio", "Long term", 7L,
                Instant.parse("2026-04-05T17:00:00Z"));

        when(portfolioRepository.findById(15L)).thenReturn(Optional.of(portfolio));
        when(transactionRepository.findByPortfolioId(15L)).thenReturn(List.of());

        useCase.execute(7L, 15L);

        verify(transactionRepository).findByPortfolioId(15L);
        verify(transactionRepository, never()).deleteById(org.mockito.ArgumentMatchers.any());
        verify(portfolioRepository).deleteById(15L);
    }
}
