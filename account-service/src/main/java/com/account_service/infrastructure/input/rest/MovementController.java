package com.account_service.infrastructure.input.rest;

import com.account_service.domain.port.in.RegisterMovementUseCase;
import com.account_service.infrastructure.mapper.AccountContractMapper;
import com.bank.customer.generated.api.MovementsApi;
import com.bank.customer.generated.model.MovementRequest;
import com.bank.customer.generated.model.MovementResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class MovementController implements MovementsApi {

    private final RegisterMovementUseCase registerMovementUseCase;
    private final AccountContractMapper mapper;

    @Override
    public Mono<ResponseEntity<MovementResponse>> createMovement(Mono<MovementRequest> movementRequest, ServerWebExchange exchange) {
        return movementRequest
                .map(mapper::toRegisterMovementCommand)
                .flatMap(registerMovementUseCase::create)
                .map(mapper::toMovementResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @Override
    public Mono<ResponseEntity<MovementResponse>> getMovementById(Long movementId, ServerWebExchange exchange) {
        return registerMovementUseCase.getById(movementId)
                .map(mapper::toMovementResponse)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Flux<MovementResponse>>> getMovements(ServerWebExchange exchange) {
        Flux<MovementResponse> response = registerMovementUseCase.getAll().map(mapper::toMovementResponse);
        return Mono.just(ResponseEntity.ok(response));
    }
}