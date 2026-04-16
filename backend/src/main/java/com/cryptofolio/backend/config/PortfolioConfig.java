package com.cryptofolio.backend.config;

import com.cryptofolio.backend.application.mapper.PortfolioMapper;
import com.cryptofolio.backend.application.port.in.CreatePortfolioInputPort;
import com.cryptofolio.backend.application.port.in.DeletePortfolioInputPort;
import com.cryptofolio.backend.application.port.in.GetPortfolioOverviewInputPort;
import com.cryptofolio.backend.application.port.in.GetPortfolioInputPort;
import com.cryptofolio.backend.application.port.in.GetPortfolioSummaryInputPort;
import com.cryptofolio.backend.application.port.in.ListUserPortfoliosInputPort;
import com.cryptofolio.backend.application.port.in.UpdatePortfolioInputPort;
import com.cryptofolio.backend.application.port.out.CryptoPriceProvider;
import com.cryptofolio.backend.application.port.out.PortfolioRepository;
import com.cryptofolio.backend.application.port.out.TransactionRepository;
import com.cryptofolio.backend.application.port.out.UserRepository;
import com.cryptofolio.backend.application.usecase.portfolio.CreatePortfolioUseCase;
import com.cryptofolio.backend.application.usecase.portfolio.DeletePortfolioUseCase;
import com.cryptofolio.backend.application.usecase.portfolio.GetPortfolioOverviewUseCase;
import com.cryptofolio.backend.application.usecase.portfolio.GetPortfolioSummaryUseCase;
import com.cryptofolio.backend.application.usecase.portfolio.GetPortfolioUseCase;
import com.cryptofolio.backend.application.usecase.portfolio.ListUserPortfoliosUseCase;
import com.cryptofolio.backend.application.usecase.portfolio.UpdatePortfolioUseCase;
import com.cryptofolio.backend.domain.service.PortfolioCalculator;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class PortfolioConfig {

    @Bean
    public PortfolioMapper portfolioMapper() {
        return Mappers.getMapper(PortfolioMapper.class);
    }

    @Bean
    public PortfolioCalculator portfolioCalculator() {
        return new PortfolioCalculator();
    }

    @Bean
    public CreatePortfolioInputPort createPortfolioInputPort(
            UserRepository userRepository,
            PortfolioRepository portfolioRepository,
            PortfolioMapper portfolioMapper,
            Clock clock) {
        return new CreatePortfolioUseCase(userRepository, portfolioRepository, portfolioMapper, clock);
    }

    @Bean
    public GetPortfolioInputPort getPortfolioInputPort(PortfolioRepository portfolioRepository, PortfolioMapper portfolioMapper) {
        return new GetPortfolioUseCase(portfolioRepository, portfolioMapper);
    }

    @Bean
    public ListUserPortfoliosInputPort listUserPortfoliosInputPort(
            PortfolioRepository portfolioRepository,
            PortfolioMapper portfolioMapper) {
        return new ListUserPortfoliosUseCase(portfolioRepository, portfolioMapper);
    }

    @Bean
    public UpdatePortfolioInputPort updatePortfolioInputPort(
            PortfolioRepository portfolioRepository,
            PortfolioMapper portfolioMapper) {
        return new UpdatePortfolioUseCase(portfolioRepository, portfolioMapper);
    }

    @Bean
    public DeletePortfolioInputPort deletePortfolioInputPort(
            PortfolioRepository portfolioRepository,
            TransactionRepository transactionRepository) {
        return new DeletePortfolioUseCase(portfolioRepository, transactionRepository);
    }

    @Bean
    public GetPortfolioSummaryInputPort getPortfolioSummaryInputPort(
            PortfolioRepository portfolioRepository,
            TransactionRepository transactionRepository,
            CryptoPriceProvider cryptoPriceProvider,
            PortfolioCalculator portfolioCalculator,
            PortfolioMapper portfolioMapper) {
        return new GetPortfolioSummaryUseCase(
                portfolioRepository,
                transactionRepository,
                cryptoPriceProvider,
                portfolioCalculator,
                portfolioMapper);
    }

    @Bean
    public GetPortfolioOverviewInputPort getPortfolioOverviewInputPort(
            PortfolioRepository portfolioRepository,
            TransactionRepository transactionRepository,
            CryptoPriceProvider cryptoPriceProvider,
            PortfolioCalculator portfolioCalculator,
            PortfolioMapper portfolioMapper) {
        return new GetPortfolioOverviewUseCase(
                portfolioRepository,
                transactionRepository,
                cryptoPriceProvider,
                portfolioCalculator,
                portfolioMapper);
    }
}
