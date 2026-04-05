package com.cryptofolio.backend.application.usecase.auth;

import com.cryptofolio.backend.application.dto.request.LoginRequest;
import com.cryptofolio.backend.application.dto.response.AuthResponse;
import com.cryptofolio.backend.application.mapper.UserMapper;
import com.cryptofolio.backend.application.port.in.LoginUserInputPort;
import com.cryptofolio.backend.application.port.out.AuthTokenGenerator;
import com.cryptofolio.backend.application.port.out.PasswordHasher;
import com.cryptofolio.backend.application.port.out.UserRepository;
import com.cryptofolio.backend.domain.exception.InvalidCredentialsException;
import com.cryptofolio.backend.domain.model.User;

import java.util.Objects;

public class LoginUserUseCase implements LoginUserInputPort {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final AuthTokenGenerator authTokenGenerator;
    private final UserMapper userMapper;

    public LoginUserUseCase(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            AuthTokenGenerator authTokenGenerator,
            UserMapper userMapper) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository cannot be null");
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "passwordHasher cannot be null");
        this.authTokenGenerator = Objects.requireNonNull(authTokenGenerator, "authTokenGenerator cannot be null");
        this.userMapper = Objects.requireNonNull(userMapper, "userMapper cannot be null");
    }

    @Override
    public AuthResponse execute(LoginRequest request) {
        LoginRequest nonNullRequest = Objects.requireNonNull(request, "request cannot be null");
        String normalizedEmail = normalizeField(nonNullRequest.email());

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        boolean passwordMatches = passwordHasher.matches(nonNullRequest.password(), user.getPasswordHash());
        if (!passwordMatches) {
            throw new InvalidCredentialsException();
        }

        String token = authTokenGenerator.generateToken(user.getId());
        return userMapper.toAuthResponse(user, token);
    }

    private String normalizeField(String value) {
        return value == null ? null : value.trim();
    }
}
