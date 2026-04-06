package com.cryptofolio.backend.infrastructure.in.web.controller;

import com.cryptofolio.backend.application.dto.request.CreatePortfolioRequest;
import com.cryptofolio.backend.application.dto.response.PortfolioResponse;
import com.cryptofolio.backend.application.dto.response.PortfolioSummaryResponse;
import com.cryptofolio.backend.application.port.in.CreatePortfolioInputPort;
import com.cryptofolio.backend.application.port.in.DeletePortfolioInputPort;
import com.cryptofolio.backend.application.port.in.GetPortfolioInputPort;
import com.cryptofolio.backend.application.port.in.GetPortfolioSummaryInputPort;
import com.cryptofolio.backend.application.port.in.ListUserPortfoliosInputPort;
import com.cryptofolio.backend.application.port.in.UpdatePortfolioInputPort;
import com.cryptofolio.backend.domain.exception.PortfolioNotFoundException;
import com.cryptofolio.backend.domain.exception.UnauthorizedPortfolioAccessException;
import com.cryptofolio.backend.infrastructure.security.AuthenticatedUserResolver;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/portfolios")
public class PortfolioController {

    private final CreatePortfolioInputPort createPortfolioInputPort;
    private final ListUserPortfoliosInputPort listUserPortfoliosInputPort;
    private final GetPortfolioInputPort getPortfolioInputPort;
    private final UpdatePortfolioInputPort updatePortfolioInputPort;
    private final DeletePortfolioInputPort deletePortfolioInputPort;
    private final GetPortfolioSummaryInputPort getPortfolioSummaryInputPort;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    public PortfolioController(
            CreatePortfolioInputPort createPortfolioInputPort,
            ListUserPortfoliosInputPort listUserPortfoliosInputPort,
            GetPortfolioInputPort getPortfolioInputPort,
            UpdatePortfolioInputPort updatePortfolioInputPort,
            DeletePortfolioInputPort deletePortfolioInputPort,
            GetPortfolioSummaryInputPort getPortfolioSummaryInputPort,
            AuthenticatedUserResolver authenticatedUserResolver) {
        this.createPortfolioInputPort = createPortfolioInputPort;
        this.listUserPortfoliosInputPort = listUserPortfoliosInputPort;
        this.getPortfolioInputPort = getPortfolioInputPort;
        this.updatePortfolioInputPort = updatePortfolioInputPort;
        this.deletePortfolioInputPort = deletePortfolioInputPort;
        this.getPortfolioSummaryInputPort = getPortfolioSummaryInputPort;
        this.authenticatedUserResolver = authenticatedUserResolver;
    }

    @PostMapping
    public ResponseEntity<PortfolioResponse> create(@Valid @RequestBody CreatePortfolioRequest request, Principal principal) {
        Long userId = authenticatedUserResolver.resolveUserId(principal);
        PortfolioResponse response = createPortfolioInputPort.execute(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PortfolioResponse>> list(Principal principal) {
        Long userId = authenticatedUserResolver.resolveUserId(principal);
        return ResponseEntity.ok(listUserPortfoliosInputPort.execute(userId));
    }

    @GetMapping("/{portfolioId}")
    public ResponseEntity<PortfolioResponse> get(@PathVariable Long portfolioId, Principal principal) {
        Long userId = authenticatedUserResolver.resolveUserId(principal);
        return ResponseEntity.ok(getPortfolioInputPort.execute(userId, portfolioId));
    }

    @PutMapping("/{portfolioId}")
    public ResponseEntity<PortfolioResponse> update(
            @PathVariable Long portfolioId,
            @Valid @RequestBody CreatePortfolioRequest request,
            Principal principal) {
        Long userId = authenticatedUserResolver.resolveUserId(principal);
        return ResponseEntity.ok(updatePortfolioInputPort.execute(userId, portfolioId, request));
    }

    @DeleteMapping("/{portfolioId}")
    public ResponseEntity<Void> delete(@PathVariable Long portfolioId, Principal principal) {
        Long userId = authenticatedUserResolver.resolveUserId(principal);
        deletePortfolioInputPort.execute(userId, portfolioId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{portfolioId}/summary")
    public ResponseEntity<PortfolioSummaryResponse> summary(@PathVariable Long portfolioId, Principal principal) {
        Long userId = authenticatedUserResolver.resolveUserId(principal);
        return ResponseEntity.ok(getPortfolioSummaryInputPort.execute(userId, portfolioId));
    }

    @ExceptionHandler(PortfolioNotFoundException.class)
    public ResponseEntity<Map<String, String>> handlePortfolioNotFound(PortfolioNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(UnauthorizedPortfolioAccessException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorizedPortfolio(UnauthorizedPortfolioAccessException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", exception.getMessage()));
    }
}
