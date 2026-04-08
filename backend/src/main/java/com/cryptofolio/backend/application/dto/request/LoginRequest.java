package com.cryptofolio.backend.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "LoginRequest", description = "Credenciales para iniciar sesion.")
public record LoginRequest(
        @Schema(description = "Correo del usuario registrado.", example = "cristian@example.com")
        @NotBlank(message = "email cannot be blank")
        @Email(message = "email must be a valid email address")
        @Size(max = 255, message = "email must not exceed 255 characters")
        String email,

        @Schema(description = "Contrasena del usuario.", example = "SecurePass123")
        @NotBlank(message = "password cannot be blank")
        @Size(min = 8, max = 100, message = "password must be between 8 and 100 characters")
        String password) {
}
