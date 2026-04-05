package com.account_service.domain.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@SuperBuilder
public class Movement {
    private final Long id;
    private final LocalDateTime date;
    private final MovementType type;
    private final BigDecimal value;
    private final BigDecimal balance;
    private final Long accountId;
}