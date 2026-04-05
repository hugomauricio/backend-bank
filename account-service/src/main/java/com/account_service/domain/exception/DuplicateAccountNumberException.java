package com.account_service.domain.exception;

public class DuplicateAccountNumberException extends RuntimeException {
    public DuplicateAccountNumberException(String number) {
        super("Account number already exists: " + number);
    }
}
