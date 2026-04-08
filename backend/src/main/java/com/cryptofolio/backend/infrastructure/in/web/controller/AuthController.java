package com.cryptofolio.backend.infrastructure.in.web.controller;

import com.cryptofolio.backend.application.dto.request.LoginRequest;
import com.cryptofolio.backend.application.dto.request.RegisterUserRequest;
import com.cryptofolio.backend.application.dto.response.AuthResponse;
import com.cryptofolio.backend.application.port.in.LoginUserInputPort;
import com.cryptofolio.backend.application.port.in.RegisterUserInputPort;
import com.cryptofolio.backend.infrastructure.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Endpoints de registro e inicio de sesion.")
public class AuthController {

    private final RegisterUserInputPort registerUserInputPort;
    private final LoginUserInputPort loginUserInputPort;

    public AuthController(RegisterUserInputPort registerUserInputPort, LoginUserInputPort loginUserInputPort) {
        this.registerUserInputPort = registerUserInputPort;
        this.loginUserInputPort = loginUserInputPort;
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar usuario", description = "Crea un usuario nuevo y devuelve un JWT valido.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado correctamente",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Payload invalido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El usuario ya existe",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        AuthResponse response = registerUserInputPort.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion", description = "Autentica al usuario y devuelve un JWT para consumir endpoints protegidos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login correcto",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Payload invalido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales invalidas",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(loginUserInputPort.execute(request));
    }
}
