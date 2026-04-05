package com.account_service.application.dto;

import com.account_service.domain.model.AccountType;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@SuperBuilder
public class UpdateAccountCommand {
    private final String number;
    private final AccountType type;
    private final BigDecimal initialBalance;
    private final Boolean status;
    private final Long customerId;
}