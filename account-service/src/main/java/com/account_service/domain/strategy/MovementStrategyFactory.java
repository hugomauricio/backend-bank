package com.account_service.domain.strategy;

import com.account_service.domain.model.MovementType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MovementStrategyFactory {

    private final List<MovementStrategy> strategies;

    public MovementStrategyFactory(List<MovementStrategy> strategies) {
        this.strategies = strategies;
    }

    public MovementStrategy get(MovementType type) {
        return strategies.stream()
                .filter(strategy -> strategy.supports().equals(type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported movement type: " + type));
    }
}