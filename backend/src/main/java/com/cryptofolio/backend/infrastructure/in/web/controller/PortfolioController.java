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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/portfolios")
@Tag(name = "Portfolios", description = "Gestion de portfolios del usuario autenticado.")
@SecurityRequirement(name = "bearerAuth")
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
    @Operation(summary = "Crear portfolio", description = "Crea un nuevo portfolio para el usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Portfolio creado",
                    content = @Content(schema = @Schema(implementation = PortfolioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos invalidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o invalido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PortfolioResponse> create(@Valid @RequestBody CreatePortfolioRequest request, Principal principal) {
        Long userId = authenticatedUserResolver.resolveUserId(principal);
        PortfolioResponse response = createPortfolioInputPort.execute(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar portfolios", description = "Devuelve todos los portfolios del usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PortfolioResponse.class)))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o invalido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<PortfolioResponse>> list(Principal principal) {
        Long userId = authenticatedUserResolver.resolveUserId(principal);
        return ResponseEntity.ok(listUserPortfoliosInputPort.execute(userId));
    }

    @GetMapping("/{portfolioId}")
    @Operation(summary = "Obtener portfolio", description = "Recupera un portfolio concreto del usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Portfolio encontrado",
                    content = @Content(schema = @Schema(implementation = PortfolioResponse.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o invalido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acceso a portfolio ajeno",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Portfolio no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PortfolioResponse> get(@PathVariable Long portfolioId, Principal principal) {
        Long userId = authenticatedUserResolver.resolveUserId(principal);
        return ResponseEntity.ok(getPortfolioInputPort.execute(userId, portfolioId));
    }

    @PutMapping("/{portfolioId}")
    @Operation(summary = "Actualizar portfolio", description = "Actualiza el nombre o la descripcion de un portfolio del usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Portfolio actualizado",
                    content = @Content(schema = @Schema(implementation = PortfolioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos invalidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o invalido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acceso a portfolio ajeno",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Portfolio no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PortfolioResponse> update(
            @PathVariable Long portfolioId,
            @Valid @RequestBody CreatePortfolioRequest request,
            Principal principal) {
        Long userId = authenticatedUserResolver.resolveUserId(principal);
        return ResponseEntity.ok(updatePortfolioInputPort.execute(userId, portfolioId, request));
    }

    @DeleteMapping("/{portfolioId}")
    @Operation(summary = "Eliminar portfolio", description = "Elimina un portfolio y sus transacciones asociadas.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Portfolio eliminado"),
            @ApiResponse(responseCode = "401", description = "JWT ausente o invalido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acceso a portfolio ajeno",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Portfolio no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(@PathVariable Long portfolioId, Principal principal) {
        Long userId = authenticatedUserResolver.resolveUserId(principal);
        deletePortfolioInputPort.execute(userId, portfolioId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{portfolioId}/summary")
    @Operation(summary = "Obtener resumen del portfolio", description = "Devuelve balance, profit/loss y ROI del portfolio indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumen calculado",
                    content = @Content(schema = @Schema(implementation = PortfolioSummaryResponse.class))),
            @ApiResponse(responseCode = "401", description = "JWT ausente o invalido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Acceso a portfolio ajeno",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Portfolio no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PortfolioSummaryResponse> summary(@PathVariable Long portfolioId, Principal principal) {
        Long userId = authenticatedUserResolver.resolveUserId(principal);
        return ResponseEntity.ok(getPortfolioSummaryInputPort.execute(userId, portfolioId));
    }
}
