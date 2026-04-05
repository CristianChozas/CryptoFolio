package com.cryptofolio.backend.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
        @NotBlank(message = "username cannot be blank")
        @Size(min = 3, max = 50, message = "username must be between 3 and 50 characters")
        String username,

        @NotBlank(message = "email cannot be blank")
        @Email(message = "email must be a valid email address")
        @Size(max = 255, message = "email must not exceed 255 characters")
        String email,

        @NotBlank(message = "password cannot be blank")
        @Size(min = 8, max = 100, message = "password must be between 8 and 100 characters")
        String password) {
}
