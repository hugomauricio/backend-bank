package com.account_service.application.dto;

import com.account_service.domain.model.MovementType;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@SuperBuilder
public class MovementResult {
    private final Long id;
    private final Long accountId;
    private final LocalDateTime date;
    private final MovementType type;
    private final BigDecimal value;
    private final BigDecimal balance;
}