package com.account_service.domain.exception;

public class InvalidMovementValueException extends RuntimeException {
    public InvalidMovementValueException() {
        super("Movement value must be greater than zero");
    }
}
