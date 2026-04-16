package com.cryptofolio.backend.infrastructure.in.web.controller;

import com.cryptofolio.backend.application.dto.request.AddTransactionRequest;
import com.cryptofolio.backend.application.dto.request.CreatePortfolioRequest;
import com.cryptofolio.backend.application.dto.response.PortfolioResponse;
import com.cryptofolio.backend.application.dto.response.TransactionResponse;
import com.cryptofolio.backend.application.port.in.AddTransactionInputPort;
import com.cryptofolio.backend.application.port.in.CreatePortfolioInputPort;
import com.cryptofolio.backend.application.port.in.GetPortfolioInputPort;
import com.cryptofolio.backend.application.port.in.ListUserPortfoliosInputPort;
import com.cryptofolio.backend.domain.exception.UnauthorizedPortfolioAccessException;
import com.cryptofolio.backend.infrastructure.out.persistence.entity.UserEntity;
import com.cryptofolio.backend.infrastructure.out.persistence.repository.JpaUserRepository;
import com.cryptofolio.backend.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@SuppressWarnings("null")
class ApiSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaUserRepository jpaUserRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private ListUserPortfoliosInputPort listUserPortfoliosInputPort;

    @MockitoBean
    private CreatePortfolioInputPort createPortfolioInputPort;

    @MockitoBean
    private GetPortfolioInputPort getPortfolioInputPort;

    @MockitoBean
    private AddTransactionInputPort addTransactionInputPort;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        jpaUserRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAllowAccessToRegisterEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "ab",
                                  "email": "invalid-email",
                                  "password": "short"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void shouldRejectProtectedPortfolioEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/portfolios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowProtectedPortfolioEndpointWithValidJwt() throws Exception {
        UserEntity savedUser = persistUser();
        String token = jwtTokenProvider.generateToken(savedUser.getId());

        when(listUserPortfoliosInputPort.execute(savedUser.getId())).thenReturn(List.of(
                new PortfolioResponse(10L, "Main", "Long term", Instant.parse("2026-04-07T10:05:00Z"))));

        mockMvc.perform(get("/api/v1/portfolios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].name").value("Main"));
    }

    @Test
    void shouldCreatePortfolioWithValidJwt() throws Exception {
        UserEntity savedUser = persistUser();
        String token = jwtTokenProvider.generateToken(savedUser.getId());

        when(createPortfolioInputPort.execute(eq(savedUser.getId()), any(CreatePortfolioRequest.class)))
                .thenReturn(new PortfolioResponse(21L, "Trading", "Active trading", Instant.parse("2026-04-07T10:10:00Z")));

        mockMvc.perform(post("/api/v1/portfolios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Trading",
                                  "description": "Active trading"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(21))
                .andExpect(jsonPath("$.name").value("Trading"));

        verify(createPortfolioInputPort).execute(eq(savedUser.getId()), any(CreatePortfolioRequest.class));
    }

    @Test
    void shouldCreateTransactionWithValidJwt() throws Exception {
        UserEntity savedUser = persistUser();
        String token = jwtTokenProvider.generateToken(savedUser.getId());

        when(addTransactionInputPort.execute(eq(savedUser.getId()), any(AddTransactionRequest.class)))
                .thenReturn(new TransactionResponse(
                        77L,
                        "BTC",
                        "BUY",
                        new BigDecimal("0.25000000"),
                        new BigDecimal("65000.00"),
                        Instant.parse("2026-04-07T10:15:00Z")));

        mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 10,
                                  "crypto": "BTC",
                                  "type": "BUY",
                                  "amount": 0.25,
                                  "pricePerUnit": 65000.00
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(77))
                .andExpect(jsonPath("$.crypto").value("BTC"));

        verify(addTransactionInputPort).execute(eq(savedUser.getId()), any(AddTransactionRequest.class));
    }

    @Test
    void shouldReturnForbiddenWhenAuthenticatedUserAccessesAnotherUsersPortfolio() throws Exception {
        UserEntity savedUser = persistUser();
        String token = jwtTokenProvider.generateToken(savedUser.getId());

        when(getPortfolioInputPort.execute(savedUser.getId(), 99L))
                .thenThrow(new UnauthorizedPortfolioAccessException(99L, savedUser.getId()));

        mockMvc.perform(get("/api/v1/portfolios/99")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("User " + savedUser.getId() + " is not authorized to access portfolio: 99"))
                .andExpect(jsonPath("$.path").value("/api/v1/portfolios/99"));
    }

    private UserEntity persistUser() {
        return jpaUserRepository.save(new UserEntity(
                null,
                "cristian",
                "cristian@example.com",
                "hashed-password",
                Instant.parse("2026-04-07T10:00:00Z")));
    }
}
