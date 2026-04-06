package com.cryptofolio.backend.config;

import com.cryptofolio.backend.application.mapper.UserMapper;
import com.cryptofolio.backend.application.port.in.LoginUserInputPort;
import com.cryptofolio.backend.application.port.in.RegisterUserInputPort;
import com.cryptofolio.backend.application.port.out.AuthTokenGenerator;
import com.cryptofolio.backend.application.port.out.PasswordHasher;
import com.cryptofolio.backend.application.port.out.UserRepository;
import com.cryptofolio.backend.application.usecase.auth.LoginUserUseCase;
import com.cryptofolio.backend.application.usecase.auth.RegisterUserUseCase;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AuthConfig {

    @Bean
    public UserMapper userMapper() {
        return Mappers.getMapper(UserMapper.class);
    }

    @Bean
    public Clock systemClock() {
        return Clock.systemUTC();
    }

    @Bean
    public RegisterUserInputPort registerUserInputPort(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            AuthTokenGenerator authTokenGenerator,
            UserMapper userMapper,
            Clock clock) {
        return new RegisterUserUseCase(userRepository, passwordHasher, authTokenGenerator, userMapper, clock);
    }

    @Bean
    public LoginUserInputPort loginUserInputPort(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            AuthTokenGenerator authTokenGenerator,
            UserMapper userMapper) {
        return new LoginUserUseCase(userRepository, passwordHasher, authTokenGenerator, userMapper);
    }
}
