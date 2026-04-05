package com.account_service.infrastructure.utput.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MovementRepository extends JpaRepository<MovementEntity, Long> {
    List<MovementEntity> findByAccountIdAndDateBetween(Long accountId, LocalDateTime start, LocalDateTime end);
}
