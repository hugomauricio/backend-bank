package com.customer_service.domain.port.in;


import com.customer_service.application.dto.CreateCustomerCommand;
import com.customer_service.application.dto.CustomerResult;
import reactor.core.publisher.Mono;

public interface CreateCustomerUseCase {
    Mono<CustomerResult> create(CreateCustomerCommand command);
}