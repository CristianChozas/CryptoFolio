package com.cryptofolio.backend.application.usecase.transaction;

import com.cryptofolio.backend.application.dto.request.AddTransactionRequest;
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
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AddTransactionUseCaseTest {

    private static final Instant NOW = Instant.parse("2026-04-05T18:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    private final PortfolioRepository portfolioRepository = mock(PortfolioRepository.class);
    private final TransactionRepository transactionRepository = mock(TransactionRepository.class);
    private final TransactionMapper transactionMapper = Mappers.getMapper(TransactionMapper.class);
    private final AddTransactionUseCase useCase = new AddTransactionUseCase(
            portfolioRepository,
            transactionRepository,
            transactionMapper,
            FIXED_CLOCK);

    @Test
    void givenOwnedPortfolioAndBuyRequest_whenExecuting_thenSavesTransactionWithoutCheckingBalance() {
        Portfolio portfolio = new Portfolio(15L, "Main Portfolio", "Long term", 7L,
                Instant.parse("2026-04-05T17:00:00Z"));
        AddTransactionRequest request = new AddTransactionRequest(15L, "BTC", "BUY",
                new BigDecimal("0.10000000"), new BigDecimal("65000.00"));
        Transaction savedTransaction = new Transaction(99L, 15L, "BTC", TransactionType.BUY,
                new BigDecimal("0.10000000"), new BigDecimal("65000.00"), NOW);

        when(portfolioRepository.findById(15L)).thenReturn(Optional.of(portfolio));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        TransactionResponse response = useCase.execute(7L, request);

        assertThat(response.getId()).isEqualTo(99L);
        assertThat(response.getCrypto()).isEqualTo("BTC");
        assertThat(response.getType()).isEqualTo("BUY");
        verify(transactionRepository, never()).findByPortfolioId(15L);
    }

    @Test
    void givenOwnedPortfolioAndSellRequest_whenExecuting_thenSavesTransactionWithoutBalanceValidation() {
        Portfolio portfolio = new Portfolio(15L, "Main Portfolio", "Long term", 7L,
                Instant.parse("2026-04-05T17:00:00Z"));
        AddTransactionRequest request = new AddTransactionRequest(15L, "BTC", "SELL",
                new BigDecimal("0.10000000"), new BigDecimal("70000.00"));
        Transaction savedTransaction = new Transaction(99L, 15L, "BTC", TransactionType.SELL,
                new BigDecimal("0.10000000"), new BigDecimal("70000.00"), NOW);

        when(portfolioRepository.findById(15L)).thenReturn(Optional.of(portfolio));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        TransactionResponse response = useCase.execute(7L, request);

        assertThat(response.getId()).isEqualTo(99L);
        assertThat(response.getCrypto()).isEqualTo("BTC");
        assertThat(response.getType()).isEqualTo("SELL");
        verify(transactionRepository, never()).findByPortfolioId(15L);
    }

    @Test
    void givenOwnedPortfolioAndNoExistingBalance_whenExecutingSell_thenStillSavesTransaction() {
        Portfolio portfolio = new Portfolio(15L, "Main Portfolio", "Long term", 7L,
                Instant.parse("2026-04-05T17:00:00Z"));
        AddTransactionRequest request = new AddTransactionRequest(15L, "BTC", "SELL",
                new BigDecimal("0.10000000"), new BigDecimal("70000.00"));
        Transaction savedTransaction = new Transaction(99L, 15L, "BTC", TransactionType.SELL,
                new BigDecimal("0.10000000"), new BigDecimal("70000.00"), NOW);

        when(portfolioRepository.findById(15L)).thenReturn(Optional.of(portfolio));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        TransactionResponse response = useCase.execute(7L, request);

        assertThat(response.getId()).isEqualTo(99L);
        assertThat(response.getType()).isEqualTo("SELL");
        verify(transactionRepository).save(any(Transaction.class));
        verify(transactionRepository, never()).findByPortfolioId(15L);
    }

    @Test
    void givenMissingPortfolio_whenExecuting_thenThrowsPortfolioNotFound() {
        AddTransactionRequest request = new AddTransactionRequest(15L, "BTC", "BUY",
                new BigDecimal("0.10000000"), new BigDecimal("65000.00"));

        when(portfolioRepository.findById(15L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(7L, request))
                .isInstanceOf(PortfolioNotFoundException.class)
                .hasMessage("Portfolio not found with id: 15");
    }

    @Test
    void givenPortfolioOwnedByAnotherUser_whenExecuting_thenThrowsUnauthorizedAccess() {
        Portfolio portfolio = new Portfolio(15L, "Main Portfolio", "Long term", 99L,
                Instant.parse("2026-04-05T17:00:00Z"));
        AddTransactionRequest request = new AddTransactionRequest(15L, "BTC", "BUY",
                new BigDecimal("0.10000000"), new BigDecimal("65000.00"));

        when(portfolioRepository.findById(15L)).thenReturn(Optional.of(portfolio));

        assertThatThrownBy(() -> useCase.execute(7L, request))
                .isInstanceOf(UnauthorizedPortfolioAccessException.class)
                .hasMessage("User 7 is not authorized to access portfolio: 15");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void givenOwnedPortfolioAndBuyRequest_whenExecuting_thenUsesClockTimestampForSavedTransaction() {
        Portfolio portfolio = new Portfolio(15L, "Main Portfolio", "Long term", 7L,
                Instant.parse("2026-04-05T17:00:00Z"));
        AddTransactionRequest request = new AddTransactionRequest(15L, "BTC", "BUY",
                new BigDecimal("0.10000000"), new BigDecimal("65000.00"));
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);

        when(portfolioRepository.findById(15L)).thenReturn(Optional.of(portfolio));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response = useCase.execute(7L, request);

        assertThat(response.getCrypto()).isEqualTo("BTC");
        assertThat(response.getType()).isEqualTo("BUY");
        verify(transactionRepository).save(transactionCaptor.capture());
        assertThat(transactionCaptor.getValue().getTimestamp()).isEqualTo(NOW);
    }
}
