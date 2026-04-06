package com.cryptofolio.backend.infrastructure.out.persistence.repository;

import com.cryptofolio.backend.infrastructure.out.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);
}
