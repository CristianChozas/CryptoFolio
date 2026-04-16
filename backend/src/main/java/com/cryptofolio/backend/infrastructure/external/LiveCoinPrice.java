package com.cryptofolio.backend.infrastructure.external;

import java.math.BigDecimal;

public record LiveCoinPrice(
        String symbol,
        String name,
        BigDecimal eurPrice,
        BigDecimal change24hPercent) {
}
