package com.customer_service.domain.port.in;

import reactor.core.publisher.Mono;

public interface DeleteCustomerUseCase {
    Mono<Void> delete(Long id);
}