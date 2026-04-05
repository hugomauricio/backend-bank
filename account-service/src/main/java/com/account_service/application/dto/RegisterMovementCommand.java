package com.account_service.application.dto;

import com.account_service.domain.model.MovementType;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@SuperBuilder
public class RegisterMovementCommand {
    private final Long accountId;
    private final MovementType type;
    private final BigDecimal value;
}
