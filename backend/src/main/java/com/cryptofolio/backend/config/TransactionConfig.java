package com.cryptofolio.backend.config;

import com.cryptofolio.backend.application.mapper.TransactionMapper;
import com.cryptofolio.backend.application.port.in.AddTransactionInputPort;
import com.cryptofolio.backend.application.port.in.DeleteTransactionInputPort;
import com.cryptofolio.backend.application.port.in.GetTransactionHistoryInputPort;
import com.cryptofolio.backend.application.port.out.PortfolioRepository;
import com.cryptofolio.backend.application.port.out.TransactionRepository;
import com.cryptofolio.backend.application.usecase.transaction.AddTransactionUseCase;
import com.cryptofolio.backend.application.usecase.transaction.DeleteTransactionUseCase;
import com.cryptofolio.backend.application.usecase.transaction.GetTransactionHistoryUseCase;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class TransactionConfig {

    @Bean
    public TransactionMapper transactionMapper() {
        return Mappers.getMapper(TransactionMapper.class);
    }

    @Bean
    public AddTransactionInputPort addTransactionInputPort(
            PortfolioRepository portfolioRepository,
            TransactionRepository transactionRepository,
            TransactionMapper transactionMapper,
            Clock clock) {
        return new AddTransactionUseCase(
                portfolioRepository,
                transactionRepository,
                transactionMapper,
                clock);
    }

    @Bean
    public GetTransactionHistoryInputPort getTransactionHistoryInputPort(
            PortfolioRepository portfolioRepository,
            TransactionRepository transactionRepository,
            TransactionMapper transactionMapper) {
        return new GetTransactionHistoryUseCase(portfolioRepository, transactionRepository, transactionMapper);
    }

    @Bean
    public DeleteTransactionInputPort deleteTransactionInputPort(
            TransactionRepository transactionRepository,
            PortfolioRepository portfolioRepository) {
        return new DeleteTransactionUseCase(transactionRepository, portfolioRepository);
    }
}
