package com.cryptofolio.backend.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class DomainExceptionsTest {

    @Test
    void shouldCreateUserNotFoundExceptionWithDescriptiveMessage() {
        UserNotFoundException exception = new UserNotFoundException(42L);

        assertInstanceOf(RuntimeException.class, exception);
        assertEquals("User not found with id: 42", exception.getMessage());
    }

    @Test
    void shouldCreatePortfolioNotFoundExceptionWithDescriptiveMessage() {
        PortfolioNotFoundException exception = new PortfolioNotFoundException(24L);

        assertInstanceOf(RuntimeException.class, exception);
        assertEquals("Portfolio not found with id: 24", exception.getMessage());
    }

    @Test
    void shouldCreateTransactionNotFoundExceptionWithDescriptiveMessage() {
        TransactionNotFoundException exception = new TransactionNotFoundException(15L);

        assertInstanceOf(RuntimeException.class, exception);
        assertEquals("Transaction not found with id: 15", exception.getMessage());
    }

    @Test
    void shouldCreateUnauthorizedPortfolioAccessExceptionWithDescriptiveMessage() {
        UnauthorizedPortfolioAccessException exception = new UnauthorizedPortfolioAccessException(7L, 99L);

        assertInstanceOf(RuntimeException.class, exception);
        assertEquals("User 99 is not authorized to access portfolio: 7", exception.getMessage());
    }

    @Test
    void shouldCreateInvalidTransactionExceptionWithProvidedMessage() {
        InvalidTransactionException exception = new InvalidTransactionException("transaction is invalid");

        assertInstanceOf(RuntimeException.class, exception);
        assertEquals("transaction is invalid", exception.getMessage());
    }

    @Test
    void shouldCreateInsufficientFundsExceptionWithDescriptiveMessage() {
        InsufficientFundsException exception = new InsufficientFundsException("BTC", "0.25", "0.50");

        assertInstanceOf(RuntimeException.class, exception);
        assertEquals("Insufficient funds for BTC. Available: 0.25, requested: 0.50", exception.getMessage());
    }
}
