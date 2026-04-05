package com.account_service.infrastructure.utput.persistence;

import com.account_service.domain.model.Movement;
import com.account_service.domain.port.out.MovementCommandPort;
import com.account_service.domain.port.out.MovementQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MovementJpaAdapter implements MovementCommandPort, MovementQueryPort {

    private final MovementRepository movementRepository;
    private final AccountPersistenceMapper mapper;

    @Override
    public Movement save(Movement movement) {
        return mapper.toDomain(movementRepository.save(mapper.toEntity(movement)));
    }

    @Override
    public Optional<Movement> findById(Long id) {
        return movementRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Movement> findAll() {
        return movementRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Movement> findByAccountIdAndDateBetween(Long accountId, LocalDateTime start, LocalDateTime end) {
        return movementRepository.findByAccountIdAndDateBetween(accountId, start, end)
                .stream().map(mapper::toDomain).toList();
    }
}