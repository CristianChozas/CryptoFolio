package com.cryptofolio.backend.application.usecase.transaction;

import com.cryptofolio.backend.application.dto.request.AddTransactionRequest;
import com.cryptofolio.backend.application.dto.response.TransactionResponse;
import com.cryptofolio.backend.application.mapper.TransactionMapper;
import com.cryptofolio.backend.application.port.in.AddTransactionInputPort;
import com.cryptofolio.backend.application.port.out.PortfolioRepository;
import com.cryptofolio.backend.application.port.out.TransactionRepository;
import com.cryptofolio.backend.domain.exception.InsufficientFundsException;
import com.cryptofolio.backend.domain.exception.PortfolioNotFoundException;
import com.cryptofolio.backend.domain.exception.UnauthorizedPortfolioAccessException;
import com.cryptofolio.backend.domain.model.Portfolio;
import com.cryptofolio.backend.domain.model.Transaction;
import com.cryptofolio.backend.domain.service.PortfolioCalculator;
import com.cryptofolio.backend.domain.valueobject.Crypto;
import com.cryptofolio.backend.domain.valueobject.TransactionType;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AddTransactionUseCase implements AddTransactionInputPort {

    private final PortfolioRepository portfolioRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final PortfolioCalculator portfolioCalculator;
    private final Clock clock;

    public AddTransactionUseCase(
            PortfolioRepository portfolioRepository,
            TransactionRepository transactionRepository,
            TransactionMapper transactionMapper,
            PortfolioCalculator portfolioCalculator,
            Clock clock) {
        this.portfolioRepository = Objects.requireNonNull(portfolioRepository, "portfolioRepository cannot be null");
        this.transactionRepository = Objects.requireNonNull(transactionRepository, "transactionRepository cannot be null");
        this.transactionMapper = Objects.requireNonNull(transactionMapper, "transactionMapper cannot be null");
        this.portfolioCalculator = Objects.requireNonNull(portfolioCalculator, "portfolioCalculator cannot be null");
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
    }

    @Override
    public TransactionResponse execute(Long userId, AddTransactionRequest request) {
        Long nonNullUserId = Objects.requireNonNull(userId, "userId cannot be null");
        AddTransactionRequest nonNullRequest = Objects.requireNonNull(request, "request cannot be null");

        Portfolio portfolio = portfolioRepository.findById(nonNullRequest.portfolioId())
                .orElseThrow(() -> new PortfolioNotFoundException(nonNullRequest.portfolioId()));

        if (!portfolio.getUserId().equals(nonNullUserId)) {
            throw new UnauthorizedPortfolioAccessException(portfolio.getId(), nonNullUserId);
        }

        validateSellBalanceIfNeeded(nonNullRequest);

        Instant timestamp = Instant.now(clock);
        Transaction transactionToSave = transactionMapper.toTransaction(nonNullRequest, timestamp);
        Transaction savedTransaction = transactionRepository.save(transactionToSave);

        return transactionMapper.toTransactionResponse(savedTransaction);
    }

    private void validateSellBalanceIfNeeded(AddTransactionRequest request) {
        TransactionType transactionType = TransactionType.from(request.type());
        if (!transactionType.isSell()) {
            return;
        }

        List<Transaction> existingTransactions = transactionRepository.findByPortfolioId(request.portfolioId());
        Map<Crypto, BigDecimal> balance = portfolioCalculator.calculateBalance(existingTransactions);
        Crypto crypto = Crypto.from(request.crypto());
        BigDecimal availableAmount = balance.getOrDefault(crypto, BigDecimal.ZERO);

        if (availableAmount.compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException(crypto.getSymbol(), availableAmount.toPlainString(), request.amount().toPlainString());
        }
    }
}
