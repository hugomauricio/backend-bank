package com.account_service.domain.port.out;


import com.account_service.domain.model.Movement;

public interface MovementCommandPort {
    Movement save(Movement movement);
}
