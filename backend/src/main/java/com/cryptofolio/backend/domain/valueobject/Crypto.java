package com.cryptofolio.backend.domain.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

public final class Crypto {

    private static final Pattern SYMBOL_PATTERN = Pattern.compile("^[A-Z0-9]{2,10}$");

    private final String symbol;

    private Crypto(String symbol) {
        this.symbol = symbol;
    }

    public static Crypto from(String rawValue) {
        String normalizedSymbol = normalizeField(rawValue);
        requireNonNull(normalizedSymbol, "symbol");

        if (!SYMBOL_PATTERN.matcher(normalizedSymbol).matches()) {
            throw new IllegalArgumentException("symbol must contain 2-10 uppercase letters or numbers");
        }

        return new Crypto(normalizedSymbol);
    }

    public String getSymbol() {
        return symbol;
    }

    private static String normalizeField(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim().toUpperCase();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    private static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Crypto other)) {
            return false;
        }

        return symbol.equals(other.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol);
    }

    @Override
    public String toString() {
        return symbol;
    }
}
