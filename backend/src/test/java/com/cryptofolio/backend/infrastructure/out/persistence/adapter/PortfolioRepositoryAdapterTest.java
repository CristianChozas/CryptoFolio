package com.cryptofolio.backend.infrastructure.out.persistence.adapter;

import com.cryptofolio.backend.domain.model.Portfolio;
import com.cryptofolio.backend.infrastructure.out.persistence.entity.UserEntity;
import com.cryptofolio.backend.infrastructure.out.persistence.repository.JpaPortfolioRepository;
import com.cryptofolio.backend.infrastructure.out.persistence.repository.JpaUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PortfolioRepositoryAdapter.class)
@SuppressWarnings("null")
class PortfolioRepositoryAdapterTest extends PostgreSqlContainerTest {

    @Autowired
    private PortfolioRepositoryAdapter portfolioRepositoryAdapter;

    @Autowired
    private JpaPortfolioRepository jpaPortfolioRepository;

    @Autowired
    private JpaUserRepository jpaUserRepository;

    @Test
    void givenNewPortfolio_whenSaving_thenPersistsAndReturnsGeneratedId() {
        Long userId = persistUser("cristian", "cristian@example.com");
        Portfolio portfolio = new Portfolio(null, "Main Portfolio", "Long term", userId,
                Instant.parse("2026-04-06T14:00:00Z"));

        Portfolio savedPortfolio = portfolioRepositoryAdapter.save(portfolio);

        assertThat(savedPortfolio.getId()).isNotNull();
        assertThat(savedPortfolio.getName()).isEqualTo("Main Portfolio");
        assertThat(savedPortfolio.getDescription()).isEqualTo("Long term");
        assertThat(savedPortfolio.getUserId()).isEqualTo(userId);
        assertThat(savedPortfolio.getCreatedAt()).isEqualTo(Instant.parse("2026-04-06T14:00:00Z"));
    }

    @Test
    void givenPersistedPortfolios_whenFindingByUserId_thenReturnsOnlyMatchingDomainPortfolios() {
        Long cristianId = persistUser("cristian", "cristian@example.com");
        Long aliceId = persistUser("alice", "alice@example.com");

        jpaPortfolioRepository.save(new com.cryptofolio.backend.infrastructure.out.persistence.entity.PortfolioEntity(
                null,
                "Main",
                "Primary",
                Instant.parse("2026-04-06T14:10:00Z"),
                jpaUserRepository.getReferenceById(cristianId)));
        jpaPortfolioRepository.save(new com.cryptofolio.backend.infrastructure.out.persistence.entity.PortfolioEntity(
                null,
                "Trading",
                null,
                Instant.parse("2026-04-06T14:20:00Z"),
                jpaUserRepository.getReferenceById(cristianId)));
        jpaPortfolioRepository.save(new com.cryptofolio.backend.infrastructure.out.persistence.entity.PortfolioEntity(
                null,
                "Other User",
                "Ignore",
                Instant.parse("2026-04-06T14:30:00Z"),
                jpaUserRepository.getReferenceById(aliceId)));

        List<Portfolio> portfolios = portfolioRepositoryAdapter.findByUserId(cristianId);

        assertThat(portfolios).hasSize(2);
        assertThat(portfolios)
                .extracting(Portfolio::getName)
                .containsExactlyInAnyOrder("Main", "Trading");
        assertThat(portfolios)
                .extracting(Portfolio::getUserId)
                .containsOnly(cristianId);
    }

    @Test
    void givenPersistedPortfolio_whenDeletingById_thenRemovesItFromPersistence() {
        Long userId = persistUser("bob", "bob@example.com");
        Long portfolioId = jpaPortfolioRepository.save(new com.cryptofolio.backend.infrastructure.out.persistence.entity.PortfolioEntity(
                null,
                "Disposable",
                null,
                Instant.parse("2026-04-06T14:40:00Z"),
                jpaUserRepository.getReferenceById(userId))).getId();

        portfolioRepositoryAdapter.deleteById(portfolioId);

        assertThat(jpaPortfolioRepository.findById(portfolioId)).isEmpty();
    }

    private Long persistUser(String username, String email) {
        return jpaUserRepository.save(new UserEntity(
                null,
                username,
                email,
                username + "-hash",
                Instant.parse("2026-04-06T13:55:00Z"))).getId();
    }
}
