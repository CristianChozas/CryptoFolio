package com.cryptofolio.backend.infrastructure.out.persistence.adapter;

import com.cryptofolio.backend.application.port.out.TransactionRepository;
import com.cryptofolio.backend.domain.model.Transaction;
import com.cryptofolio.backend.infrastructure.out.persistence.entity.TransactionEntity;
import com.cryptofolio.backend.infrastructure.out.persistence.repository.JpaPortfolioRepository;
import com.cryptofolio.backend.infrastructure.out.persistence.repository.JpaTransactionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TransactionRepositoryAdapter implements TransactionRepository {

    private final JpaTransactionRepository jpaTransactionRepository;
    private final JpaPortfolioRepository jpaPortfolioRepository;

    public TransactionRepositoryAdapter(
            JpaTransactionRepository jpaTransactionRepository,
            JpaPortfolioRepository jpaPortfolioRepository) {
        this.jpaTransactionRepository = jpaTransactionRepository;
        this.jpaPortfolioRepository = jpaPortfolioRepository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity savedTransaction = jpaTransactionRepository.save(toEntity(transaction));
        return toDomain(savedTransaction);
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        return jpaTransactionRepository.findById(id)
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
        jpaTransactionRepository.deleteById(id);
    }

    private TransactionEntity toEntity(Transaction transaction) {
        return new TransactionEntity(
                transaction.getId(),
                transaction.getCrypto(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getPricePerUnit(),
                transaction.getTimestamp(),
                jpaPortfolioRepository.getReferenceById(transaction.getPortfolioId()));
    }

    private Transaction toDomain(TransactionEntity transactionEntity) {
        return new Transaction(
                transactionEntity.getId(),
                transactionEntity.getPortfolio().getId(),
                transactionEntity.getCrypto(),
                transactionEntity.getType(),
                transactionEntity.getAmount(),
                transactionEntity.getPricePerUnit(),
                transactionEntity.getTimestamp());
    }
}
