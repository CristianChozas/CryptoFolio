package com.cryptofolio.backend.domain.model;

import com.cryptofolio.backend.domain.valueobject.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionTest {

    private static final Instant TIMESTAMP = Instant.parse("2026-04-05T18:00:00Z");

    @Test
    void givenValidValues_whenCreatingTransaction_thenNormalizesCryptoAndStoresFields() {
        Transaction transaction = new Transaction(
                1L,
                15L,
                "  btc  ",
                TransactionType.BUY,
                new BigDecimal("0.10000000"),
                new BigDecimal("65000.00"),
                TIMESTAMP);

        assertEquals(15L, transaction.getPortfolioId());
        assertEquals("btc", transaction.getCrypto());
        assertEquals(TransactionType.BUY, transaction.getType());
        assertEquals(new BigDecimal("0.10000000"), transaction.getAmount());
        assertEquals(new BigDecimal("65000.00"), transaction.getPricePerUnit());
        assertEquals(TIMESTAMP, transaction.getTimestamp());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void givenBlankCrypto_whenCreatingTransaction_thenRejectsIt(String crypto) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Transaction(1L, 15L, crypto, TransactionType.BUY,
                        new BigDecimal("0.10000000"), new BigDecimal("65000.00"), TIMESTAMP));

        assertEquals("crypto cannot be null", exception.getMessage());
    }

    @Test
    void givenNullPortfolioId_whenCreatingTransaction_thenRejectsIt() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Transaction(1L, null, "BTC", TransactionType.BUY,
                        new BigDecimal("0.10000000"), new BigDecimal("65000.00"), TIMESTAMP));

        assertEquals("portfolioId cannot be null", exception.getMessage());
    }

    @Test
    void givenNonPositiveAmount_whenCreatingTransaction_thenRejectsIt() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Transaction(1L, 15L, "BTC", TransactionType.BUY,
                        BigDecimal.ZERO, new BigDecimal("65000.00"), TIMESTAMP));

        assertEquals("amount must be greater than zero", exception.getMessage());
    }

    @Test
    void givenNonPositivePricePerUnit_whenCreatingTransaction_thenRejectsIt() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Transaction(1L, 15L, "BTC", TransactionType.BUY,
                        new BigDecimal("0.10000000"), BigDecimal.ZERO, TIMESTAMP));

        assertEquals("pricePerUnit must be greater than zero", exception.getMessage());
    }

    @Test
    void givenNullTimestamp_whenCreatingTransaction_thenRejectsIt() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Transaction(1L, 15L, "BTC", TransactionType.BUY,
                        new BigDecimal("0.10000000"), new BigDecimal("65000.00"), null));

        assertEquals("timestamp cannot be null", exception.getMessage());
    }
}
