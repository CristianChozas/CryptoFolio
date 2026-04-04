package com.cryptofolio.backend.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Crypto tests")
class CryptoTest {

    @ParameterizedTest
    @ValueSource(strings = {"BTC", "eth", "  xRp  ", "ADA123"})
    void shouldCreateValidCryptoWithNormalization(String rawValue) {
        Crypto crypto = Crypto.from(rawValue);

        String expectedSymbol = rawValue.trim().toUpperCase();
        assertEquals(expectedSymbol, crypto.getSymbol());
        assertEquals(expectedSymbol, crypto.toString());
    }

    @Test
    void shouldTreatValueObjectEqualityBySymbol() {
        Crypto first = Crypto.from("btc");
        Crypto second = Crypto.from("  BTC  ");
        Crypto different = Crypto.from("ETH");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, different);
    }

    @Test
    void shouldRejectNullSymbol() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Crypto.from(null));

        assertEquals("symbol cannot be null", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void shouldRejectBlankSymbol(String rawValue) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Crypto.from(rawValue));

        assertEquals("symbol cannot be null", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "A",            // too short
            "ABCDEFGHIJK",  // too long
            "BTC@",         // invalid char
            "BIT COIN",     // inner whitespace
            "BTC-USD"       // hyphen
    })
    void shouldRejectInvalidSymbolFormat(String rawValue) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Crypto.from(rawValue));

        assertEquals("symbol must contain 2-10 uppercase letters or numbers", exception.getMessage());
    }
}
