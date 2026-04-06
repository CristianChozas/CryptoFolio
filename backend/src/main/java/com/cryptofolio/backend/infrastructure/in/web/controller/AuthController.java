package com.cryptofolio.backend.infrastructure.in.web.controller;

import com.cryptofolio.backend.application.dto.request.LoginRequest;
import com.cryptofolio.backend.application.dto.request.RegisterUserRequest;
import com.cryptofolio.backend.application.dto.response.AuthResponse;
import com.cryptofolio.backend.application.port.in.LoginUserInputPort;
import com.cryptofolio.backend.application.port.in.RegisterUserInputPort;
import com.cryptofolio.backend.domain.exception.InvalidCredentialsException;
import com.cryptofolio.backend.domain.exception.UserAlreadyExistsException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUserInputPort registerUserInputPort;
    private final LoginUserInputPort loginUserInputPort;

    public AuthController(RegisterUserInputPort registerUserInputPort, LoginUserInputPort loginUserInputPort) {
        this.registerUserInputPort = registerUserInputPort;
        this.loginUserInputPort = loginUserInputPort;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        AuthResponse response = registerUserInputPort.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(loginUserInputPort.execute(request));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUserAlreadyExists(UserAlreadyExistsException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials(InvalidCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", exception.getMessage()));
    }
}
