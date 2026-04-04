package com.cryptofolio.backend.domain.valueobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {

    @Test
    void shouldCreateValidMoney() {
        Money money = Money.of(new BigDecimal("10"), "usd");

        assertEquals(new BigDecimal("10.00"), money.getAmount());
        assertEquals("USD", money.getCurrency());
    }

    @Test
    void shouldRejectNullAmount() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Money.of(null, "USD"));

        assertEquals("amount cannot be null", exception.getMessage());
    }

    @Test
    void shouldRejectNegativeAmount() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Money.of(new BigDecimal("-1"), "USD"));

        assertEquals("amount cannot be negative", exception.getMessage());
    }

    @Test
    void shouldAllowNegativeAmountWhenUsingSignedFactory() {
        Money money = Money.signed(new BigDecimal("-1.50"), "usd");

        assertEquals(new BigDecimal("-1.50"), money.getAmount());
        assertEquals("USD", money.getCurrency());
    }

    @Test
    void shouldRejectNullCurrency() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Money.of(new BigDecimal("10"), null));

        assertEquals("currency cannot be null", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "   " })
    void shouldRejectBlankCurrency(String currency) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Money.of(new BigDecimal("10"), currency));

        assertEquals("currency cannot be null", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = { "US", "USDD", "US1", "U$D" })
    void shouldRejectInvalidCurrencyFormat(String currency) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Money.of(new BigDecimal("10"), currency));

        assertEquals("currency must be a 3-letter ISO code", exception.getMessage());
    }

    @Test
    void shouldAddMoneyWithSameCurrency() {
        Money left = Money.of(new BigDecimal("10.10"), "USD");
        Money right = Money.of(new BigDecimal("5.20"), "USD");

        Money result = left.add(right);

        assertEquals(new BigDecimal("15.30"), result.getAmount());
        assertEquals("USD", result.getCurrency());
    }

    @Test
    void shouldRejectAddWithDifferentCurrency() {
        Money left = Money.of(new BigDecimal("10"), "USD");
        Money right = Money.of(new BigDecimal("5"), "EUR");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> left.add(right));

        assertEquals("currencies must match", exception.getMessage());
    }

    @Test
    void shouldSubtractMoneyWithSameCurrency() {
        Money left = Money.of(new BigDecimal("10.50"), "USD");
        Money right = Money.of(new BigDecimal("3.25"), "USD");

        Money result = left.subtract(right);

        assertEquals(new BigDecimal("7.25"), result.getAmount());
        assertEquals("USD", result.getCurrency());
    }

    @Test
    void shouldRejectNegativeResultOnSubtract() {
        Money left = Money.of(new BigDecimal("2"), "USD");
        Money right = Money.of(new BigDecimal("3"), "USD");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> left.subtract(right));

        assertEquals("resulting amount cannot be negative", exception.getMessage());
    }

    @Test
    void shouldBehaveAsValueObject() {
        Money first = Money.of(new BigDecimal("10"), "usd");
        Money second = Money.of(new BigDecimal("10.00"), "USD");
        Money different = Money.of(new BigDecimal("11"), "USD");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, different);
    }
}
