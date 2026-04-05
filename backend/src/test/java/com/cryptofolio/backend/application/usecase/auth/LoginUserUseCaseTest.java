package com.cryptofolio.backend.application.usecase.auth;

import com.cryptofolio.backend.application.dto.request.LoginRequest;
import com.cryptofolio.backend.application.dto.response.AuthResponse;
import com.cryptofolio.backend.application.mapper.UserMapper;
import com.cryptofolio.backend.application.port.out.AuthTokenGenerator;
import com.cryptofolio.backend.application.port.out.PasswordHasher;
import com.cryptofolio.backend.application.port.out.UserRepository;
import com.cryptofolio.backend.domain.exception.InvalidCredentialsException;
import com.cryptofolio.backend.domain.model.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginUserUseCaseTest {

    private static final Instant NOW = Instant.parse("2026-04-05T14:00:00Z");

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordHasher passwordHasher = mock(PasswordHasher.class);
    private final AuthTokenGenerator authTokenGenerator = mock(AuthTokenGenerator.class);
    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    private final LoginUserUseCase useCase = new LoginUserUseCase(
            userRepository,
            passwordHasher,
            authTokenGenerator,
            userMapper);

    @Test
    void shouldAuthenticateUserAndReturnAuthResponse() {
        LoginRequest request = new LoginRequest("cristian@example.com", "password123");
        User existingUser = new User(7L, "cristian", "cristian@example.com", "hashed-password", NOW);

        when(userRepository.findByEmail("cristian@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordHasher.matches("password123", "hashed-password")).thenReturn(true);
        when(authTokenGenerator.generateToken(7L)).thenReturn("jwt-token");

        AuthResponse response = useCase.execute(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUserId()).isEqualTo(7L);
        assertThat(response.getUsername()).isEqualTo("cristian");

        verify(userRepository).findByEmail("cristian@example.com");
        verify(passwordHasher).matches("password123", "hashed-password");
        verify(authTokenGenerator).generateToken(7L);
    }

    @Test
    void shouldRejectUnknownEmail() {
        LoginRequest request = new LoginRequest("  cristian@example.com  ", "password123");

        when(userRepository.findByEmail("cristian@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");

        verify(passwordHasher, never()).matches(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(authTokenGenerator, never()).generateToken(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectPasswordMismatch() {
        LoginRequest request = new LoginRequest("cristian@example.com", "wrong-password");
        User existingUser = new User(7L, "cristian", "cristian@example.com", "hashed-password", NOW);

        when(userRepository.findByEmail("cristian@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordHasher.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");

        verify(authTokenGenerator, never()).generateToken(org.mockito.ArgumentMatchers.any());
    }
}
