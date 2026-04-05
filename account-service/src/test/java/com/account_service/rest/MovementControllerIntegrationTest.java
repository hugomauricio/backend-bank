package com.account_service.rest;

import com.account_service.AccountServiceApplication;
import com.account_service.domain.model.AccountType;
import com.account_service.domain.model.MovementType;
import com.account_service.infrastructure.utput.persistence.AccountEntity;
import com.account_service.infrastructure.utput.persistence.AccountRepository;
import com.account_service.infrastructure.utput.persistence.MovementEntity;
import com.account_service.infrastructure.utput.persistence.MovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Testcontainers
@SpringBootTest(
        classes = AccountServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class MovementControllerIntegrationTest {

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
    private AccountRepository accountRepository;

    @Autowired
    private MovementRepository movementRepository;

    @BeforeEach
    void setUp() {
        movementRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    void shouldCreateCreditMovementSuccessfully() {
        AccountEntity account = buildAccount("478758", "2000.00");
        AccountEntity savedAccount = accountRepository.save(account);

        String requestBody = """
                {
                  "accountId": %d,
                  "type": "CREDIT",
                  "value": 500.00
                }
                """.formatted(savedAccount.getId());

        webTestClient.post()
                .uri("/api/v1/movements")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isNumber()
                .jsonPath("$.accountId").isEqualTo(savedAccount.getId())
                .jsonPath("$.type").isEqualTo("CREDIT")
                .jsonPath("$.value").isEqualTo(500.0)
                .jsonPath("$.balance").isEqualTo(2500.0);
    }

    @Test
    void shouldCreateDebitMovementSuccessfully() {
        AccountEntity account = buildAccount("478759", "2000.00");
        AccountEntity savedAccount = accountRepository.save(account);

        String requestBody = """
                {
                  "accountId": %d,
                  "type": "DEBIT",
                  "value": 500.00
                }
                """.formatted(savedAccount.getId());

        webTestClient.post()
                .uri("/api/v1/movements")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isNumber()
                .jsonPath("$.accountId").isEqualTo(savedAccount.getId())
                .jsonPath("$.type").isEqualTo("DEBIT")
                .jsonPath("$.value").isEqualTo(500.0)
                .jsonPath("$.balance").isEqualTo(1500.0);
    }

    @Test
    void shouldReturnUnprocessableEntityWhenBalanceIsInsufficient() {
        AccountEntity account = buildAccount("478760", "100.00");
        AccountEntity savedAccount = accountRepository.save(account);

        String requestBody = """
                {
                  "accountId": %d,
                  "type": "DEBIT",
                  "value": 500.00
                }
                """.formatted(savedAccount.getId());

        webTestClient.post()
                .uri("/api/v1/movements")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(422)
                .jsonPath("$.message").isEqualTo("Saldo no disponible");
    }

    @Test
    void shouldReturnUnprocessableEntityWhenMovementValueIsZero() {
        AccountEntity account = buildAccount("478761", "1000.00");
        AccountEntity savedAccount = accountRepository.save(account);

        String requestBody = """
                {
                  "accountId": %d,
                  "type": "DEBIT",
                  "value": 0
                }
                """.formatted(savedAccount.getId());

        webTestClient.post()
                .uri("/api/v1/movements")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody()
                .jsonPath("$.status").isEqualTo(422);
    }

    @Test
    void shouldReturnNotFoundWhenAccountDoesNotExist() {
        String requestBody = """
                {
                  "accountId": 999999,
                  "type": "DEBIT",
                  "value": 100.00
                }
                """;

        webTestClient.post()
                .uri("/api/v1/movements")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void shouldReturnBadRequestWhenRequestBodyIsInvalid() {
        String requestBody = """
                {
                  "accountId": null,
                  "type": "DEBIT",
                  "value": 100.00
                }
                """;

        webTestClient.post()
                .uri("/api/v1/movements")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    void shouldGetMovementByIdSuccessfully() {
        AccountEntity account = buildAccount("478762", "1000.00");
        AccountEntity savedAccount = accountRepository.save(account);

        MovementEntity movement = new MovementEntity();
        movement.setAccountId(savedAccount.getId());
        movement.setDate(LocalDateTime.now());
        movement.setType(MovementType.CREDIT);
        movement.setValue(new BigDecimal("200.00"));
        movement.setBalance(new BigDecimal("1200.00"));

        MovementEntity savedMovement = movementRepository.save(movement);

        webTestClient.get()
                .uri("/api/v1/movements/{id}", savedMovement.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(savedMovement.getId())
                .jsonPath("$.accountId").isEqualTo(savedAccount.getId())
                .jsonPath("$.type").isEqualTo("CREDIT")
                .jsonPath("$.value").isEqualTo(200.0)
                .jsonPath("$.balance").isEqualTo(1200.0);
    }

    @Test
    void shouldReturnErrorWhenMovementDoesNotExist() {
        webTestClient.get()
                .uri("/api/v1/movements/{id}", 999999L)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldGetAllMovementsSuccessfully() {
        AccountEntity account = buildAccount("478763", "1000.00");
        AccountEntity savedAccount = accountRepository.save(account);

        MovementEntity movement1 = new MovementEntity();
        movement1.setAccountId(savedAccount.getId());
        movement1.setDate(LocalDateTime.now());
        movement1.setType(MovementType.CREDIT);
        movement1.setValue(new BigDecimal("200.00"));
        movement1.setBalance(new BigDecimal("1200.00"));

        MovementEntity movement2 = new MovementEntity();
        movement2.setAccountId(savedAccount.getId());
        movement2.setDate(LocalDateTime.now());
        movement2.setType(MovementType.DEBIT);
        movement2.setValue(new BigDecimal("100.00"));
        movement2.setBalance(new BigDecimal("1100.00"));

        movementRepository.save(movement1);
        movementRepository.save(movement2);

        webTestClient.get()
                .uri("/api/v1/movements")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2);
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoMovements() {
        webTestClient.get()
                .uri("/api/v1/movements")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .json("[]");
    }

    private AccountEntity buildAccount(String number, String currentBalance) {
        AccountEntity account = new AccountEntity();
        account.setNumber(number);
        account.setType(AccountType.SAVINGS);
        account.setInitialBalance(new BigDecimal(currentBalance));
        account.setCurrentBalance(new BigDecimal(currentBalance));
        account.setStatus(true);
        account.setCustomerId(10L);
        return account;
    }
}