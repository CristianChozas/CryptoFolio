package com.cryptofolio.backend.application.usecase.transaction;

import com.cryptofolio.backend.application.port.in.DeleteTransactionInputPort;
import com.cryptofolio.backend.application.port.out.PortfolioRepository;
import com.cryptofolio.backend.application.port.out.TransactionRepository;
import com.cryptofolio.backend.domain.exception.PortfolioNotFoundException;
import com.cryptofolio.backend.domain.exception.TransactionNotFoundException;
import com.cryptofolio.backend.domain.exception.UnauthorizedPortfolioAccessException;
import com.cryptofolio.backend.domain.model.Portfolio;
import com.cryptofolio.backend.domain.model.Transaction;

import java.util.Objects;

public class DeleteTransactionUseCase implements DeleteTransactionInputPort {

    private final TransactionRepository transactionRepository;
    private final PortfolioRepository portfolioRepository;

    public DeleteTransactionUseCase(TransactionRepository transactionRepository, PortfolioRepository portfolioRepository) {
        this.transactionRepository = Objects.requireNonNull(transactionRepository, "transactionRepository cannot be null");
        this.portfolioRepository = Objects.requireNonNull(portfolioRepository, "portfolioRepository cannot be null");
    }

    @Override
    public void execute(Long userId, Long transactionId) {
        Long nonNullUserId = Objects.requireNonNull(userId, "userId cannot be null");
        Long nonNullTransactionId = Objects.requireNonNull(transactionId, "transactionId cannot be null");

        Transaction transaction = transactionRepository.findById(nonNullTransactionId)
                .orElseThrow(() -> new TransactionNotFoundException(nonNullTransactionId));

        Portfolio portfolio = portfolioRepository.findById(transaction.getPortfolioId())
                .orElseThrow(() -> new PortfolioNotFoundException(transaction.getPortfolioId()));

        if (!portfolio.getUserId().equals(nonNullUserId)) {
            throw new UnauthorizedPortfolioAccessException(portfolio.getId(), nonNullUserId);
        }

        transactionRepository.deleteById(nonNullTransactionId);
    }
}
