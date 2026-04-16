package com.cryptofolio.backend.infrastructure.out.persistence.adapter;

import com.cryptofolio.backend.application.port.out.TransactionRepository;
import com.cryptofolio.backend.domain.model.Transaction;
import com.cryptofolio.backend.infrastructure.out.persistence.entity.PortfolioEntity;
import com.cryptofolio.backend.infrastructure.out.persistence.entity.TransactionEntity;
import com.cryptofolio.backend.infrastructure.out.persistence.repository.JpaPortfolioRepository;
import com.cryptofolio.backend.infrastructure.out.persistence.repository.JpaTransactionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class TransactionRepositoryAdapter implements TransactionRepository {

    private final JpaTransactionRepository jpaTransactionRepository;
    private final JpaPortfolioRepository jpaPortfolioRepository;

    public TransactionRepositoryAdapter(
            JpaTransactionRepository jpaTransactionRepository,
            JpaPortfolioRepository jpaPortfolioRepository) {
        this.jpaTransactionRepository = Objects.requireNonNull(
                jpaTransactionRepository,
                "jpaTransactionRepository cannot be null");
        this.jpaPortfolioRepository = Objects.requireNonNull(jpaPortfolioRepository, "jpaPortfolioRepository cannot be null");
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity savedTransaction = jpaTransactionRepository.save(toEntity(transaction));
        return toDomain(savedTransaction);
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        Long nonNullId = Objects.requireNonNull(id, "transaction id cannot be null");
        return jpaTransactionRepository.findById(nonNullId)
                .map(this::toDomain);
    }

    @Override
    public List<Transaction> findByPortfolioId(Long portfolioId) {
        return jpaTransactionRepository.findByPortfolioId(portfolioId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        Long nonNullId = Objects.requireNonNull(id, "transaction id cannot be null");
        jpaTransactionRepository.deleteById(nonNullId);
    }

    private TransactionEntity toEntity(Transaction transaction) {
        Long portfolioId = Objects.requireNonNull(transaction.getPortfolioId(), "transaction portfolio id cannot be null");
        return new TransactionEntity(
                transaction.getId(),
                transaction.getCrypto(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getPricePerUnit(),
                transaction.getTimestamp(),
                jpaPortfolioRepository.getReferenceById(portfolioId));
    }

    private Transaction toDomain(TransactionEntity transactionEntity) {
        PortfolioEntity portfolio = Objects.requireNonNull(
                transactionEntity.getPortfolio(),
                "transaction portfolio cannot be null");
        return new Transaction(
                transactionEntity.getId(),
                Objects.requireNonNull(portfolio.getId(), "transaction portfolio id cannot be null"),
                Objects.requireNonNull(transactionEntity.getCrypto(), "transaction crypto cannot be null"),
                Objects.requireNonNull(transactionEntity.getType(), "transaction type cannot be null"),
                Objects.requireNonNull(transactionEntity.getAmount(), "transaction amount cannot be null"),
                Objects.requireNonNull(transactionEntity.getPricePerUnit(), "transaction pricePerUnit cannot be null"),
                Objects.requireNonNull(transactionEntity.getTimestamp(), "transaction timestamp cannot be null"));
    }
}
