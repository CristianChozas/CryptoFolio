package com.cryptofolio.backend.application.usecase.transaction;

import com.cryptofolio.backend.application.port.out.PortfolioRepository;
import com.cryptofolio.backend.application.port.out.TransactionRepository;
import com.cryptofolio.backend.domain.exception.PortfolioNotFoundException;
import com.cryptofolio.backend.domain.exception.TransactionNotFoundException;
import com.cryptofolio.backend.domain.exception.UnauthorizedPortfolioAccessException;
import com.cryptofolio.backend.domain.model.Portfolio;
import com.cryptofolio.backend.domain.model.Transaction;
import com.cryptofolio.backend.domain.valueobject.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeleteTransactionUseCaseTest {

    private final TransactionRepository transactionRepository = mock(TransactionRepository.class);
    private final PortfolioRepository portfolioRepository = mock(PortfolioRepository.class);

    private final DeleteTransactionUseCase useCase = new DeleteTransactionUseCase(transactionRepository, portfolioRepository);

    @Test
    void shouldDeleteTransactionForPortfolioOwner() {
        Transaction transaction = new Transaction(99L, 15L, "BTC", TransactionType.BUY,
                new BigDecimal("0.10000000"), new BigDecimal("65000.00"), Instant.parse("2026-04-05T18:00:00Z"));
        Portfolio portfolio = new Portfolio(15L, "Main Portfolio", "Long term", 7L,
                Instant.parse("2026-04-05T17:00:00Z"));

        when(transactionRepository.findById(99L)).thenReturn(Optional.of(transaction));
        when(portfolioRepository.findById(15L)).thenReturn(Optional.of(portfolio));

        useCase.execute(7L, 99L);

        verify(transactionRepository).deleteById(99L);
    }

    @Test
    void shouldRejectMissingTransaction() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(7L, 99L))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessage("Transaction not found with id: 99");

        verify(portfolioRepository, never()).findById(15L);
        verify(transactionRepository, never()).deleteById(99L);
    }

    @Test
    void shouldRejectMissingAssociatedPortfolio() {
        Transaction transaction = new Transaction(99L, 15L, "BTC", TransactionType.BUY,
                new BigDecimal("0.10000000"), new BigDecimal("65000.00"), Instant.parse("2026-04-05T18:00:00Z"));

        when(transactionRepository.findById(99L)).thenReturn(Optional.of(transaction));
        when(portfolioRepository.findById(15L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(7L, 99L))
                .isInstanceOf(PortfolioNotFoundException.class)
                .hasMessage("Portfolio not found with id: 15");

        verify(transactionRepository, never()).deleteById(99L);
    }

    @Test
    void shouldRejectTransactionOwnedByAnotherUser() {
        Transaction transaction = new Transaction(99L, 15L, "BTC", TransactionType.BUY,
                new BigDecimal("0.10000000"), new BigDecimal("65000.00"), Instant.parse("2026-04-05T18:00:00Z"));
        Portfolio portfolio = new Portfolio(15L, "Main Portfolio", "Long term", 42L,
                Instant.parse("2026-04-05T17:00:00Z"));

        when(transactionRepository.findById(99L)).thenReturn(Optional.of(transaction));
        when(portfolioRepository.findById(15L)).thenReturn(Optional.of(portfolio));

        assertThatThrownBy(() -> useCase.execute(7L, 99L))
                .isInstanceOf(UnauthorizedPortfolioAccessException.class)
                .hasMessage("User 7 is not authorized to access portfolio: 15");

        verify(transactionRepository, never()).deleteById(99L);
    }
}
