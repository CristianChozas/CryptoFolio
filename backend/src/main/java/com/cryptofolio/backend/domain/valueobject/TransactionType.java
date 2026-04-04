package com.cryptofolio.backend.domain.valueobject;

public enum TransactionType {
    BUY,
    SELL;

    public static TransactionType from(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException("transactionType cannot be null or blank");
        }

        String normalizedValue = rawValue.trim().toUpperCase();

        try {
            return TransactionType.valueOf(normalizedValue);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("transactionType must be BUY or SELL");
        }
    }

    public boolean isBuy() {
        return this == BUY;
    }

    public boolean isSell() {
        return this == SELL;
    }
}
