package com.account_service.domain.strategy;

import com.account_service.domain.exception.InsufficientBalanceException;
import com.account_service.domain.model.MovementType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DebitMovementStrategy implements MovementStrategy {

    @Override
    public MovementType supports() {
        return MovementType.DEBIT;
    }

    @Override
    public BigDecimal calculate(BigDecimal currentBalance, BigDecimal amount) {
        BigDecimal result = currentBalance.subtract(amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientBalanceException();
        }
        return result;
    }
}