package com.account_service.domain.port.in;

import com.account_service.application.dto.MovementResult;
import com.account_service.application.dto.RegisterMovementCommand;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RegisterMovementUseCase {
    Mono<MovementResult> create(RegisterMovementCommand command);

    Mono<MovementResult> getById(Long id);

    Flux<MovementResult> getAll();
}