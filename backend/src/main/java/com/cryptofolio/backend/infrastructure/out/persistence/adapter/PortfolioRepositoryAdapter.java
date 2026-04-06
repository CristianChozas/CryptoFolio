package com.cryptofolio.backend.infrastructure.out.persistence.adapter;

import com.cryptofolio.backend.application.port.out.PortfolioRepository;
import com.cryptofolio.backend.domain.model.Portfolio;
import com.cryptofolio.backend.infrastructure.out.persistence.entity.PortfolioEntity;
import com.cryptofolio.backend.infrastructure.out.persistence.repository.JpaPortfolioRepository;
import com.cryptofolio.backend.infrastructure.out.persistence.repository.JpaUserRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PortfolioRepositoryAdapter implements PortfolioRepository {

    private final JpaPortfolioRepository jpaPortfolioRepository;
    private final JpaUserRepository jpaUserRepository;

    public PortfolioRepositoryAdapter(JpaPortfolioRepository jpaPortfolioRepository, JpaUserRepository jpaUserRepository) {
        this.jpaPortfolioRepository = jpaPortfolioRepository;
        this.jpaUserRepository = jpaUserRepository;
    }

    @Override
    public Portfolio save(Portfolio portfolio) {
        PortfolioEntity savedPortfolio = jpaPortfolioRepository.save(toEntity(portfolio));
        return toDomain(savedPortfolio);
    }

    @Override
    public Optional<Portfolio> findById(Long id) {
        return jpaPortfolioRepository.findById(id)
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
        jpaPortfolioRepository.deleteById(id);
    }

    private PortfolioEntity toEntity(Portfolio portfolio) {
        return new PortfolioEntity(
                portfolio.getId(),
                portfolio.getName(),
                portfolio.getDescription(),
                portfolio.getCreatedAt(),
                jpaUserRepository.getReferenceById(portfolio.getUserId()));
    }

    private Portfolio toDomain(PortfolioEntity portfolioEntity) {
        return new Portfolio(
                portfolioEntity.getId(),
                portfolioEntity.getName(),
                portfolioEntity.getDescription(),
                portfolioEntity.getUser().getId(),
                portfolioEntity.getCreatedAt());
    }
}
