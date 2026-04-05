package com.cryptofolio.backend.application.mapper;

import com.cryptofolio.backend.application.dto.request.RegisterUserRequest;
import com.cryptofolio.backend.application.dto.response.AuthResponse;
import com.cryptofolio.backend.domain.model.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    void shouldMapRegisterUserRequestToUser() {
        RegisterUserRequest request = new RegisterUserRequest("cristian", "cristian@example.com", "password123");
        Instant createdAt = Instant.parse("2026-04-05T12:00:00Z");

        User user = mapper.toUser(request, "hashed-password", createdAt);

        assertThat(user.getId()).isNull();
        assertThat(user.getUsername()).isEqualTo("cristian");
        assertThat(user.getEmail()).isEqualTo("cristian@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(user.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldMapUserToAuthResponse() {
        User user = new User(7L, "cristian", "cristian@example.com", "hashed-password",
                Instant.parse("2026-04-05T12:00:00Z"));

        AuthResponse response = mapper.toAuthResponse(user, "jwt-token");

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUserId()).isEqualTo(7L);
        assertThat(response.getUsername()).isEqualTo("cristian");
    }
}
