package com.cryptofolio.backend.infrastructure.in.web.response;

import java.math.BigDecimal;

public record CoinPriceResponse(
        String symbol,
        String name,
        BigDecimal eurPrice,
        BigDecimal change24hPercent
) {}
