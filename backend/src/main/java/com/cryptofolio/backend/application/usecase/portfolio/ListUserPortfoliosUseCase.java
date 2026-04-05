package com.cryptofolio.backend.application.usecase.portfolio;

import com.cryptofolio.backend.application.dto.response.PortfolioResponse;
import com.cryptofolio.backend.application.mapper.PortfolioMapper;
import com.cryptofolio.backend.application.port.in.ListUserPortfoliosInputPort;
import com.cryptofolio.backend.application.port.out.PortfolioRepository;
import com.cryptofolio.backend.domain.model.Portfolio;

import java.util.List;
import java.util.Objects;

public class ListUserPortfoliosUseCase implements ListUserPortfoliosInputPort {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioMapper portfolioMapper;

    public ListUserPortfoliosUseCase(PortfolioRepository portfolioRepository, PortfolioMapper portfolioMapper) {
        this.portfolioRepository = Objects.requireNonNull(portfolioRepository, "portfolioRepository cannot be null");
        this.portfolioMapper = Objects.requireNonNull(portfolioMapper, "portfolioMapper cannot be null");
    }

    @Override
    public List<PortfolioResponse> execute(Long userId) {
        Long nonNullUserId = Objects.requireNonNull(userId, "userId cannot be null");

        List<Portfolio> portfolios = portfolioRepository.findByUserId(nonNullUserId);
        return portfolios.stream()
                .map(portfolioMapper::toPortfolioResponse)
                .toList();
    }
}
