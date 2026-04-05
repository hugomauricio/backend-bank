package com.account_service.application.service;

import com.account_service.application.dto.MovementResult;
import com.account_service.application.dto.RegisterMovementCommand;
import com.account_service.domain.exception.AccountNotFoundException;
import com.account_service.domain.exception.InvalidMovementValueException;
import com.account_service.domain.model.Account;
import com.account_service.domain.model.Movement;
import com.account_service.domain.port.in.RegisterMovementUseCase;
import com.account_service.domain.port.out.AccountCommandPort;
import com.account_service.domain.port.out.AccountQueryPort;
import com.account_service.domain.port.out.MovementCommandPort;
import com.account_service.domain.port.out.MovementQueryPort;
import com.account_service.domain.strategy.MovementStrategyFactory;
import com.account_service.infrastructure.utput.persistence.AccountPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MovementApplicationService implements RegisterMovementUseCase {

    private final AccountQueryPort accountQueryPort;
    private final AccountCommandPort accountCommandPort;
    private final MovementCommandPort movementCommandPort;
    private final MovementQueryPort movementQueryPort;
    private final MovementStrategyFactory movementStrategyFactory;
    private final AccountPersistenceMapper mapper;

    @Override
    public Mono<MovementResult> create(RegisterMovementCommand command) {
        return Mono.fromCallable(() -> {
                    if (command.getValue() == null || command.getValue().compareTo(BigDecimal.ZERO) <= 0) {
                        throw new InvalidMovementValueException();
                    }

                    Account account = accountQueryPort.findById(command.getAccountId())
                            .orElseThrow(() -> new AccountNotFoundException(command.getAccountId()));

                    BigDecimal newBalance = movementStrategyFactory.get(command.getType())
                            .calculate(account.getCurrentBalance(), command.getValue());

                    Account updatedAccount = accountCommandPort.save(Account.builder()
                            .id(account.getId())
                            .number(account.getNumber())
                            .type(account.getType())
                            .initialBalance(account.getInitialBalance())
                            .currentBalance(newBalance)
                            .status(account.getStatus())
                            .customerId(account.getCustomerId())
                            .build());

                    Movement saved = movementCommandPort.save(Movement.builder()
                            .accountId(updatedAccount.getId())
                            .date(LocalDateTime.now())
                            .type(command.getType())
                            .value(command.getValue())
                            .balance(newBalance)
                            .build());

                    return mapper.toMovementResult(saved);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<MovementResult> getById(Long id) {
        return Mono.fromCallable(() -> movementQueryPort.findById(id)
                        .map(mapper::toMovementResult)
                        .orElseThrow(() -> new IllegalArgumentException("Movement with id " + id + " was not found")))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<MovementResult> getAll() {
        return Mono.fromCallable(movementQueryPort::findAll)
                .flatMapMany(Flux::fromIterable)
                .map(mapper::toMovementResult)
                .subscribeOn(Schedulers.boundedElastic());
    }
}