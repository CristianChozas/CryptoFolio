package com.cryptofolio.backend.domain.service;

import com.cryptofolio.backend.domain.model.Transaction;
import com.cryptofolio.backend.domain.valueobject.Crypto;
import com.cryptofolio.backend.domain.valueobject.Money;
import com.cryptofolio.backend.domain.valueobject.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PortfolioCalculatorTest {

    private static final Long PORTFOLIO_ID = 1L;
    private final PortfolioCalculator calculator = new PortfolioCalculator();

    @Test
    void shouldCalculateBalanceForBuyAndSellTransactions() {
        List<Transaction> transactions = List.of(
                transaction(1L, "BTC", TransactionType.BUY, "0.50", "30000"),
                transaction(2L, "BTC", TransactionType.SELL, "0.10", "35000"),
                transaction(3L, "ETH", TransactionType.BUY, "2.00", "2000"));

        Map<Crypto, BigDecimal> balance = calculator.calculateBalance(transactions);

        assertEquals(new BigDecimal("0.40"), balance.get(Crypto.from("BTC")));
        assertEquals(new BigDecimal("2.00"), balance.get(Crypto.from("ETH")));
    }

    @Test
    void shouldCalculateProfitLossWhenOnlyBuys() {
        List<Transaction> transactions = List.of(
                transaction(1L, "BTC", TransactionType.BUY, "1.00", "30000"));

        Map<Crypto, BigDecimal> currentPrices = Map.of(
                Crypto.from("BTC"), new BigDecimal("35000"));

        Money profitLoss = calculator.calculateProfitLoss(transactions, currentPrices);

        assertEquals(new BigDecimal("5000.00"), profitLoss.getAmount());
        assertEquals("USD", profitLoss.getCurrency());
    }

    @Test
    void shouldCalculateProfitLossWithBuyAndSellTransactions() {
        List<Transaction> transactions = List.of(
                transaction(1L, "BTC", TransactionType.BUY, "1.00", "30000"),
                transaction(2L, "BTC", TransactionType.SELL, "0.40", "35000"));

        Map<Crypto, BigDecimal> currentPrices = Map.of(
                Crypto.from("BTC"), new BigDecimal("34000"));

        Money profitLoss = calculator.calculateProfitLoss(transactions, currentPrices);

        assertEquals(new BigDecimal("4400.00"), profitLoss.getAmount());
    }

    @Test
    void shouldCalculateProfitLossWhenPortfolioHasLosses() {
        List<Transaction> transactions = List.of(
                transaction(1L, "BTC", TransactionType.BUY, "1.00", "50000"));

        Map<Crypto, BigDecimal> currentPrices = Map.of(
                Crypto.from("BTC"), new BigDecimal("42000"));

        Money profitLoss = calculator.calculateProfitLoss(transactions, currentPrices);

        assertEquals(new BigDecimal("-8000.00"), profitLoss.getAmount());
    }

    @Test
    void shouldCalculateRoiPercentage() {
        List<Transaction> transactions = List.of(
                transaction(1L, "BTC", TransactionType.BUY, "1.00", "30000"),
                transaction(2L, "BTC", TransactionType.SELL, "0.40", "35000"));

        Map<Crypto, BigDecimal> currentPrices = Map.of(
                Crypto.from("BTC"), new BigDecimal("34000"));

        BigDecimal roi = calculator.calculateROI(transactions, currentPrices);

        assertEquals(new BigDecimal("14.67"), roi);
    }

    @Test
    void shouldReturnZeroRoiWhenNoBuyTransactionsExist() {
        List<Transaction> transactions = List.of(
                transaction(1L, "BTC", TransactionType.SELL, "0.40", "35000"));

        Map<Crypto, BigDecimal> currentPrices = Map.of(
                Crypto.from("BTC"), new BigDecimal("34000"));

        BigDecimal roi = calculator.calculateROI(transactions, currentPrices);

        assertEquals(BigDecimal.ZERO, roi);
    }

    @Test
    void shouldRejectMissingCurrentPrice() {
        List<Transaction> transactions = List.of(
                transaction(1L, "BTC", TransactionType.BUY, "1.00", "30000"));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculateProfitLoss(transactions, Map.of()));

        assertEquals("current price for BTC cannot be null", ex.getMessage());
    }

    private Transaction transaction(Long id, String crypto, TransactionType type, String amount, String pricePerUnit) {
        return new Transaction(
                id,
                PORTFOLIO_ID,
                crypto,
                type,
                new BigDecimal(amount),
                new BigDecimal(pricePerUnit),
                Instant.parse("2026-01-15T10:30:00Z"));
    }
}
