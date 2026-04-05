package com.account_service.domain.port.in;

import reactor.core.publisher.Mono;

public interface DeleteAccountUseCase {
    Mono<Void> delete(Long id);
}
