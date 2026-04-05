package com.account_service.domain.port.out;

import com.account_service.domain.model.Movement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MovementQueryPort {
    Optional<Movement> findById(Long id);

    List<Movement> findAll();

    List<Movement> findByAccountIdAndDateBetween(Long accountId, LocalDateTime start, LocalDateTime end);
}