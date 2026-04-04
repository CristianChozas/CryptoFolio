package com.cryptofolio.backend.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("TransactionType tests")
class TransactionTypeTest {

    @ParameterizedTest
    @ValueSource(strings = {"BUY", "buy", "  Buy  "})
    void shouldParseBuy(String rawValue) {
        TransactionType type = TransactionType.from(rawValue);

        assertSame(TransactionType.BUY, type);
        assertTrue(type.isBuy());
        assertFalse(type.isSell());
    }

    @ParameterizedTest
    @ValueSource(strings = {"SELL", "sell", "  SeLl  "})
    void shouldParseSell(String rawValue) {
        TransactionType type = TransactionType.from(rawValue);

        assertSame(TransactionType.SELL, type);
        assertTrue(type.isSell());
        assertFalse(type.isBuy());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void shouldRejectBlankInput(String rawValue) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TransactionType.from(rawValue));

        assertEquals("transactionType cannot be null or blank", exception.getMessage());
    }

    @Test
    void shouldRejectNullInput() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TransactionType.from(null));

        assertEquals("transactionType cannot be null or blank", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"TRANSFER", "BUY SELL", "1", "BUYS"})
    void shouldRejectInvalidValues(String rawValue) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TransactionType.from(rawValue));

        assertEquals("transactionType must be BUY or SELL", exception.getMessage());
    }
}
