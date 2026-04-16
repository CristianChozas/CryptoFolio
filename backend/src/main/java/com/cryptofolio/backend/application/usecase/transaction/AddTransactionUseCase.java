package com.cryptofolio.backend.application.usecase.transaction;

import com.cryptofolio.backend.application.dto.request.AddTransactionRequest;
import com.cryptofolio.backend.application.dto.response.TransactionResponse;
import com.cryptofolio.backend.application.mapper.TransactionMapper;
import com.cryptofolio.backend.application.port.in.AddTransactionInputPort;
import com.cryptofolio.backend.application.port.out.PortfolioRepository;
import com.cryptofolio.backend.application.port.out.TransactionRepository;
import com.cryptofolio.backend.domain.exception.PortfolioNotFoundException;
import com.cryptofolio.backend.domain.exception.UnauthorizedPortfolioAccessException;
import com.cryptofolio.backend.domain.model.Portfolio;
import com.cryptofolio.backend.domain.model.Transaction;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class AddTransactionUseCase implements AddTransactionInputPort {

    private final PortfolioRepository portfolioRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final Clock clock;

    public AddTransactionUseCase(
            PortfolioRepository portfolioRepository,
            TransactionRepository transactionRepository,
            TransactionMapper transactionMapper,
            Clock clock) {
        this.portfolioRepository = Objects.requireNonNull(portfolioRepository, "portfolioRepository cannot be null");
        this.transactionRepository = Objects.requireNonNull(transactionRepository, "transactionRepository cannot be null");
        this.transactionMapper = Objects.requireNonNull(transactionMapper, "transactionMapper cannot be null");
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

        Instant timestamp = Instant.now(clock);
        Transaction transactionToSave = transactionMapper.toTransaction(nonNullRequest, timestamp);
        Transaction savedTransaction = transactionRepository.save(transactionToSave);

        return transactionMapper.toTransactionResponse(savedTransaction);
    }
}
