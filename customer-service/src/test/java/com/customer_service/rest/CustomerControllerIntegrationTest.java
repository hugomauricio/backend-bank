package com.customer_service.rest;

import com.customer_service.CustomerServiceApplication;
import com.customer_service.domain.model.Gender;
import com.customer_service.infrastructure.utput.persistence.CustomerEntity;
import com.customer_service.infrastructure.utput.persistence.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@Testcontainers
@SpringBootTest(
        classes = CustomerServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class CustomerControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("bank_db")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CustomerRepository customerRepository;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void cleanUp() {
        customerRepository.deleteAll();
        doNothing().when(rabbitTemplate).convertAndSend(any(String.class), any(String.class), Optional.ofNullable(any()));
    }

    @Test
    void shouldCreateCustomerSuccessfully() {
        String requestBody = """
                {
                  "name": "Jose Lema",
                  "gender": "MALE",
                  "identification": "1234567890",
                  "address": "Otavalo sn y principal",
                  "phone": "098254785",
                  "password": "1234",
                  "status": true
                }
                """;

        webTestClient.post()
                .uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isNumber()
                .jsonPath("$.name").isEqualTo("Jose Lema")
                .jsonPath("$.identification").isEqualTo("1234567890")
                .jsonPath("$.status").isEqualTo(true);
    }

    @Test
    void shouldGetCustomerByIdSuccessfully() {
        CustomerEntity customer = new CustomerEntity();
        customer.setName("Marianela Montalvo");
        customer.setGender(Gender.FEMALE);
        customer.setIdentification("0987654321");
        customer.setAddress("Amazonas y nnuu");
        customer.setPhone("098754521");
        customer.setPassword("5678");
        customer.setStatus(true);

        CustomerEntity savedCustomer = customerRepository.save(customer);

        webTestClient.get()
                .uri("/api/v1/customers/{id}", savedCustomer.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(savedCustomer.getId())
                .jsonPath("$.name").isEqualTo("Marianela Montalvo")
                .jsonPath("$.identification").isEqualTo("0987654321");
    }

    @Test
    void shouldGetAllCustomersSuccessfully() {
        CustomerEntity customer = new CustomerEntity();
        customer.setName("Jose Lema");
        customer.setGender(Gender.MALE);
        customer.setIdentification("12345");
        customer.setAddress("Otavalo");
        customer.setPhone("098254785");
        customer.setPassword("1234");
        customer.setStatus(true);

        customerRepository.save(customer);

        webTestClient.get()
                .uri("/api/v1/customers")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].name").isEqualTo("Jose Lema");
    }

    @Test
    void shouldUpdateCustomerSuccessfully() {
        CustomerEntity customer = new CustomerEntity();
        customer.setName("Jose Lema");
        customer.setGender(Gender.MALE);
        customer.setIdentification("12345");
        customer.setAddress("Otavalo");
        customer.setPhone("098254785");
        customer.setPassword("1234");
        customer.setStatus(true);

        CustomerEntity savedCustomer = customerRepository.save(customer);

        String requestBody = """
                {
                  "name": "Jose Lema Updated",
                  "gender": "MALE",
                  "identification": "12345",
                  "address": "Quito",
                  "phone": "099999999",
                  "password": "5678",
                  "status": true
                }
                """;

        webTestClient.put()
                .uri("/api/v1/customers/{id}", savedCustomer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Jose Lema Updated")
                .jsonPath("$.address").isEqualTo("Quito");
    }

    @Test
    void shouldDeleteCustomerSuccessfully() {
        CustomerEntity customer = new CustomerEntity();
        customer.setName("Jose Lema");
        customer.setGender(Gender.MALE);
        customer.setIdentification("12345");
        customer.setAddress("Otavalo");
        customer.setPhone("098254785");
        customer.setPassword("1234");
        customer.setStatus(true);

        CustomerEntity savedCustomer = customerRepository.save(customer);

        webTestClient.delete()
                .uri("/api/v1/customers/{id}", savedCustomer.getId())
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void shouldReturnNotFoundWhenCustomerDoesNotExist() {
        webTestClient.get()
                .uri("/api/v1/customers/{id}", 99999L)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void shouldReturnConflictWhenIdentificationAlreadyExists() {
        CustomerEntity customer = new CustomerEntity();
        customer.setName("Jose Lema");
        customer.setGender(Gender.MALE);
        customer.setIdentification("12345");
        customer.setAddress("Otavalo");
        customer.setPhone("098254785");
        customer.setPassword("1234");
        customer.setStatus(true);

        customerRepository.save(customer);

        String requestBody = """
                {
                  "name": "Otro Cliente",
                  "gender": "MALE",
                  "identification": "12345",
                  "address": "Quito",
                  "phone": "099999999",
                  "password": "5678",
                  "status": true
                }
                """;

        webTestClient.post()
                .uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.status").isEqualTo(409);
    }
}