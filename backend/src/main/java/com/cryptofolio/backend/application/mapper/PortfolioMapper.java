package com.cryptofolio.backend.application.mapper;

import com.cryptofolio.backend.application.dto.request.CreatePortfolioRequest;
import com.cryptofolio.backend.application.dto.response.PortfolioResponse;
import com.cryptofolio.backend.application.dto.response.PortfolioSummaryResponse;
import com.cryptofolio.backend.domain.model.Portfolio;
import com.cryptofolio.backend.domain.valueobject.Crypto;
import com.cryptofolio.backend.domain.valueobject.Money;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PortfolioMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "createdAt", source = "createdAt")
    Portfolio toPortfolio(CreatePortfolioRequest request, Long userId, Instant createdAt);

    PortfolioResponse toPortfolioResponse(Portfolio portfolio);

    @Mapping(target = "portfolio", source = "portfolio")
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "profitLossAmount", source = "profitLoss.amount")
    @Mapping(target = "profitLossCurrency", source = "profitLoss.currency")
    @Mapping(target = "roiPercentage", source = "roiPercentage")
    PortfolioSummaryResponse toPortfolioSummaryResponse(
            Portfolio portfolio,
            Map<Crypto, BigDecimal> balance,
            Money profitLoss,
            BigDecimal roiPercentage);

    default Map<String, BigDecimal> map(Map<Crypto, BigDecimal> balance) {
        if (balance == null) {
            return null;
        }

        Map<String, BigDecimal> balanceBySymbol = new LinkedHashMap<>();
        for (Map.Entry<Crypto, BigDecimal> entry : balance.entrySet()) {
            balanceBySymbol.put(entry.getKey().getSymbol(), entry.getValue());
        }

        return balanceBySymbol;
    }
}
