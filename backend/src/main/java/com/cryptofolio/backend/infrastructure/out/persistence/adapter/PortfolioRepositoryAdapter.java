package com.cryptofolio.backend.infrastructure.out.persistence.adapter;

import com.cryptofolio.backend.application.port.out.PortfolioRepository;
import com.cryptofolio.backend.domain.model.Portfolio;
import com.cryptofolio.backend.infrastructure.out.persistence.entity.PortfolioEntity;
import com.cryptofolio.backend.infrastructure.out.persistence.entity.UserEntity;
import com.cryptofolio.backend.infrastructure.out.persistence.repository.JpaPortfolioRepository;
import com.cryptofolio.backend.infrastructure.out.persistence.repository.JpaUserRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class PortfolioRepositoryAdapter implements PortfolioRepository {

    private final JpaPortfolioRepository jpaPortfolioRepository;
    private final JpaUserRepository jpaUserRepository;

    public PortfolioRepositoryAdapter(JpaPortfolioRepository jpaPortfolioRepository, JpaUserRepository jpaUserRepository) {
        this.jpaPortfolioRepository = Objects.requireNonNull(jpaPortfolioRepository, "jpaPortfolioRepository cannot be null");
        this.jpaUserRepository = Objects.requireNonNull(jpaUserRepository, "jpaUserRepository cannot be null");
    }

    @Override
    public Portfolio save(Portfolio portfolio) {
        PortfolioEntity savedPortfolio = jpaPortfolioRepository.save(toEntity(portfolio));
        return toDomain(savedPortfolio);
    }

    @Override
    public Optional<Portfolio> findById(Long id) {
        Long nonNullId = Objects.requireNonNull(id, "portfolio id cannot be null");
        return jpaPortfolioRepository.findById(nonNullId)
                .map(this::toDomain);
    }

    @Override
    public List<Portfolio> findByUserId(Long userId) {
        return jpaPortfolioRepository.findByUserId(userId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        Long nonNullId = Objects.requireNonNull(id, "portfolio id cannot be null");
        jpaPortfolioRepository.deleteById(nonNullId);
    }

    private PortfolioEntity toEntity(Portfolio portfolio) {
        Long userId = Objects.requireNonNull(portfolio.getUserId(), "portfolio user id cannot be null");
        return new PortfolioEntity(
                portfolio.getId(),
                portfolio.getName(),
                portfolio.getDescription(),
                portfolio.getCreatedAt(),
                jpaUserRepository.getReferenceById(userId));
    }

    private Portfolio toDomain(PortfolioEntity portfolioEntity) {
        UserEntity user = Objects.requireNonNull(portfolioEntity.getUser(), "portfolio user cannot be null");
        return new Portfolio(
                portfolioEntity.getId(),
                Objects.requireNonNull(portfolioEntity.getName(), "portfolio name cannot be null"),
                portfolioEntity.getDescription(),
                Objects.requireNonNull(user.getId(), "portfolio user id cannot be null"),
                Objects.requireNonNull(portfolioEntity.getCreatedAt(), "portfolio createdAt cannot be null"));
    }
}
