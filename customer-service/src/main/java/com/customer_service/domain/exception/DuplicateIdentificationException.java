package com.customer_service.domain.exception;

public class DuplicateIdentificationException extends RuntimeException {
    public DuplicateIdentificationException(String identification) {
        super("Customer identification already exists: " + identification);
    }
}