package com.customer_service.rest;

import com.bank.customer.generated.model.CustomerRequest;
import com.bank.customer.generated.model.CustomerResponse;
import com.customer_service.application.dto.CreateCustomerCommand;
import com.customer_service.application.dto.CustomerResult;
import com.customer_service.application.dto.UpdateCustomerCommand;
import com.customer_service.domain.model.Gender;
import com.customer_service.domain.port.in.CreateCustomerUseCase;
import com.customer_service.domain.port.in.DeleteCustomerUseCase;
import com.customer_service.domain.port.in.GetCustomerUseCase;
import com.customer_service.domain.port.in.UpdateCustomerUseCase;
import com.customer_service.infrastructure.input.rest.CustomerController;
import com.customer_service.infrastructure.mapper.CustomerContractMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    private CreateCustomerUseCase createCustomerUseCase;
    @Mock
    private UpdateCustomerUseCase updateCustomerUseCase;
    @Mock
    private GetCustomerUseCase getCustomerUseCase;
    @Mock
    private DeleteCustomerUseCase deleteCustomerUseCase;
    @Mock
    private CustomerContractMapper customerContractMapper;
    @Mock
    private ServerWebExchange serverWebExchange;

    @InjectMocks
    private CustomerController customerController;

    private CustomerRequest customerRequest;
    private CustomerResponse customerResponse;
    private CustomerResult customerResult;

    @BeforeEach
    void setUp() {
        customerRequest = new CustomerRequest();
        customerRequest.setName("Jose Lema");
        customerRequest.setGender(CustomerRequest.GenderEnum.MALE);
        customerRequest.setIdentification("12345");
        customerRequest.setAddress("Otavalo");
        customerRequest.setPhone("098254785");
        customerRequest.setPassword("1234");
        customerRequest.setStatus(true);

        customerResult = CustomerResult.builder()
                .id(1L)
                .name("Jose Lema")
                .gender(Gender.MALE)
                .identification("12345")
                .address("Otavalo")
                .phone("098254785")
                .status(true)
                .build();

        customerResponse = new CustomerResponse();
        customerResponse.setId(1L);
        customerResponse.setName("Jose Lema");
        customerResponse.setGender(CustomerResponse.GenderEnum.MALE);
        customerResponse.setIdentification("12345");
        customerResponse.setAddress("Otavalo");
        customerResponse.setPhone("098254785");
        customerResponse.setStatus(true);
    }

    @Test
    void shouldCreateCustomerSuccessfully() {
        when(customerContractMapper.toCreateCommand(any(CustomerRequest.class)))
                .thenReturn(CreateCustomerCommand.builder().build());
        when(createCustomerUseCase.create(any()))
                .thenReturn(Mono.just(customerResult));
        when(customerContractMapper.toResponse(customerResult))
                .thenReturn(customerResponse);

        ResponseEntity<CustomerResponse> response = customerController
                .createCustomer(Mono.just(customerRequest), serverWebExchange)
                .block();

        assertNotNull(response);
        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Jose Lema", response.getBody().getName());
    }

    @Test
    void shouldGetCustomerByIdSuccessfully() {
        when(getCustomerUseCase.getById(1L)).thenReturn(Mono.just(customerResult));
        when(customerContractMapper.toResponse(customerResult)).thenReturn(customerResponse);

        ResponseEntity<CustomerResponse> response = customerController
                .getCustomerById(1L, serverWebExchange)
                .block();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void shouldGetAllCustomersSuccessfully() {
        when(getCustomerUseCase.getAll()).thenReturn(Flux.just(customerResult));
        when(customerContractMapper.toResponse(customerResult)).thenReturn(customerResponse);

        ResponseEntity<Flux<CustomerResponse>> response = customerController
                .getCustomers(serverWebExchange)
                .block();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());

        CustomerResponse body = response.getBody().blockFirst();
        assertNotNull(body);
        assertEquals("Jose Lema", body.getName());
    }

    @Test
    void shouldUpdateCustomerSuccessfully() {
        when(customerContractMapper.toUpdateCommand(any(CustomerRequest.class)))
                .thenReturn(UpdateCustomerCommand.builder().build());
        when(updateCustomerUseCase.update(anyLong(), any()))
                .thenReturn(Mono.just(customerResult));
        when(customerContractMapper.toResponse(customerResult))
                .thenReturn(customerResponse);

        ResponseEntity<CustomerResponse> response = customerController
                .updateCustomer(1L, Mono.just(customerRequest), serverWebExchange)
                .block();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Jose Lema", response.getBody().getName());
    }

    @Test
    void shouldDeleteCustomerSuccessfully() {
        when(deleteCustomerUseCase.delete(1L)).thenReturn(Mono.empty());

        ResponseEntity<Void> response = customerController
                .deleteCustomer(1L, serverWebExchange)
                .block();

        assertNotNull(response);
        assertEquals(204, response.getStatusCode().value());
    }
}
