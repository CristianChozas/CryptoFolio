package com.cryptofolio.backend.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AuthResponse", description = "Respuesta de autenticacion con JWT y datos basicos del usuario.")
public class AuthResponse {

    @Schema(description = "JWT firmado para autenticarse contra endpoints protegidos.")
    private final String token;

    @Schema(description = "Identificador del usuario autenticado.", example = "1")
    private final Long userId;

    @Schema(description = "Nombre del usuario autenticado.", example = "cristian")
    private final String username;

    public AuthResponse(String token, Long userId, String username) {
        this.token = token;
        this.userId = userId;
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }
}
