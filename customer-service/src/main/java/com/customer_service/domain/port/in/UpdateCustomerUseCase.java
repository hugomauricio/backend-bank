package com.customer_service.domain.port.in;


import com.customer_service.application.dto.CustomerResult;
import com.customer_service.application.dto.UpdateCustomerCommand;
import reactor.core.publisher.Mono;

public interface UpdateCustomerUseCase {
    Mono<CustomerResult> update(Long id, UpdateCustomerCommand command);
}
