package com.cryptofolio.backend.domain.exception;

public class UnauthorizedPortfolioAccessException extends RuntimeException {

    public UnauthorizedPortfolioAccessException(Long portfolioId, Long userId) {
        super("User " + userId + " is not authorized to access portfolio: " + portfolioId);
    }
}
