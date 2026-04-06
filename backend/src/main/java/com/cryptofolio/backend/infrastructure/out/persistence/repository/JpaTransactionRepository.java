package com.cryptofolio.backend.infrastructure.out.persistence.repository;

import com.cryptofolio.backend.infrastructure.out.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaTransactionRepository extends JpaRepository<TransactionEntity, Long> {

    List<TransactionEntity> findByPortfolioId(Long portfolioId);
}
