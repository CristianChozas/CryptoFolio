package com.cryptofolio.backend.infrastructure.out.persistence.adapter;

import com.cryptofolio.backend.application.port.out.UserRepository;
import com.cryptofolio.backend.domain.model.User;
import com.cryptofolio.backend.infrastructure.out.persistence.entity.UserEntity;
import com.cryptofolio.backend.infrastructure.out.persistence.repository.JpaUserRepository;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    public UserRepositoryAdapter(JpaUserRepository jpaUserRepository) {
        this.jpaUserRepository = Objects.requireNonNull(jpaUserRepository, "jpaUserRepository cannot be null");
    }

    @Override
    public User save(User user) {
        UserEntity savedUser = jpaUserRepository.save(toEntity(user));
        return toDomain(savedUser);
    }

    @Override
    public Optional<User> findById(Long id) {
        Long nonNullId = Objects.requireNonNull(id, "user id cannot be null");
        return jpaUserRepository.findById(nonNullId)
                .map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email)
                .map(this::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        Long nonNullId = Objects.requireNonNull(id, "user id cannot be null");
        jpaUserRepository.deleteById(nonNullId);
    }

    private UserEntity toEntity(User user) {
        return new UserEntity(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getCreatedAt());
    }

    private User toDomain(UserEntity userEntity) {
        return new User(
                userEntity.getId(),
                Objects.requireNonNull(userEntity.getUsername(), "user username cannot be null"),
                Objects.requireNonNull(userEntity.getEmail(), "user email cannot be null"),
                Objects.requireNonNull(userEntity.getPasswordHash(), "user passwordHash cannot be null"),
                Objects.requireNonNull(userEntity.getCreatedAt(), "user createdAt cannot be null"));
    }
}
