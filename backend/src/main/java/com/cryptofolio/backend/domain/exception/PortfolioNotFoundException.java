package com.cryptofolio.backend.domain.exception;

public class PortfolioNotFoundException extends RuntimeException {

    public PortfolioNotFoundException(Long portfolioId) {
        super("Portfolio not found with id: " + portfolioId);
    }
}
