package com.account_service.domain.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@SuperBuilder
public class Account {
    private final Long id;
    private final String number;
    private final AccountType type;
    private final BigDecimal initialBalance;
    private final BigDecimal currentBalance;
    private final Boolean status;
    private final Long customerId;
}
