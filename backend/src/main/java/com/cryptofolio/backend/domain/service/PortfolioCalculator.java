package com.cryptofolio.backend.domain.service;

import com.cryptofolio.backend.domain.model.Transaction;
import com.cryptofolio.backend.domain.valueobject.Crypto;
import com.cryptofolio.backend.domain.valueobject.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PortfolioCalculator {

    private static final String USD = "USD";

    public Map<Crypto, BigDecimal> calculateBalance(List<Transaction> transactions) {
        List<Transaction> nonNullTransactions = requireNonNull(transactions, "transactions");

        Map<Crypto, BigDecimal> balanceByCrypto = new HashMap<>();
        for (Transaction transaction : nonNullTransactions) {
            Transaction nonNullTransaction = requireNonNull(transaction, "transaction");

            Crypto crypto = Crypto.from(nonNullTransaction.getCrypto());
            BigDecimal signedAmount = nonNullTransaction.getType().isBuy()
                    ? nonNullTransaction.getAmount()
                    : nonNullTransaction.getAmount().negate();

            balanceByCrypto.merge(crypto, signedAmount, BigDecimal::add);
        }

        return Map.copyOf(balanceByCrypto);
    }

    public Money calculateProfitLoss(List<Transaction> transactions, Map<Crypto, BigDecimal> currentPrices) {
        List<Transaction> nonNullTransactions = requireNonNull(transactions, "transactions");
        Map<Crypto, BigDecimal> nonNullCurrentPrices = requireNonNull(currentPrices, "currentPrices");

        BigDecimal totalBuyCost = BigDecimal.ZERO;
        BigDecimal totalSellProceeds = BigDecimal.ZERO;

        for (Transaction transaction : nonNullTransactions) {
            Transaction nonNullTransaction = requireNonNull(transaction, "transaction");
            BigDecimal grossValue = nonNullTransaction.getAmount().multiply(nonNullTransaction.getPricePerUnit());

            if (nonNullTransaction.getType().isBuy()) {
                totalBuyCost = totalBuyCost.add(grossValue);
            } else {
                totalSellProceeds = totalSellProceeds.add(grossValue);
            }
        }

        BigDecimal currentPortfolioValue = calculateCurrentValue(nonNullTransactions, nonNullCurrentPrices);
        BigDecimal profitLossAmount = currentPortfolioValue
                .add(totalSellProceeds)
                .subtract(totalBuyCost);

        return Money.signed(profitLossAmount, USD);
    }

    public BigDecimal calculateROI(List<Transaction> transactions, Map<Crypto, BigDecimal> currentPrices) {
        List<Transaction> nonNullTransactions = requireNonNull(transactions, "transactions");

        BigDecimal totalBuyCost = nonNullTransactions.stream()
                .map(this::requireTransaction)
                .filter(transaction -> transaction.getType().isBuy())
                .map(transaction -> transaction.getAmount().multiply(transaction.getPricePerUnit()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalBuyCost.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal profitLossAmount = calculateProfitLoss(nonNullTransactions, currentPrices).getAmount();
        return profitLossAmount
                .divide(totalBuyCost, 6, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateCurrentValue(List<Transaction> transactions, Map<Crypto, BigDecimal> currentPrices) {
        List<Transaction> nonNullTransactions = requireNonNull(transactions, "transactions");
        Map<Crypto, BigDecimal> nonNullCurrentPrices = requireNonNull(currentPrices, "currentPrices");

        return calculateCurrentValue(calculateBalance(nonNullTransactions), nonNullCurrentPrices);
    }

    private BigDecimal calculateCurrentValue(Map<Crypto, BigDecimal> balanceByCrypto, Map<Crypto, BigDecimal> currentPrices) {
        BigDecimal currentValue = BigDecimal.ZERO;

        for (Map.Entry<Crypto, BigDecimal> entry : balanceByCrypto.entrySet()) {
            Crypto crypto = entry.getKey();
            BigDecimal balanceAmount = entry.getValue();
            BigDecimal price = requireNonNull(currentPrices.get(crypto), "current price for " + crypto.getSymbol());

            currentValue = currentValue.add(balanceAmount.multiply(price));
        }

        return currentValue;
    }

    private Transaction requireTransaction(Transaction transaction) {
        return requireNonNull(transaction, "transaction");
    }

    private <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }

        return value;
    }
}
