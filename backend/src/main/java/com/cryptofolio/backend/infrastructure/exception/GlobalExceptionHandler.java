package com.cryptofolio.backend.infrastructure.exception;

import com.cryptofolio.backend.domain.exception.InsufficientFundsException;
import com.cryptofolio.backend.domain.exception.InvalidCredentialsException;
import com.cryptofolio.backend.domain.exception.PortfolioNotFoundException;
import com.cryptofolio.backend.domain.exception.TransactionNotFoundException;
import com.cryptofolio.backend.domain.exception.UnauthorizedPortfolioAccessException;
import com.cryptofolio.backend.domain.exception.UserAlreadyExistsException;
import com.cryptofolio.backend.domain.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String HTTP_LOG_MESSAGE_ATTRIBUTE = "http.logging.message";
    private static final String HTTP_LOG_SOURCE_ATTRIBUTE = "http.logging.source";

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(
            UserAlreadyExistsException exception,
            HttpServletRequest request) {
        attachLogContext(request, "email already exists", exception);
        return buildErrorResponse(HttpStatus.CONFLICT, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler({ UserNotFoundException.class, PortfolioNotFoundException.class, TransactionNotFoundException.class })
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException exception, HttpServletRequest request) {
        attachLogContext(request, exception.getMessage(), exception);
        return buildErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException exception,
            HttpServletRequest request) {
        attachLogContext(request, "invalid credentials", exception);
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(UnauthorizedPortfolioAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedPortfolio(
            UnauthorizedPortfolioAccessException exception,
            HttpServletRequest request) {
        attachLogContext(request, exception.getMessage(), exception);
        return buildErrorResponse(HttpStatus.FORBIDDEN, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler({ InsufficientFundsException.class, IllegalArgumentException.class, IllegalStateException.class })
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException exception, HttpServletRequest request) {
        attachLogContext(request, exception.getMessage(), exception);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request) {
        String message = resolveDataIntegrityMessage(exception);
        HttpStatus status = isUserEmailConstraintViolation(exception) ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;

        attachLogContext(request, message, exception);
        return buildErrorResponse(status, message, request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                request.getRequestURI(),
                errors);
        attachLogContext(request, "validation failed", exception);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception exception, HttpServletRequest request) {
        attachLogContext(request, "internal server error", exception);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", request.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message, String path) {
        ErrorResponse errorResponse = new ErrorResponse(Instant.now(), status.value(), message, path);
        return new ResponseEntity<>(errorResponse, status);
    }

    private void attachLogContext(HttpServletRequest request, String message, Throwable exception) {
        request.setAttribute(HTTP_LOG_MESSAGE_ATTRIBUTE, message);
        request.setAttribute(HTTP_LOG_SOURCE_ATTRIBUTE, resolveSource(exception));
    }

    private boolean isUserEmailConstraintViolation(DataIntegrityViolationException exception) {
        String message = exception.getMostSpecificCause() != null
                ? exception.getMostSpecificCause().getMessage()
                : exception.getMessage();
        return message != null && message.contains("uk_users_email");
    }

    private String resolveDataIntegrityMessage(DataIntegrityViolationException exception) {
        if (isUserEmailConstraintViolation(exception)) {
            return "User already exists with email";
        }

        return "Data integrity violation";
    }

    private String resolveSource(Throwable exception) {
        for (StackTraceElement element : exception.getStackTrace()) {
            if (element.getClassName().startsWith("com.cryptofolio.backend")) {
                return "%s#%s".formatted(element.getFileName(), element.getMethodName());
            }
        }

        return null;
    }
}
