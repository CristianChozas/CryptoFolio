package com.cryptofolio.backend.application.usecase.transaction;

import com.cryptofolio.backend.application.dto.response.TransactionResponse;
import com.cryptofolio.backend.application.mapper.TransactionMapper;
import com.cryptofolio.backend.application.port.in.GetTransactionHistoryInputPort;
import com.cryptofolio.backend.application.port.out.PortfolioRepository;
import com.cryptofolio.backend.application.port.out.TransactionRepository;
import com.cryptofolio.backend.domain.exception.PortfolioNotFoundException;
import com.cryptofolio.backend.domain.exception.UnauthorizedPortfolioAccessException;
import com.cryptofolio.backend.domain.model.Portfolio;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class GetTransactionHistoryUseCase implements GetTransactionHistoryInputPort {

    private final PortfolioRepository portfolioRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    public GetTransactionHistoryUseCase(
            PortfolioRepository portfolioRepository,
            TransactionRepository transactionRepository,
            TransactionMapper transactionMapper) {
        this.portfolioRepository = Objects.requireNonNull(portfolioRepository, "portfolioRepository cannot be null");
        this.transactionRepository = Objects.requireNonNull(transactionRepository, "transactionRepository cannot be null");
        this.transactionMapper = Objects.requireNonNull(transactionMapper, "transactionMapper cannot be null");
    }

    @Override
    public List<TransactionResponse> execute(Long userId, Long portfolioId) {
        Long nonNullUserId = Objects.requireNonNull(userId, "userId cannot be null");
        Long nonNullPortfolioId = Objects.requireNonNull(portfolioId, "portfolioId cannot be null");

        Portfolio portfolio = portfolioRepository.findById(nonNullPortfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException(nonNullPortfolioId));

        if (!portfolio.getUserId().equals(nonNullUserId)) {
            throw new UnauthorizedPortfolioAccessException(nonNullPortfolioId, nonNullUserId);
        }

        return transactionRepository.findByPortfolioId(nonNullPortfolioId).stream()
                .sorted(Comparator.comparing(com.cryptofolio.backend.domain.model.Transaction::getTimestamp).reversed())
                .map(transactionMapper::toTransactionResponse)
                .toList();
    }
}
