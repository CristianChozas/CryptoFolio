package com.cryptofolio.backend.application.port.out;

import com.cryptofolio.backend.domain.model.Transaction;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    Optional<Transaction> findById(Long id);

    List<Transaction> findByPortfolioId(Long portfolioId);

    void deleteById(Long id);
}
