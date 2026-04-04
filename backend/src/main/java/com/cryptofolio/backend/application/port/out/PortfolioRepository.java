package com.cryptofolio.backend.application.port.out;

import com.cryptofolio.backend.domain.model.Portfolio;

import java.util.List;
import java.util.Optional;

public interface PortfolioRepository {

    Portfolio save(Portfolio portfolio);

    Optional<Portfolio> findById(Long id);

    List<Portfolio> findByUserId(Long userId);

    void deleteById(Long id);
}
