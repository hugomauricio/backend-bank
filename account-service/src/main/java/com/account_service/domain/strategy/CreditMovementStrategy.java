package com.account_service.domain.strategy;

import com.account_service.domain.model.MovementType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CreditMovementStrategy implements MovementStrategy {

    @Override
    public MovementType supports() {
        return MovementType.CREDIT;
    }

    @Override
    public BigDecimal calculate(BigDecimal currentBalance, BigDecimal amount) {
        return currentBalance.add(amount);
    }
}
