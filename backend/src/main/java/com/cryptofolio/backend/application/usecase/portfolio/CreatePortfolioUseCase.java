package com.cryptofolio.backend.application.usecase.portfolio;

import com.cryptofolio.backend.application.dto.request.CreatePortfolioRequest;
import com.cryptofolio.backend.application.dto.response.PortfolioResponse;
import com.cryptofolio.backend.application.mapper.PortfolioMapper;
import com.cryptofolio.backend.application.port.in.CreatePortfolioInputPort;
import com.cryptofolio.backend.application.port.out.PortfolioRepository;
import com.cryptofolio.backend.application.port.out.UserRepository;
import com.cryptofolio.backend.domain.exception.UserNotFoundException;
import com.cryptofolio.backend.domain.model.Portfolio;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class CreatePortfolioUseCase implements CreatePortfolioInputPort {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioMapper portfolioMapper;
    private final Clock clock;

    public CreatePortfolioUseCase(
            UserRepository userRepository,
            PortfolioRepository portfolioRepository,
            PortfolioMapper portfolioMapper,
            Clock clock) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository cannot be null");
        this.portfolioRepository = Objects.requireNonNull(portfolioRepository, "portfolioRepository cannot be null");
        this.portfolioMapper = Objects.requireNonNull(portfolioMapper, "portfolioMapper cannot be null");
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
    }

    @Override
    public PortfolioResponse execute(Long userId, CreatePortfolioRequest request) {
        Long nonNullUserId = Objects.requireNonNull(userId, "userId cannot be null");
        CreatePortfolioRequest nonNullRequest = Objects.requireNonNull(request, "request cannot be null");

        userRepository.findById(nonNullUserId)
                .orElseThrow(() -> new UserNotFoundException(nonNullUserId));

        Instant createdAt = Instant.now(clock);
        Portfolio portfolioToSave = portfolioMapper.toPortfolio(nonNullRequest, nonNullUserId, createdAt);
        Portfolio savedPortfolio = portfolioRepository.save(portfolioToSave);

        return portfolioMapper.toPortfolioResponse(savedPortfolio);
    }
}
