package com.cryptofolio.backend.application.usecase.auth;

import com.cryptofolio.backend.application.dto.request.RegisterUserRequest;
import com.cryptofolio.backend.application.dto.response.AuthResponse;
import com.cryptofolio.backend.application.mapper.UserMapper;
import com.cryptofolio.backend.application.port.out.AuthTokenGenerator;
import com.cryptofolio.backend.application.port.out.PasswordHasher;
import com.cryptofolio.backend.application.port.out.UserRepository;
import com.cryptofolio.backend.domain.exception.UserAlreadyExistsException;
import com.cryptofolio.backend.domain.model.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

class RegisterUserUseCaseTest {

    private static final Instant NOW = Instant.parse("2026-04-05T12:30:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordHasher passwordHasher = mock(PasswordHasher.class);
    private final AuthTokenGenerator authTokenGenerator = mock(AuthTokenGenerator.class);
    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    private final RegisterUserUseCase useCase = new RegisterUserUseCase(
            userRepository,
            passwordHasher,
            authTokenGenerator,
            userMapper,
            FIXED_CLOCK);

    @Test
    void shouldRegisterUserAndReturnAuthResponse() {
        RegisterUserRequest request = new RegisterUserRequest("cristian", "cristian@example.com", "password123");
        User savedUser = new User(7L, "cristian", "cristian@example.com", "hashed-password", NOW);

        when(userRepository.findByEmail("cristian@example.com")).thenReturn(Optional.empty());
        when(passwordHasher.hash("password123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(authTokenGenerator.generateToken(7L)).thenReturn("jwt-token");

        AuthResponse response = useCase.execute(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUserId()).isEqualTo(7L);
        assertThat(response.getUsername()).isEqualTo("cristian");

        verify(userRepository).findByEmail("cristian@example.com");
        verify(passwordHasher).hash("password123");
        verify(userRepository).save(any(User.class));
        verify(authTokenGenerator).generateToken(7L);
    }

    @Test
    void shouldRejectDuplicateEmailBeforeHashingOrSaving() {
        RegisterUserRequest request = new RegisterUserRequest("cristian", "  cristian@example.com  ", "password123");
        User existingUser = new User(99L, "existing", "cristian@example.com", "existing-hash", NOW);

        when(userRepository.findByEmail("cristian@example.com")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("User already exists with email: cristian@example.com");

        verify(passwordHasher, never()).hash(any());
        verify(userRepository, never()).save(any());
        verify(authTokenGenerator, never()).generateToken(any());
    }

    @Test
    void shouldUseClockTimestampWhenCreatingUser() {
        RegisterUserRequest request = new RegisterUserRequest("cristian", "cristian@example.com", "password123");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        when(userRepository.findByEmail("cristian@example.com")).thenReturn(Optional.empty());
        when(passwordHasher.hash("password123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(authTokenGenerator.generateToken(eq(null))).thenReturn("jwt-token");

        AuthResponse response = useCase.execute(request);

        assertThat(response.getUsername()).isEqualTo("cristian");
        verify(userRepository).save(userCaptor.capture());
        verify(authTokenGenerator).generateToken(null);

        User userToSave = userCaptor.getValue();
        assertThat(userToSave.getId()).isNull();
        assertThat(userToSave.getUsername()).isEqualTo("cristian");
        assertThat(userToSave.getEmail()).isEqualTo("cristian@example.com");
        assertThat(userToSave.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(userToSave.getCreatedAt()).isEqualTo(NOW);
    }
}
