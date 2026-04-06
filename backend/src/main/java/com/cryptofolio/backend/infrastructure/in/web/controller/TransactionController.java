package com.cryptofolio.backend.infrastructure.in.web.controller;

import com.cryptofolio.backend.application.dto.request.AddTransactionRequest;
import com.cryptofolio.backend.application.dto.response.TransactionResponse;
import com.cryptofolio.backend.application.port.in.AddTransactionInputPort;
import com.cryptofolio.backend.application.port.in.DeleteTransactionInputPort;
import com.cryptofolio.backend.application.port.in.GetTransactionHistoryInputPort;
import com.cryptofolio.backend.infrastructure.security.AuthenticatedUserResolver;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping
public class TransactionController {

    private final AddTransactionInputPort addTransactionInputPort;
    private final GetTransactionHistoryInputPort getTransactionHistoryInputPort;
    private final DeleteTransactionInputPort deleteTransactionInputPort;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    public TransactionController(
            AddTransactionInputPort addTransactionInputPort,
            GetTransactionHistoryInputPort getTransactionHistoryInputPort,
            DeleteTransactionInputPort deleteTransactionInputPort,
            AuthenticatedUserResolver authenticatedUserResolver) {
        this.addTransactionInputPort = addTransactionInputPort;
        this.getTransactionHistoryInputPort = getTransactionHistoryInputPort;
        this.deleteTransactionInputPort = deleteTransactionInputPort;
        this.authenticatedUserResolver = authenticatedUserResolver;
    }

    @PostMapping("/api/v1/transactions")
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody AddTransactionRequest request, Principal principal) {
        Long userId = authenticatedUserResolver.resolveUserId(principal);
        TransactionResponse response = addTransactionInputPort.execute(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/v1/portfolios/{portfolioId}/transactions")
    public ResponseEntity<List<TransactionResponse>> history(@PathVariable Long portfolioId, Principal principal) {
        Long userId = authenticatedUserResolver.resolveUserId(principal);
        return ResponseEntity.ok(getTransactionHistoryInputPort.execute(userId, portfolioId));
    }

    @DeleteMapping("/api/v1/transactions/{transactionId}")
    public ResponseEntity<Void> delete(@PathVariable Long transactionId, Principal principal) {
        Long userId = authenticatedUserResolver.resolveUserId(principal);
        deleteTransactionInputPort.execute(userId, transactionId);
        return ResponseEntity.noContent().build();
    }
}
