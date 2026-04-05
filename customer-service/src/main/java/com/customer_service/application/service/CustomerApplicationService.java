package com.customer_service.application.service;

import com.customer_service.application.dto.CreateCustomerCommand;
import com.customer_service.application.dto.CustomerResult;
import com.customer_service.application.dto.UpdateCustomerCommand;
import com.customer_service.domain.exception.CustomerNotFoundException;
import com.customer_service.domain.exception.DuplicateIdentificationException;
import com.customer_service.domain.model.Customer;
import com.customer_service.domain.port.in.CreateCustomerUseCase;
import com.customer_service.domain.port.in.DeleteCustomerUseCase;
import com.customer_service.domain.port.in.GetCustomerUseCase;
import com.customer_service.domain.port.in.UpdateCustomerUseCase;
import com.customer_service.domain.port.out.CustomerCommandPort;
import com.customer_service.domain.port.out.CustomerQueryPort;
import com.customer_service.infrastructure.messaging.CustomerEventPublisher;
import com.customer_service.infrastructure.utput.persistence.CustomerPersistenceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerApplicationService implements CreateCustomerUseCase, UpdateCustomerUseCase,
        GetCustomerUseCase, DeleteCustomerUseCase {

    private final CustomerCommandPort customerCommandPort;
    private final CustomerQueryPort customerQueryPort;
    private final CustomerPersistenceMapper customerPersistenceMapper;
    private final CustomerEventPublisher customerEventPublisher;

    @Override
    public Mono<CustomerResult> create(CreateCustomerCommand command) {
        return Mono.fromCallable(() -> {
                    if (customerQueryPort.existsByIdentification(command.getIdentification())) {
                        throw new DuplicateIdentificationException(command.getIdentification());
                    }

                    Customer customer = Customer.builder()
                            .name(command.getName())
                            .gender(command.getGender())
                            .identification(command.getIdentification())
                            .address(command.getAddress())
                            .phone(command.getPhone())
                            .password(command.getPassword())
                            .status(command.getStatus())
                            .build();

                    Customer savedCustomer = customerCommandPort.save(customer);

                    log.info("Customer created successfully with id={}", savedCustomer.getId());
                    customerEventPublisher.publishCreated(savedCustomer);
                    return customerPersistenceMapper.toResult(savedCustomer);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<CustomerResult> update(Long id, UpdateCustomerCommand command) {
        return Mono.fromCallable(() -> {
                    customerQueryPort.findById(id)
                            .orElseThrow(() -> new CustomerNotFoundException(id));

                    if (customerQueryPort.existsByIdentificationAndIdNot(command.getIdentification(), id)) {
                        throw new DuplicateIdentificationException(command.getIdentification());
                    }

                    Customer customer = Customer.builder()
                            .id(id)
                            .name(command.getName())
                            .gender(command.getGender())
                            .identification(command.getIdentification())
                            .address(command.getAddress())
                            .phone(command.getPhone())
                            .password(command.getPassword())
                            .status(command.getStatus())
                            .build();

                    Customer updatedCustomer = customerCommandPort.save(customer);

                    log.info("Customer updated successfully with id={}", updatedCustomer.getId());
                    customerEventPublisher.publishUpdated(updatedCustomer);
                    return customerPersistenceMapper.toResult(updatedCustomer);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<CustomerResult> getById(Long id) {
        return Mono.fromCallable(() ->
                        customerQueryPort.findById(id)
                                .map(customerPersistenceMapper::toResult)
                                .orElseThrow(() -> new CustomerNotFoundException(id)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<CustomerResult> getAll() {
        return Mono.fromCallable(customerQueryPort::findAll)
                .flatMapMany(Flux::fromIterable)
                .map(customerPersistenceMapper::toResult)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> delete(Long id) {
        return Mono.fromRunnable(() -> {
                    customerQueryPort.findById(id)
                            .orElseThrow(() -> new CustomerNotFoundException(id));

                    customerCommandPort.deleteById(id);

                    log.info("Customer deleted successfully with id={}", id);
                    customerEventPublisher.publishDeleted(id);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}