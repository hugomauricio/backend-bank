package com.account_service.domain.strategy;

import com.account_service.domain.model.MovementType;

import java.math.BigDecimal;

public interface MovementStrategy {
    MovementType supports();
    BigDecimal calculate(BigDecimal currentBalance, BigDecimal amount);
}
