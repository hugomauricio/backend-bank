package com.account_service.infrastructure.input.rest.error;

import com.account_service.domain.exception.AccountNotFoundException;
import com.account_service.domain.exception.DuplicateAccountNumberException;
import com.account_service.domain.exception.InsufficientBalanceException;
import com.account_service.domain.exception.InvalidMovementValueException;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

import java.time.OffsetDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(AccountNotFoundException ex, ServerWebExchange exchange) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(HttpStatus.NOT_FOUND, ex.getMessage(), exchange.getRequest().getPath().value()));
    }

    @ExceptionHandler(DuplicateAccountNumberException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(DuplicateAccountNumberException ex, ServerWebExchange exchange) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(HttpStatus.CONFLICT, ex.getMessage(), exchange.getRequest().getPath().value()));
    }

    @ExceptionHandler({InvalidMovementValueException.class, InsufficientBalanceException.class})
    public ResponseEntity<ApiErrorResponse> handleBusinessError(RuntimeException ex, ServerWebExchange exchange) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(buildError(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), exchange.getRequest().getPath().value()));
    }

    @ExceptionHandler({WebExchangeBindException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception ex, ServerWebExchange exchange) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), exchange.getRequest().getPath().value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, ServerWebExchange exchange) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), exchange.getRequest().getPath().value()));
    }

    private ApiErrorResponse buildError(HttpStatus status, String message, String path) {
        return ApiErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .build();
    }

    @Getter
    @Builder
    public static class ApiErrorResponse {
        private final OffsetDateTime timestamp;
        private final int status;
        private final String error;
        private final String message;
        private final String path;
    }
}