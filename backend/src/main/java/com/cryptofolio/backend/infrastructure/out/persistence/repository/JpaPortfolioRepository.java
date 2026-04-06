package com.cryptofolio.backend.infrastructure.out.persistence.repository;

import com.cryptofolio.backend.infrastructure.out.persistence.entity.PortfolioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaPortfolioRepository extends JpaRepository<PortfolioEntity, Long> {

    List<PortfolioEntity> findByUserId(Long userId);
}
