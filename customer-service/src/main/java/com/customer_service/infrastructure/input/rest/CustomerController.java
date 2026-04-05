package com.customer_service.infrastructure.input.rest;

import com.bank.customer.generated.api.CustomersApi;
import com.bank.customer.generated.model.CustomerRequest;
import com.bank.customer.generated.model.CustomerResponse;
import com.customer_service.domain.port.in.CreateCustomerUseCase;
import com.customer_service.domain.port.in.DeleteCustomerUseCase;
import com.customer_service.domain.port.in.GetCustomerUseCase;
import com.customer_service.domain.port.in.UpdateCustomerUseCase;
import com.customer_service.infrastructure.mapper.CustomerContractMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class CustomerController implements CustomersApi {

    private final CreateCustomerUseCase createCustomerUseCase;
    private final UpdateCustomerUseCase updateCustomerUseCase;
    private final GetCustomerUseCase getCustomerUseCase;
    private final DeleteCustomerUseCase deleteCustomerUseCase;
    private final CustomerContractMapper customerContractMapper;

    @Override
    public Mono<ResponseEntity<CustomerResponse>> createCustomer(
            Mono<CustomerRequest> customerRequest,
            ServerWebExchange exchange) {

        return customerRequest
                .map(customerContractMapper::toCreateCommand)
                .flatMap(createCustomerUseCase::create)
                .map(customerContractMapper::toResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @Override
    public Mono<ResponseEntity<CustomerResponse>> getCustomerById(
            Long customerId,
            ServerWebExchange exchange) {

        return getCustomerUseCase.getById(customerId)
                .map(customerContractMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Flux<CustomerResponse>>> getCustomers(ServerWebExchange exchange) {
        Flux<CustomerResponse> responses = getCustomerUseCase.getAll()
                .map(customerContractMapper::toResponse);

        return Mono.just(ResponseEntity.ok(responses));
    }

    @Override
    public Mono<ResponseEntity<CustomerResponse>> updateCustomer(
            Long customerId,
            Mono<CustomerRequest> customerRequest,
            ServerWebExchange exchange) {

        return customerRequest
                .map(customerContractMapper::toUpdateCommand)
                .flatMap(command -> updateCustomerUseCase.update(customerId, command))
                .map(customerContractMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteCustomer(
            Long customerId,
            ServerWebExchange exchange) {

        return deleteCustomerUseCase.delete(customerId)
                .thenReturn(ResponseEntity.noContent().build());
    }
}