package com.cryptofolio.backend.application.usecase.portfolio;

import com.cryptofolio.backend.application.port.in.DeletePortfolioInputPort;
import com.cryptofolio.backend.application.port.out.PortfolioRepository;
import com.cryptofolio.backend.application.port.out.TransactionRepository;
import com.cryptofolio.backend.domain.exception.PortfolioNotFoundException;
import com.cryptofolio.backend.domain.exception.UnauthorizedPortfolioAccessException;
import com.cryptofolio.backend.domain.model.Portfolio;

import java.util.Objects;

public class DeletePortfolioUseCase implements DeletePortfolioInputPort {

    private final PortfolioRepository portfolioRepository;
    private final TransactionRepository transactionRepository;

    public DeletePortfolioUseCase(PortfolioRepository portfolioRepository, TransactionRepository transactionRepository) {
        this.portfolioRepository = Objects.requireNonNull(portfolioRepository, "portfolioRepository cannot be null");
        this.transactionRepository = Objects.requireNonNull(transactionRepository, "transactionRepository cannot be null");
    }

    @Override
    public void execute(Long userId, Long portfolioId) {
        Long nonNullUserId = Objects.requireNonNull(userId, "userId cannot be null");
        Long nonNullPortfolioId = Objects.requireNonNull(portfolioId, "portfolioId cannot be null");

        Portfolio portfolio = portfolioRepository.findById(nonNullPortfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException(nonNullPortfolioId));

        if (!portfolio.getUserId().equals(nonNullUserId)) {
            throw new UnauthorizedPortfolioAccessException(nonNullPortfolioId, nonNullUserId);
        }

        transactionRepository.findByPortfolioId(nonNullPortfolioId)
                .forEach(transaction -> transactionRepository.deleteById(transaction.getId()));

        portfolioRepository.deleteById(nonNullPortfolioId);
    }
}
