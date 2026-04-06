package com.cryptofolio.backend.infrastructure.in.web.controller;

import com.cryptofolio.backend.application.dto.request.LoginRequest;
import com.cryptofolio.backend.application.dto.request.RegisterUserRequest;
import com.cryptofolio.backend.application.dto.response.AuthResponse;
import com.cryptofolio.backend.application.port.in.LoginUserInputPort;
import com.cryptofolio.backend.application.port.in.RegisterUserInputPort;
import com.cryptofolio.backend.domain.exception.InvalidCredentialsException;
import com.cryptofolio.backend.domain.exception.UserAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterUserInputPort registerUserInputPort;

    @MockitoBean
    private LoginUserInputPort loginUserInputPort;

    @MockitoBean
    private com.cryptofolio.backend.infrastructure.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldRegisterUserAndReturnCreated() throws Exception {
        when(registerUserInputPort.execute(any(RegisterUserRequest.class)))
                .thenReturn(new AuthResponse("jwt-token", 1L, "cristian"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "cristian",
                                  "email": "cristian@example.com",
                                  "password": "secret123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("cristian"));
    }

    @Test
    void shouldReturnConflictWhenUserAlreadyExists() throws Exception {
        when(registerUserInputPort.execute(any(RegisterUserRequest.class)))
                .thenThrow(new UserAlreadyExistsException("cristian@example.com"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "cristian",
                                  "email": "cristian@example.com",
                                  "password": "secret123"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User already exists with email: cristian@example.com"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/register"));
    }

    @Test
    void shouldLoginUserAndReturnOk() throws Exception {
        when(loginUserInputPort.execute(any(LoginRequest.class)))
                .thenReturn(new AuthResponse("jwt-token", 1L, "cristian"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "cristian@example.com",
                                  "password": "secret123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void shouldReturnUnauthorizedWhenCredentialsAreInvalid() throws Exception {
        when(loginUserInputPort.execute(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "cristian@example.com",
                                  "password": "secret123"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/login"));
    }

    @Test
    void shouldReturnBadRequestWhenRegisterPayloadIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "",
                                  "email": "invalid-email",
                                  "password": "short"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/register"))
                .andExpect(jsonPath("$.errors.username").value("username cannot be blank"))
                .andExpect(jsonPath("$.errors.email").value("email must be a valid email address"))
                .andExpect(jsonPath("$.errors.password").value("password must be between 8 and 100 characters"));
    }
}
