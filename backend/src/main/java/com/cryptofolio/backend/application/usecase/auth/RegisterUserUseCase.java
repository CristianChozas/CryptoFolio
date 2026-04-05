package com.cryptofolio.backend.application.usecase.auth;

import com.cryptofolio.backend.application.dto.request.RegisterUserRequest;
import com.cryptofolio.backend.application.dto.response.AuthResponse;
import com.cryptofolio.backend.application.mapper.UserMapper;
import com.cryptofolio.backend.application.port.in.RegisterUserInputPort;
import com.cryptofolio.backend.application.port.out.AuthTokenGenerator;
import com.cryptofolio.backend.application.port.out.PasswordHasher;
import com.cryptofolio.backend.application.port.out.UserRepository;
import com.cryptofolio.backend.domain.exception.UserAlreadyExistsException;
import com.cryptofolio.backend.domain.model.User;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class RegisterUserUseCase implements RegisterUserInputPort {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final AuthTokenGenerator authTokenGenerator;
    private final UserMapper userMapper;
    private final Clock clock;

    public RegisterUserUseCase(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            AuthTokenGenerator authTokenGenerator,
            UserMapper userMapper,
            Clock clock) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository cannot be null");
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "passwordHasher cannot be null");
        this.authTokenGenerator = Objects.requireNonNull(authTokenGenerator, "authTokenGenerator cannot be null");
        this.userMapper = Objects.requireNonNull(userMapper, "userMapper cannot be null");
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
    }

    @Override
    public AuthResponse execute(RegisterUserRequest request) {
        RegisterUserRequest nonNullRequest = Objects.requireNonNull(request, "request cannot be null");
        String normalizedEmail = normalizeField(nonNullRequest.email());

        userRepository.findByEmail(normalizedEmail)
                .ifPresent(existingUser -> {
                    throw new UserAlreadyExistsException(normalizedEmail);
                });

        String passwordHash = passwordHasher.hash(nonNullRequest.password());
        Instant createdAt = Instant.now(clock);

        User userToSave = userMapper.toUser(nonNullRequest, passwordHash, createdAt);
        User savedUser = userRepository.save(userToSave);
        String token = authTokenGenerator.generateToken(savedUser.getId());

        return userMapper.toAuthResponse(savedUser, token);
    }

    private String normalizeField(String value) {
        return value == null ? null : value.trim();
    }
}
