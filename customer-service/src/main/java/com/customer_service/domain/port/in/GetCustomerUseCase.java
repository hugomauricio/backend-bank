package com.customer_service.domain.port.in;

import com.customer_service.application.dto.CustomerResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GetCustomerUseCase {
    Mono<CustomerResult> getById(Long id);

    Flux<CustomerResult> getAll();
}
