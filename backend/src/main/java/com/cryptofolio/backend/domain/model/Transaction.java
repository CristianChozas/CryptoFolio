package com.cryptofolio.backend.domain.model;

import com.cryptofolio.backend.domain.valueobject.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public class Transaction {

    private final Long id;
    private final Long portfolioId;
    private final String crypto;
    private final TransactionType type;
    private final BigDecimal amount;
    private final BigDecimal pricePerUnit;
    private final Instant timestamp;

    public Transaction(Long id, Long portfolioId, String crypto, TransactionType type, BigDecimal amount,
            BigDecimal pricePerUnit, Instant timestamp) {
        this.id = id;
        this.portfolioId = requireNonNull(portfolioId, "portfolioId");
        this.crypto = requireNonNull(normalizeField(crypto), "crypto");
        this.type = requireNonNull(type, "type");
        this.amount = validatePositive(amount, "amount");
        this.pricePerUnit = validatePositive(pricePerUnit, "pricePerUnit");
        this.timestamp = requireNonNull(timestamp, "timestamp");
    }

    public Long getId() {
        return id;
    }

    public Long getPortfolioId() {
        return portfolioId;
    }

    public String getCrypto() {
        return crypto;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getPricePerUnit() {
        return pricePerUnit;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @SuppressWarnings("unchecked")
    private <T> T normalizeField(T value) {
        if (value instanceof String stringValue) {
            String trimmedValue = stringValue.trim();
            return (T) (trimmedValue.isEmpty() ? null : trimmedValue);
        }

        return value;
    }

    private <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }

        return value;
    }

    private BigDecimal validatePositive(BigDecimal value, String fieldName) {
        BigDecimal nonNullValue = requireNonNull(value, fieldName);

        if (nonNullValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero");
        }

        return nonNullValue;
    }
}
