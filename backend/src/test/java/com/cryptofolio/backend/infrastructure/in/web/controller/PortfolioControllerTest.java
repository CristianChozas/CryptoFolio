package com.cryptofolio.backend.infrastructure.in.web.controller;

import com.cryptofolio.backend.application.dto.request.CreatePortfolioRequest;
import com.cryptofolio.backend.application.dto.response.PortfolioOverviewItemResponse;
import com.cryptofolio.backend.application.dto.response.PortfolioOverviewOperationResponse;
import com.cryptofolio.backend.application.dto.response.PortfolioOverviewResponse;
import com.cryptofolio.backend.application.dto.response.PortfolioResponse;
import com.cryptofolio.backend.application.dto.response.PortfolioSummaryResponse;
import com.cryptofolio.backend.application.port.in.CreatePortfolioInputPort;
import com.cryptofolio.backend.application.port.in.DeletePortfolioInputPort;
import com.cryptofolio.backend.application.port.in.GetPortfolioOverviewInputPort;
import com.cryptofolio.backend.application.port.in.GetPortfolioInputPort;
import com.cryptofolio.backend.application.port.in.GetPortfolioSummaryInputPort;
import com.cryptofolio.backend.application.port.in.ListUserPortfoliosInputPort;
import com.cryptofolio.backend.application.port.in.UpdatePortfolioInputPort;
import com.cryptofolio.backend.domain.exception.PortfolioNotFoundException;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PortfolioController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("null")
class PortfolioControllerTest {

    private static final Principal PRINCIPAL = () -> "cristian@example.com";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreatePortfolioInputPort createPortfolioInputPort;

    @MockitoBean
    private ListUserPortfoliosInputPort listUserPortfoliosInputPort;

    @MockitoBean
    private GetPortfolioInputPort getPortfolioInputPort;

    @MockitoBean
    private UpdatePortfolioInputPort updatePortfolioInputPort;

    @MockitoBean
    private DeletePortfolioInputPort deletePortfolioInputPort;

    @MockitoBean
    private GetPortfolioSummaryInputPort getPortfolioSummaryInputPort;

    @MockitoBean
    private GetPortfolioOverviewInputPort getPortfolioOverviewInputPort;

    @MockitoBean
    private AuthenticatedUserResolver authenticatedUserResolver;

    @MockitoBean
    private com.cryptofolio.backend.infrastructure.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldCreatePortfolio() throws Exception {
        when(authenticatedUserResolver.resolveUserId(any())).thenReturn(1L);
        when(createPortfolioInputPort.execute(anyLong(), any(CreatePortfolioRequest.class)))
                .thenReturn(new PortfolioResponse(10L, "Main", "Long term", Instant.parse("2026-04-06T20:00:00Z")));

        mockMvc.perform(post("/api/v1/portfolios")
                        .principal(PRINCIPAL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Main",
                                  "description": "Long term"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Main"));
    }

    @Test
    void shouldListUserPortfolios() throws Exception {
        when(authenticatedUserResolver.resolveUserId(any())).thenReturn(1L);
        when(listUserPortfoliosInputPort.execute(1L)).thenReturn(List.of(
                new PortfolioResponse(10L, "Main", "Long term", Instant.parse("2026-04-06T20:00:00Z"))));

        mockMvc.perform(get("/api/v1/portfolios").principal(PRINCIPAL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].name").value("Main"));
    }

    @Test
    void shouldGetPortfolioById() throws Exception {
        when(authenticatedUserResolver.resolveUserId(any())).thenReturn(1L);
        when(getPortfolioInputPort.execute(1L, 10L))
                .thenReturn(new PortfolioResponse(10L, "Main", "Long term", Instant.parse("2026-04-06T20:00:00Z")));

        mockMvc.perform(get("/api/v1/portfolios/10").principal(PRINCIPAL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void shouldUpdatePortfolio() throws Exception {
        when(authenticatedUserResolver.resolveUserId(any())).thenReturn(1L);
        when(updatePortfolioInputPort.execute(anyLong(), anyLong(), any(CreatePortfolioRequest.class)))
                .thenReturn(new PortfolioResponse(10L, "Updated", "Desc", Instant.parse("2026-04-06T20:00:00Z")));

        mockMvc.perform(put("/api/v1/portfolios/10")
                        .principal(PRINCIPAL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated",
                                  "description": "Desc"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void shouldDeletePortfolio() throws Exception {
        when(authenticatedUserResolver.resolveUserId(any())).thenReturn(1L);
        doNothing().when(deletePortfolioInputPort).execute(1L, 10L);

        mockMvc.perform(delete("/api/v1/portfolios/10").principal(PRINCIPAL))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldGetPortfolioSummary() throws Exception {
        when(authenticatedUserResolver.resolveUserId(any())).thenReturn(1L);
        when(getPortfolioSummaryInputPort.execute(1L, 10L)).thenReturn(new PortfolioSummaryResponse(
                new PortfolioResponse(10L, "Main", "Long term", Instant.parse("2026-04-06T20:00:00Z")),
                Map.of("BTC", new BigDecimal("0.50000000")),
                new BigDecimal("1250.50"),
                "USD",
                new BigDecimal("8.25")));

        mockMvc.perform(get("/api/v1/portfolios/10/summary").principal(PRINCIPAL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.portfolio.id").value(10))
                .andExpect(jsonPath("$.balance.BTC").value(0.50000000))
                .andExpect(jsonPath("$.profitLossCurrency").value("USD"));
    }

    @Test
    void shouldGetPortfolioOverview() throws Exception {
        when(authenticatedUserResolver.resolveUserId(any())).thenReturn(1L);
        when(getPortfolioOverviewInputPort.execute(1L)).thenReturn(new PortfolioOverviewResponse(
                1,
                new BigDecimal("1250.50"),
                "USD",
                new BigDecimal("250.25"),
                "USD",
                List.of(new PortfolioOverviewItemResponse(
                        new PortfolioResponse(10L, "Main", "Long term", Instant.parse("2026-04-06T20:00:00Z")),
                        Map.of("BTC", new BigDecimal("0.50000000")),
                        new BigDecimal("1250.50"),
                        "USD",
                        new BigDecimal("250.25"),
                        "USD",
                        new BigDecimal("8.25"))),
                List.of(new PortfolioOverviewOperationResponse(
                        77L,
                        10L,
                        "Main",
                        "BTC",
                        "BUY",
                        new BigDecimal("0.10000000"),
                        new BigDecimal("65000.00"),
                        Instant.parse("2026-04-06T21:00:00Z")))));

        mockMvc.perform(get("/api/v1/portfolios/overview").principal(PRINCIPAL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.portfolioCount").value(1))
                .andExpect(jsonPath("$.portfolios[0].portfolio.id").value(10))
                .andExpect(jsonPath("$.recentOperations[0].portfolioName").value("Main"));
    }

    @Test
    void shouldReturnNotFoundWhenPortfolioDoesNotExist() throws Exception {
        when(authenticatedUserResolver.resolveUserId(any())).thenReturn(1L);
        when(getPortfolioInputPort.execute(1L, 999L)).thenThrow(new PortfolioNotFoundException(999L));

        mockMvc.perform(get("/api/v1/portfolios/999").principal(PRINCIPAL))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/v1/portfolios/999"));
    }

    @Test
    void shouldReturnValidationErrorsWhenCreatePayloadIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/portfolios")
                        .principal(PRINCIPAL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "description": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").value("name cannot be blank"))
                .andExpect(jsonPath("$.errors.description").value("description must not exceed 255 characters"));
    }
}
