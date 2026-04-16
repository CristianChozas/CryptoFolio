package com.cryptofolio.backend.application.usecase.portfolio;

import com.cryptofolio.backend.application.dto.response.PortfolioOverviewItemResponse;
import com.cryptofolio.backend.application.dto.response.PortfolioOverviewOperationResponse;
import com.cryptofolio.backend.application.dto.response.PortfolioOverviewResponse;
import com.cryptofolio.backend.application.dto.response.PortfolioResponse;
import com.cryptofolio.backend.application.mapper.PortfolioMapper;
import com.cryptofolio.backend.application.port.in.GetPortfolioOverviewInputPort;
import com.cryptofolio.backend.application.port.out.CryptoPriceProvider;
import com.cryptofolio.backend.application.port.out.PortfolioRepository;
import com.cryptofolio.backend.application.port.out.TransactionRepository;
import com.cryptofolio.backend.domain.model.Portfolio;
import com.cryptofolio.backend.domain.model.Transaction;
import com.cryptofolio.backend.domain.service.PortfolioCalculator;
import com.cryptofolio.backend.domain.valueobject.Crypto;
import com.cryptofolio.backend.domain.valueobject.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GetPortfolioOverviewUseCase implements GetPortfolioOverviewInputPort {

    private static final Logger log = LoggerFactory.getLogger(GetPortfolioOverviewUseCase.class);

    private static final String USD = "USD";
    private static final int RECENT_OPERATIONS_LIMIT = 5;

    private final PortfolioRepository portfolioRepository;
    private final TransactionRepository transactionRepository;
    private final CryptoPriceProvider cryptoPriceProvider;
    private final PortfolioCalculator portfolioCalculator;
    private final PortfolioMapper portfolioMapper;

    public GetPortfolioOverviewUseCase(
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
    public PortfolioOverviewResponse execute(Long userId) {
        Long nonNullUserId = Objects.requireNonNull(userId, "userId cannot be null");
        List<Portfolio> portfolios = portfolioRepository.findByUserId(nonNullUserId);
        log.info("[PortfolioOverview] building overview for userId={} portfolioCount={}", nonNullUserId, portfolios.size());

        List<PortfolioOverviewItemResponse> overviewItems = new ArrayList<>();
        List<PortfolioOverviewOperationResponse> recentOperations = new ArrayList<>();
        BigDecimal totalCurrentValue = BigDecimal.ZERO;
        BigDecimal totalProfitLoss = BigDecimal.ZERO;

        for (Portfolio portfolio : portfolios) {
            List<Transaction> transactions = transactionRepository.findByPortfolioId(portfolio.getId());
            Map<Crypto, BigDecimal> balance = portfolioCalculator.calculateBalance(transactions);
            Map<Crypto, BigDecimal> currentPrices = resolveCurrentPrices(balance.keySet());
            BigDecimal currentValue = portfolioCalculator.calculateCurrentValue(transactions, currentPrices);
            Money profitLoss = portfolioCalculator.calculateProfitLoss(transactions, currentPrices);
            BigDecimal roiPercentage = portfolioCalculator.calculateROI(transactions, currentPrices);
            PortfolioResponse portfolioResponse = portfolioMapper.toPortfolioResponse(portfolio);

            overviewItems.add(new PortfolioOverviewItemResponse(
                    portfolioResponse,
                    portfolioMapper.map(balance),
                    currentValue,
                    USD,
                    profitLoss.getAmount(),
                    profitLoss.getCurrency(),
                    roiPercentage));

            totalCurrentValue = totalCurrentValue.add(currentValue);
            totalProfitLoss = totalProfitLoss.add(profitLoss.getAmount());

            for (Transaction transaction : transactions) {
                recentOperations.add(new PortfolioOverviewOperationResponse(
                        transaction.getId(),
                        portfolio.getId(),
                        portfolio.getName(),
                        transaction.getCrypto(),
                        transaction.getType().name(),
                        transaction.getAmount(),
                        transaction.getPricePerUnit(),
                        transaction.getTimestamp()));
            }
        }

        List<PortfolioOverviewOperationResponse> sortedRecentOperations = recentOperations.stream()
                .sorted(Comparator.comparing(PortfolioOverviewOperationResponse::getTimestamp).reversed())
                .limit(RECENT_OPERATIONS_LIMIT)
                .toList();

        return new PortfolioOverviewResponse(
                portfolios.size(),
                totalCurrentValue,
                USD,
                totalProfitLoss,
                USD,
                overviewItems,
                sortedRecentOperations);
    }

    private Map<Crypto, BigDecimal> resolveCurrentPrices(Iterable<Crypto> cryptos) {
        Map<Crypto, BigDecimal> currentPrices = new HashMap<>();
        for (Crypto crypto : cryptos) {
            try {
                currentPrices.put(crypto, cryptoPriceProvider.getCurrentPrice(crypto));
            } catch (RuntimeException exception) {
                log.warn("[PortfolioOverview] fallback price=0 for crypto={} reason={}", crypto.getSymbol(), exception.getMessage());
                currentPrices.put(crypto, BigDecimal.ZERO);
            }
        }
        return currentPrices;
    }
}
