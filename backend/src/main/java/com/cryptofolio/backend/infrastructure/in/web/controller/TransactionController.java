package com.cryptofolio.backend.infrastructure.in.web.controller;

import com.cryptofolio.backend.application.dto.request.AddTransactionRequest;
import com.cryptofolio.backend.application.dto.response.TransactionResponse;
import com.cryptofolio.backend.application.port.in.AddTransactionInputPort;
import com.cryptofolio.backend.application.port.in.DeleteTransactionInputPort;
import com.cryptofolio.backend.application.port.in.GetTransactionHistoryInputPort;
import com.cryptofolio.backend.infrastructure.exception.ErrorResponse;
import com.cryptofolio.backend.infrastructure.security.AuthenticatedUserResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Transactions", description = "Gestion de transacciones asociadas a portfolios.")
@SecurityRequirement(name = "bearerAuth")
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
    @Operation(summary = "Crear transaccion", description = "Registra una compra o venta dentro de un portfolio del usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transaccion creada",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos invalidos o fondos insuficientes",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o invalido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acceso a portfolio ajeno",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Portfolio no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody AddTransactionRequest request, Principal principal) {
        Long userId = authenticatedUserResolver.resolveUserId(principal);
        TransactionResponse response = addTransactionInputPort.execute(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/v1/portfolios/{portfolioId}/transactions")
    @Operation(summary = "Listar historial de transacciones", description = "Devuelve las transacciones de un portfolio ordenadas de mas reciente a mas antigua.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial obtenido",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TransactionResponse.class)))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o invalido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acceso a portfolio ajeno",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Portfolio no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<TransactionResponse>> history(@PathVariable Long portfolioId, Principal principal) {
        Long userId = authenticatedUserResolver.resolveUserId(principal);
        return ResponseEntity.ok(getTransactionHistoryInputPort.execute(userId, portfolioId));
    }

    @DeleteMapping("/api/v1/transactions/{transactionId}")
    @Operation(summary = "Eliminar transaccion", description = "Elimina una transaccion perteneciente a un portfolio del usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Transaccion eliminada"),
            @ApiResponse(responseCode = "401", description = "JWT ausente o invalido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acceso a portfolio ajeno",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Transaccion no encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(@PathVariable Long transactionId, Principal principal) {
        Long userId = authenticatedUserResolver.resolveUserId(principal);
        deleteTransactionInputPort.execute(userId, transactionId);
        return ResponseEntity.noContent().build();
    }
}
