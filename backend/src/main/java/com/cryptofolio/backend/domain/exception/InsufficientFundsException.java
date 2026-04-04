package com.cryptofolio.backend.domain.exception;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(String crypto, String availableAmount, String requestedAmount) {
        super("Insufficient funds for " + crypto + ". Available: " + availableAmount + ", requested: "
                + requestedAmount);
    }
}
