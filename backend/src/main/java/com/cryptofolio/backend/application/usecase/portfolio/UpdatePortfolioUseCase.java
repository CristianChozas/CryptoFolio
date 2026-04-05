package com.cryptofolio.backend.application.usecase.portfolio;

import com.cryptofolio.backend.application.dto.request.CreatePortfolioRequest;
import com.cryptofolio.backend.application.dto.response.PortfolioResponse;
import com.cryptofolio.backend.application.mapper.PortfolioMapper;
import com.cryptofolio.backend.application.port.in.UpdatePortfolioInputPort;
import com.cryptofolio.backend.application.port.out.PortfolioRepository;
import com.cryptofolio.backend.domain.exception.PortfolioNotFoundException;
import com.cryptofolio.backend.domain.exception.UnauthorizedPortfolioAccessException;
import com.cryptofolio.backend.domain.model.Portfolio;

import java.util.Objects;

public class UpdatePortfolioUseCase implements UpdatePortfolioInputPort {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioMapper portfolioMapper;

    public UpdatePortfolioUseCase(PortfolioRepository portfolioRepository, PortfolioMapper portfolioMapper) {
        this.portfolioRepository = Objects.requireNonNull(portfolioRepository, "portfolioRepository cannot be null");
        this.portfolioMapper = Objects.requireNonNull(portfolioMapper, "portfolioMapper cannot be null");
    }

    @Override
    public PortfolioResponse execute(Long userId, Long portfolioId, CreatePortfolioRequest request) {
        Long nonNullUserId = Objects.requireNonNull(userId, "userId cannot be null");
        Long nonNullPortfolioId = Objects.requireNonNull(portfolioId, "portfolioId cannot be null");
        CreatePortfolioRequest nonNullRequest = Objects.requireNonNull(request, "request cannot be null");

        Portfolio portfolio = portfolioRepository.findById(nonNullPortfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException(nonNullPortfolioId));

        if (!portfolio.getUserId().equals(nonNullUserId)) {
            throw new UnauthorizedPortfolioAccessException(nonNullPortfolioId, nonNullUserId);
        }

        Portfolio updatedPortfolio = portfolio;
        if (!portfolio.getName().equals(nonNullRequest.name().trim())) {
            updatedPortfolio = updatedPortfolio.rename(nonNullRequest.name());
        }
        updatedPortfolio = updatedPortfolio.updateDescription(nonNullRequest.description());

        Portfolio savedPortfolio = portfolioRepository.save(updatedPortfolio);
        return portfolioMapper.toPortfolioResponse(savedPortfolio);
    }
}
