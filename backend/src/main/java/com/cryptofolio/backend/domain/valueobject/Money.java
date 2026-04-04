package com.cryptofolio.backend.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Money {

    private final BigDecimal amount;
    private final String currency;

    private Money(BigDecimal amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public static Money of(BigDecimal rawAmount, String rawCurrency) {
        BigDecimal normalizedAmount = requireNonNull(rawAmount, "amount");
        if (normalizedAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("amount cannot be negative");
        }

        String normalizedCurrency = normalizeField(rawCurrency);
        requireNonNull(normalizedCurrency, "currency");

        if (!normalizedCurrency.matches("^[A-Z]{3}$")) {
            throw new IllegalArgumentException("currency must be a 3-letter ISO code");
        }

        return new Money(normalizedAmount.setScale(2, RoundingMode.HALF_UP), normalizedCurrency);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public Money add(Money other) {
        Money nonNullOther = requireNonNull(other, "other");
        validateSameCurrency(nonNullOther);
        return Money.of(amount.add(nonNullOther.amount), currency);
    }

    public Money subtract(Money other) {
        Money nonNullOther = requireNonNull(other, "other");
        validateSameCurrency(nonNullOther);

        BigDecimal result = amount.subtract(nonNullOther.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("resulting amount cannot be negative");
        }

        return Money.of(result, currency);
    }

    private void validateSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("currencies must match");
        }
    }

    private static String normalizeField(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim().toUpperCase();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    private static <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }

        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Money other)) {
            return false;
        }

        return amount.equals(other.amount) && currency.equals(other.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return amount + " " + currency;
    }
}
