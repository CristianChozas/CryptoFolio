package com.cryptofolio.backend.application.port.in;

public interface DeletePortfolioInputPort {

    void execute(Long userId, Long portfolioId);
}
