package com.cryptofolio.backend.infrastructure.in.web.controller;

import com.cryptofolio.backend.application.dto.request.AddTransactionRequest;
import com.cryptofolio.backend.application.dto.response.TransactionResponse;
import com.cryptofolio.backend.application.port.in.AddTransactionInputPort;
import com.cryptofolio.backend.application.port.in.DeleteTransactionInputPort;
import com.cryptofolio.backend.application.port.in.GetTransactionHistoryInputPort;
import com.cryptofolio.backend.infrastructure.security.AuthenticatedUserResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    private static final Principal PRINCIPAL = () -> "cristian@example.com";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AddTransactionInputPort addTransactionInputPort;

    @MockitoBean
    private GetTransactionHistoryInputPort getTransactionHistoryInputPort;

    @MockitoBean
    private DeleteTransactionInputPort deleteTransactionInputPort;

    @MockitoBean
    private AuthenticatedUserResolver authenticatedUserResolver;

    @MockitoBean
    private com.cryptofolio.backend.infrastructure.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldCreateTransaction() throws Exception {
        when(authenticatedUserResolver.resolveUserId(any())).thenReturn(1L);
        when(addTransactionInputPort.execute(anyLong(), any(AddTransactionRequest.class)))
                .thenReturn(new TransactionResponse(
                        77L,
                        "BTC",
                        "BUY",
                        new BigDecimal("0.25000000"),
                        new BigDecimal("65000.00"),
                        Instant.parse("2026-04-06T20:10:00Z")));

        mockMvc.perform(post("/api/v1/transactions")
                        .principal(PRINCIPAL)
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
    }

    @Test
    void shouldGetTransactionHistory() throws Exception {
        when(authenticatedUserResolver.resolveUserId(any())).thenReturn(1L);
        when(getTransactionHistoryInputPort.execute(1L, 10L)).thenReturn(List.of(
                new TransactionResponse(
                        77L,
                        "BTC",
                        "BUY",
                        new BigDecimal("0.25000000"),
                        new BigDecimal("65000.00"),
                        Instant.parse("2026-04-06T20:10:00Z"))));

        mockMvc.perform(get("/api/v1/portfolios/10/transactions").principal(PRINCIPAL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(77));
    }

    @Test
    void shouldDeleteTransaction() throws Exception {
        when(authenticatedUserResolver.resolveUserId(any())).thenReturn(1L);
        doNothing().when(deleteTransactionInputPort).execute(1L, 77L);

        mockMvc.perform(delete("/api/v1/transactions/77").principal(PRINCIPAL))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnValidationErrorsWhenCreatePayloadIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                        .principal(PRINCIPAL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioId": 0,
                                  "crypto": "btc",
                                  "type": "HOLD",
                                  "amount": -1,
                                  "pricePerUnit": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.portfolioId").value("portfolioId must be greater than zero"))
                .andExpect(jsonPath("$.errors.crypto").value("crypto must contain 2-10 uppercase letters or numbers"))
                .andExpect(jsonPath("$.errors.type").value("type must be BUY or SELL"))
                .andExpect(jsonPath("$.errors.amount").value("amount must be greater than zero"))
                .andExpect(jsonPath("$.errors.pricePerUnit").value("pricePerUnit must be greater than zero"));
    }
}
