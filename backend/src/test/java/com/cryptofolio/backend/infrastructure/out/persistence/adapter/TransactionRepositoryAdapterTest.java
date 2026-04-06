package com.cryptofolio.backend.infrastructure.out.persistence.adapter;

import com.cryptofolio.backend.domain.model.Transaction;
import com.cryptofolio.backend.domain.valueobject.TransactionType;
import com.cryptofolio.backend.infrastructure.out.persistence.entity.PortfolioEntity;
import com.cryptofolio.backend.infrastructure.out.persistence.entity.TransactionEntity;
import com.cryptofolio.backend.infrastructure.out.persistence.entity.UserEntity;
import com.cryptofolio.backend.infrastructure.out.persistence.repository.JpaPortfolioRepository;
import com.cryptofolio.backend.infrastructure.out.persistence.repository.JpaTransactionRepository;
import com.cryptofolio.backend.infrastructure.out.persistence.repository.JpaUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import(TransactionRepositoryAdapter.class)
class TransactionRepositoryAdapterTest {

    @Autowired
    private TransactionRepositoryAdapter transactionRepositoryAdapter;

    @Autowired
    private JpaTransactionRepository jpaTransactionRepository;

    @Autowired
    private JpaPortfolioRepository jpaPortfolioRepository;

    @Autowired
    private JpaUserRepository jpaUserRepository;

    @Test
    void givenNewTransaction_whenSaving_thenPersistsAndReturnsGeneratedId() {
        Long portfolioId = persistPortfolio("cristian", "cristian@example.com", "Main Portfolio");
        Transaction transaction = new Transaction(
                null,
                portfolioId,
                "BTC",
                TransactionType.BUY,
                new BigDecimal("0.15000000"),
                new BigDecimal("65000.00"),
                Instant.parse("2026-04-06T15:00:00Z"));

        Transaction savedTransaction = transactionRepositoryAdapter.save(transaction);

        assertThat(savedTransaction.getId()).isNotNull();
        assertThat(savedTransaction.getPortfolioId()).isEqualTo(portfolioId);
        assertThat(savedTransaction.getCrypto()).isEqualTo("BTC");
        assertThat(savedTransaction.getType()).isEqualTo(TransactionType.BUY);
        assertThat(savedTransaction.getAmount()).isEqualByComparingTo("0.15000000");
        assertThat(savedTransaction.getPricePerUnit()).isEqualByComparingTo("65000.00");
        assertThat(savedTransaction.getTimestamp()).isEqualTo(Instant.parse("2026-04-06T15:00:00Z"));
    }

    @Test
    void givenPersistedTransactions_whenFindingByPortfolioId_thenReturnsOnlyMatchingDomainTransactions() {
        Long mainPortfolioId = persistPortfolio("cristian", "cristian@example.com", "Main Portfolio");
        Long tradingPortfolioId = persistPortfolio("alice", "alice@example.com", "Trading Portfolio");

        jpaTransactionRepository.save(new TransactionEntity(
                null,
                "BTC",
                TransactionType.BUY,
                new BigDecimal("0.10000000"),
                new BigDecimal("62000.00"),
                Instant.parse("2026-04-06T15:10:00Z"),
                jpaPortfolioRepository.getReferenceById(mainPortfolioId)));
        jpaTransactionRepository.save(new TransactionEntity(
                null,
                "ETH",
                TransactionType.SELL,
                new BigDecimal("1.50000000"),
                new BigDecimal("3200.00"),
                Instant.parse("2026-04-06T15:20:00Z"),
                jpaPortfolioRepository.getReferenceById(mainPortfolioId)));
        jpaTransactionRepository.save(new TransactionEntity(
                null,
                "ADA",
                TransactionType.BUY,
                new BigDecimal("100.00000000"),
                new BigDecimal("1.00"),
                Instant.parse("2026-04-06T15:30:00Z"),
                jpaPortfolioRepository.getReferenceById(tradingPortfolioId)));

        List<Transaction> transactions = transactionRepositoryAdapter.findByPortfolioId(mainPortfolioId);

        assertThat(transactions).hasSize(2);
        assertThat(transactions)
                .extracting(Transaction::getCrypto)
                .containsExactlyInAnyOrder("BTC", "ETH");
        assertThat(transactions)
                .extracting(Transaction::getPortfolioId)
                .containsOnly(mainPortfolioId);
    }

    @Test
    void givenPersistedTransaction_whenDeletingById_thenRemovesItFromPersistence() {
        Long portfolioId = persistPortfolio("bob", "bob@example.com", "Disposable Portfolio");
        Long transactionId = jpaTransactionRepository.save(new TransactionEntity(
                null,
                "BTC",
                TransactionType.BUY,
                new BigDecimal("0.05000000"),
                new BigDecimal("63000.00"),
                Instant.parse("2026-04-06T15:40:00Z"),
                jpaPortfolioRepository.getReferenceById(portfolioId))).getId();

        transactionRepositoryAdapter.deleteById(transactionId);

        assertThat(jpaTransactionRepository.findById(transactionId)).isEmpty();
    }

    private Long persistPortfolio(String username, String email, String portfolioName) {
        Long userId = jpaUserRepository.save(new UserEntity(
                null,
                username,
                email,
                username + "-hash",
                Instant.parse("2026-04-06T14:55:00Z"))).getId();

        return jpaPortfolioRepository.save(new PortfolioEntity(
                null,
                portfolioName,
                "Seed portfolio",
                Instant.parse("2026-04-06T14:56:00Z"),
                jpaUserRepository.getReferenceById(userId))).getId();
    }
}
