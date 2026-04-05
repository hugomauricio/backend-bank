package com.account_service.application.dto;

import com.account_service.domain.model.AccountType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class AccountReportItemResult {

    private final Long accountId;
    private final String number;
    private final AccountType type;
    private final BigDecimal initialBalance;
    private final BigDecimal currentBalance;
    private final Boolean status;
    private final List<MovementResult> movements;
}