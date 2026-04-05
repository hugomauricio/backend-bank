package com.account_service.domain.port.in;

import com.account_service.application.dto.AccountResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GetAccountUseCase {
    Mono<AccountResult> getById(Long id);

    Flux<AccountResult> getAll();
}