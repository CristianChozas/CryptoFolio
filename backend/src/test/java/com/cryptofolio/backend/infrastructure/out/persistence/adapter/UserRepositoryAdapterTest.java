package com.cryptofolio.backend.infrastructure.out.persistence.adapter;

import com.cryptofolio.backend.domain.model.User;
import com.cryptofolio.backend.infrastructure.out.persistence.repository.JpaUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(UserRepositoryAdapter.class)
class UserRepositoryAdapterTest extends PostgreSqlContainerTest {

    @Autowired
    private UserRepositoryAdapter userRepositoryAdapter;

    @Autowired
    private JpaUserRepository jpaUserRepository;

    @Test
    void givenNewUser_whenSaving_thenPersistsAndReturnsGeneratedId() {
        User user = new User(null, "cristian", "cristian@example.com", "hashed-password",
                Instant.parse("2026-04-06T10:15:30Z"));

        User savedUser = userRepositoryAdapter.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("cristian");
        assertThat(savedUser.getEmail()).isEqualTo("cristian@example.com");
        assertThat(savedUser.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(savedUser.getCreatedAt()).isEqualTo(Instant.parse("2026-04-06T10:15:30Z"));
    }

    @Test
    void givenPersistedUser_whenFindingByEmail_thenReturnsMappedDomainUser() {
        jpaUserRepository.save(new com.cryptofolio.backend.infrastructure.out.persistence.entity.UserEntity(
                null,
                "alice",
                "alice@example.com",
                "stored-hash",
                Instant.parse("2026-04-06T11:00:00Z")));

        assertThat(userRepositoryAdapter.findByEmail("alice@example.com"))
                .isPresent()
                .get()
                .satisfies(user -> {
                    assertThat(user.getUsername()).isEqualTo("alice");
                    assertThat(user.getEmail()).isEqualTo("alice@example.com");
                    assertThat(user.getPasswordHash()).isEqualTo("stored-hash");
                    assertThat(user.getCreatedAt()).isEqualTo(Instant.parse("2026-04-06T11:00:00Z"));
                });
    }

    @Test
    void givenPersistedUser_whenDeletingById_thenRemovesItFromPersistence() {
        Long userId = jpaUserRepository.save(new com.cryptofolio.backend.infrastructure.out.persistence.entity.UserEntity(
                null,
                "bob",
                "bob@example.com",
                "another-hash",
                Instant.parse("2026-04-06T12:00:00Z"))).getId();

        userRepositoryAdapter.deleteById(userId);

        assertThat(jpaUserRepository.findById(userId)).isEmpty();
    }
}
