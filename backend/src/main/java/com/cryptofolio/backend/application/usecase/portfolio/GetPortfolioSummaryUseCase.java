package com.cryptofolio.backend.application.usecase.portfolio;

import com.cryptofolio.backend.application.dto.response.PortfolioSummaryResponse;
import com.cryptofolio.backend.application.mapper.PortfolioMapper;
import com.cryptofolio.backend.application.port.in.GetPortfolioSummaryInputPort;
import com.cryptofolio.backend.application.port.out.CryptoPriceProvider;
import com.cryptofolio.backend.application.port.out.PortfolioRepository;
import com.cryptofolio.backend.application.port.out.TransactionRepository;
import com.cryptofolio.backend.domain.exception.PortfolioNotFoundException;
import com.cryptofolio.backend.domain.exception.UnauthorizedPortfolioAccessException;
import com.cryptofolio.backend.domain.model.Portfolio;
import com.cryptofolio.backend.domain.model.Transaction;
import com.cryptofolio.backend.domain.service.PortfolioCalculator;
import com.cryptofolio.backend.domain.valueobject.Crypto;
import com.cryptofolio.backend.domain.valueobject.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GetPortfolioSummaryUseCase implements GetPortfolioSummaryInputPort {

    private static final Logger log = LoggerFactory.getLogger(GetPortfolioSummaryUseCase.class);

    private final PortfolioRepository portfolioRepository;
    private final TransactionRepository transactionRepository;
    private final CryptoPriceProvider cryptoPriceProvider;
    private final PortfolioCalculator portfolioCalculator;
    private final PortfolioMapper portfolioMapper;

    public GetPortfolioSummaryUseCase(
            PortfolioRepository portfolioRepository,
            TransactionRepository transactionRepository,
            CryptoPriceProvider cryptoPriceProvider,
            PortfolioCalculator portfolioCalculator,
            PortfolioMapper portfolioMapper) {
        this.portfolioRepository = Objects.requireNonNull(portfolioRepository, "portfolioRepository cannot be null");
        this.transactionRepository = Objects.requireNonNull(transactionRepository, "transactionRepository cannot be null");
        this.cryptoPriceProvider = Objects.requireNonNull(cryptoPriceProvider, "cryptoPriceProvider cannot be null");
        this.portfolioCalculator = Objects.requireNonNull(portfolioCalculator, "portfolioCalculator cannot be null");
        this.portfolioMapper = Objects.requireNonNull(portfolioMapper, "portfolioMapper cannot be null");
    }

    @Override
    public PortfolioSummaryResponse execute(Long userId, Long portfolioId) {
        Long nonNullUserId = Objects.requireNonNull(userId, "userId cannot be null");
        Long nonNullPortfolioId = Objects.requireNonNull(portfolioId, "portfolioId cannot be null");

        Portfolio portfolio = portfolioRepository.findById(nonNullPortfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException(nonNullPortfolioId));

        if (!portfolio.getUserId().equals(nonNullUserId)) {
            throw new UnauthorizedPortfolioAccessException(nonNullPortfolioId, nonNullUserId);
        }

        List<Transaction> transactions = transactionRepository.findByPortfolioId(nonNullPortfolioId);
        Map<Crypto, BigDecimal> balance = portfolioCalculator.calculateBalance(transactions);
        Map<Crypto, BigDecimal> currentPrices = resolveCurrentPrices(balance.keySet());
        Money profitLoss = portfolioCalculator.calculateProfitLoss(transactions, currentPrices);
        BigDecimal roiPercentage = portfolioCalculator.calculateROI(transactions, currentPrices);

        return portfolioMapper.toPortfolioSummaryResponse(portfolio, balance, profitLoss, roiPercentage);
    }

    private Map<Crypto, BigDecimal> resolveCurrentPrices(Iterable<Crypto> cryptos) {
        Map<Crypto, BigDecimal> currentPrices = new HashMap<>();
        for (Crypto crypto : cryptos) {
            try {
                currentPrices.put(crypto, cryptoPriceProvider.getCurrentPrice(crypto));
            } catch (RuntimeException exception) {
                log.warn("[PortfolioSummary] fallback price=0 for crypto={} reason={}", crypto.getSymbol(), exception.getMessage());
                currentPrices.put(crypto, BigDecimal.ZERO);
            }
        }
        return currentPrices;
    }
}
