package com.customer_service.service;

import com.customer_service.application.dto.CreateCustomerCommand;
import com.customer_service.application.dto.CustomerResult;
import com.customer_service.application.dto.UpdateCustomerCommand;
import com.customer_service.application.service.CustomerApplicationService;
import com.customer_service.domain.exception.CustomerNotFoundException;
import com.customer_service.domain.exception.DuplicateIdentificationException;
import com.customer_service.domain.model.Customer;
import com.customer_service.domain.model.Gender;
import com.customer_service.domain.port.out.CustomerCommandPort;
import com.customer_service.domain.port.out.CustomerQueryPort;
import com.customer_service.infrastructure.messaging.CustomerEventPublisher;
import com.customer_service.infrastructure.utput.persistence.CustomerPersistenceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerApplicationServiceTest {

    @Mock
    private CustomerCommandPort customerCommandPort;
    @Mock
    private CustomerQueryPort customerQueryPort;
    @Mock
    private CustomerPersistenceMapper customerPersistenceMapper;
    @Mock
    private CustomerEventPublisher customerEventPublisher;

    @InjectMocks
    private CustomerApplicationService customerApplicationService;

    private CreateCustomerCommand createCommand;
    private UpdateCustomerCommand updateCommand;
    private Customer customer;
    private CustomerResult customerResult;

    @BeforeEach
    void setUp() {
        createCommand = CreateCustomerCommand.builder()
                .name("Jose Lema")
                .gender(Gender.MALE)
                .identification("12345")
                .address("Otavalo")
                .phone("098254785")
                .password("1234")
                .status(true)
                .build();

        updateCommand = UpdateCustomerCommand.builder()
                .name("Jose Lema Updated")
                .gender(Gender.MALE)
                .identification("12345")
                .address("Quito")
                .phone("099999999")
                .password("5678")
                .status(true)
                .build();

        customer = Customer.builder()
                .id(1L)
                .name("Jose Lema")
                .gender(Gender.MALE)
                .identification("12345")
                .address("Otavalo")
                .phone("098254785")
                .password("1234")
                .status(true)
                .build();

        customerResult = CustomerResult.builder()
                .id(1L)
                .name("Jose Lema")
                .gender(Gender.MALE)
                .identification("12345")
                .address("Otavalo")
                .phone("098254785")
                .status(true)
                .build();
    }

    @Test
    void shouldCreateCustomerSuccessfully() {
        when(customerQueryPort.existsByIdentification(createCommand.getIdentification())).thenReturn(false);
        when(customerCommandPort.save(any(Customer.class))).thenReturn(customer);
        when(customerPersistenceMapper.toResult(customer)).thenReturn(customerResult);

        StepVerifier.create(customerApplicationService.create(createCommand))
                .expectNextMatches(result ->
                        result.getId().equals(1L) &&
                                result.getName().equals("Jose Lema") &&
                                result.getIdentification().equals("12345"))
                .verifyComplete();

        verify(customerQueryPort).existsByIdentification(createCommand.getIdentification());
        verify(customerCommandPort).save(any(Customer.class));
        verify(customerPersistenceMapper).toResult(customer);
        verify(customerEventPublisher).publishCreated(customer);
    }

    @Test
    void shouldFailCreateWhenIdentificationAlreadyExists() {
        when(customerQueryPort.existsByIdentification(createCommand.getIdentification())).thenReturn(true);

        StepVerifier.create(customerApplicationService.create(createCommand))
                .expectError(DuplicateIdentificationException.class)
                .verify();

        verify(customerCommandPort, never()).save(any(Customer.class));
        verify(customerEventPublisher, never()).publishCreated(any());
    }

    @Test
    void shouldGetCustomerByIdSuccessfully() {
        when(customerQueryPort.findById(1L)).thenReturn(Optional.of(customer));
        when(customerPersistenceMapper.toResult(customer)).thenReturn(customerResult);

        StepVerifier.create(customerApplicationService.getById(1L))
                .expectNextMatches(result -> result.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    void shouldFailGetByIdWhenCustomerDoesNotExist() {
        when(customerQueryPort.findById(99L)).thenReturn(Optional.empty());

        StepVerifier.create(customerApplicationService.getById(99L))
                .expectError(CustomerNotFoundException.class)
                .verify();
    }

    @Test
    void shouldGetAllCustomersSuccessfully() {
        when(customerQueryPort.findAll()).thenReturn(List.of(customer));
        when(customerPersistenceMapper.toResult(customer)).thenReturn(customerResult);

        StepVerifier.create(customerApplicationService.getAll())
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void shouldUpdateCustomerSuccessfully() {
        when(customerQueryPort.findById(1L)).thenReturn(Optional.of(customer));
        when(customerQueryPort.existsByIdentificationAndIdNot(updateCommand.getIdentification(), 1L)).thenReturn(false);

        Customer updatedCustomer = Customer.builder()
                .id(1L)
                .name(updateCommand.getName())
                .gender(updateCommand.getGender())
                .identification(updateCommand.getIdentification())
                .address(updateCommand.getAddress())
                .phone(updateCommand.getPhone())
                .password(updateCommand.getPassword())
                .status(updateCommand.getStatus())
                .build();

        CustomerResult updatedResult = CustomerResult.builder()
                .id(1L)
                .name(updateCommand.getName())
                .gender(updateCommand.getGender())
                .identification(updateCommand.getIdentification())
                .address(updateCommand.getAddress())
                .phone(updateCommand.getPhone())
                .status(updateCommand.getStatus())
                .build();

        when(customerCommandPort.save(any(Customer.class))).thenReturn(updatedCustomer);
        when(customerPersistenceMapper.toResult(updatedCustomer)).thenReturn(updatedResult);

        StepVerifier.create(customerApplicationService.update(1L, updateCommand))
                .expectNextMatches(result ->
                        result.getName().equals("Jose Lema Updated") &&
                                result.getAddress().equals("Quito"))
                .verifyComplete();

        verify(customerEventPublisher).publishUpdated(updatedCustomer);
    }

    @Test
    void shouldFailUpdateWhenCustomerDoesNotExist() {
        when(customerQueryPort.findById(1L)).thenReturn(Optional.empty());

        StepVerifier.create(customerApplicationService.update(1L, updateCommand))
                .expectError(CustomerNotFoundException.class)
                .verify();
    }

    @Test
    void shouldFailUpdateWhenIdentificationAlreadyExists() {
        when(customerQueryPort.findById(1L)).thenReturn(Optional.of(customer));
        when(customerQueryPort.existsByIdentificationAndIdNot(updateCommand.getIdentification(), 1L)).thenReturn(true);

        StepVerifier.create(customerApplicationService.update(1L, updateCommand))
                .expectError(DuplicateIdentificationException.class)
                .verify();

        verify(customerCommandPort, never()).save(any(Customer.class));
        verify(customerEventPublisher, never()).publishUpdated(any());
    }

    @Test
    void shouldDeleteCustomerSuccessfully() {
        when(customerQueryPort.findById(1L)).thenReturn(Optional.of(customer));
        doNothing().when(customerCommandPort).deleteById(1L);
        doNothing().when(customerEventPublisher).publishDeleted(1L);

        StepVerifier.create(customerApplicationService.delete(1L))
                .verifyComplete();

        verify(customerCommandPort).deleteById(1L);
        verify(customerEventPublisher).publishDeleted(1L);
    }

    @Test
    void shouldFailDeleteWhenCustomerDoesNotExist() {
        when(customerQueryPort.findById(1L)).thenReturn(Optional.empty());

        StepVerifier.create(customerApplicationService.delete(1L))
                .expectError(CustomerNotFoundException.class)
                .verify();

        verify(customerCommandPort, never()).deleteById(anyLong());
        verify(customerEventPublisher, never()).publishDeleted(anyLong());
    }
}