package com.account_service.application.service;

import com.account_service.application.dto.AccountResult;
import com.account_service.application.dto.CreateAccountCommand;
import com.account_service.application.dto.UpdateAccountCommand;
import com.account_service.domain.exception.AccountNotFoundException;
import com.account_service.domain.exception.DuplicateAccountNumberException;
import com.account_service.domain.model.Account;
import com.account_service.domain.port.in.CreateAccountUseCase;
import com.account_service.domain.port.in.DeleteAccountUseCase;
import com.account_service.domain.port.in.GetAccountUseCase;
import com.account_service.domain.port.in.UpdateAccountUseCase;
import com.account_service.domain.port.out.AccountCommandPort;
import com.account_service.domain.port.out.AccountQueryPort;
import com.account_service.infrastructure.utput.persistence.AccountPersistenceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountApplicationService implements CreateAccountUseCase, UpdateAccountUseCase,
        GetAccountUseCase, DeleteAccountUseCase {

    private final AccountCommandPort accountCommandPort;
    private final AccountQueryPort accountQueryPort;
    private final AccountPersistenceMapper mapper;

    @Override
    public Mono<AccountResult> create(CreateAccountCommand command) {
        return Mono.fromCallable(() -> {
                    if (accountQueryPort.existsByNumber(command.getNumber())) {
                        throw new DuplicateAccountNumberException(command.getNumber());
                    }
                    Account saved = accountCommandPort.save(Account.builder()
                            .number(command.getNumber())
                            .type(command.getType())
                            .initialBalance(command.getInitialBalance())
                            .currentBalance(command.getInitialBalance())
                            .status(command.getStatus())
                            .customerId(command.getCustomerId())
                            .build());
                    return mapper.toAccountResult(saved);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<AccountResult> update(Long id, UpdateAccountCommand command) {
        return Mono.fromCallable(() -> {
                    accountQueryPort.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
                    if (accountQueryPort.existsByNumberAndIdNot(command.getNumber(), id)) {
                        throw new DuplicateAccountNumberException(command.getNumber());
                    }
                    Account current = accountQueryPort.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
                    Account updated = accountCommandPort.save(Account.builder()
                            .id(id)
                            .number(command.getNumber())
                            .type(command.getType())
                            .initialBalance(command.getInitialBalance())
                            .currentBalance(current.getCurrentBalance())
                            .status(command.getStatus())
                            .customerId(command.getCustomerId())
                            .build());
                    return mapper.toAccountResult(updated);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<AccountResult> getById(Long id) {
        return Mono.fromCallable(() -> accountQueryPort.findById(id)
                        .map(mapper::toAccountResult)
                        .orElseThrow(() -> new AccountNotFoundException(id)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<AccountResult> getAll() {
        return Mono.fromCallable(accountQueryPort::findAll)
                .flatMapMany(Flux::fromIterable)
                .map(mapper::toAccountResult)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> delete(Long id) {
        return Mono.fromRunnable(() -> {
                    accountQueryPort.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
                    accountCommandPort.deleteById(id);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
