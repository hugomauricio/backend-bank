package com.account_service.rest;

import com.account_service.AccountServiceApplication;
import com.account_service.domain.model.AccountType;
import com.account_service.infrastructure.utput.persistence.AccountEntity;
import com.account_service.infrastructure.utput.persistence.AccountRepository;
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

@Testcontainers
@SpringBootTest(
        classes = AccountServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class AccountControllerIntegrationTest {

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
    void shouldCreateAccountSuccessfully() {
        String requestBody = """
                {
                  "number": "478758",
                  "type": "SAVINGS",
                  "initialBalance": 2000.00,
                  "status": true,
                  "customerId": 10
                }
                """;

        webTestClient.post()
                .uri("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isNumber()
                .jsonPath("$.number").isEqualTo("478758")
                .jsonPath("$.type").isEqualTo("SAVINGS")
                .jsonPath("$.initialBalance").isEqualTo(2000.0)
                .jsonPath("$.currentBalance").isEqualTo(2000.0)
                .jsonPath("$.status").isEqualTo(true)
                .jsonPath("$.customerId").isEqualTo(10);
    }

    @Test
    void shouldReturnConflictWhenAccountNumberAlreadyExists() {
        AccountEntity account = buildAccount("478758", AccountType.SAVINGS, "2000.00", "2000.00", true, 10L);
        accountRepository.save(account);

        String requestBody = """
                {
                  "number": "478758",
                  "type": "SAVINGS",
                  "initialBalance": 3000.00,
                  "status": true,
                  "customerId": 20
                }
                """;

        webTestClient.post()
                .uri("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(409);
    }

    @Test
    void shouldReturnBadRequestWhenCreateRequestIsInvalid() {
        String requestBody = """
                {
                  "number": "",
                  "type": "SAVINGS",
                  "initialBalance": 2000.00,
                  "status": true,
                  "customerId": 10
                }
                """;

        webTestClient.post()
                .uri("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    void shouldGetAccountByIdSuccessfully() {
        AccountEntity account = buildAccount("478758", AccountType.SAVINGS, "2000.00", "2000.00", true, 10L);
        AccountEntity savedAccount = accountRepository.save(account);

        webTestClient.get()
                .uri("/api/v1/accounts/{id}", savedAccount.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(savedAccount.getId())
                .jsonPath("$.number").isEqualTo("478758")
                .jsonPath("$.type").isEqualTo("SAVINGS")
                .jsonPath("$.initialBalance").isEqualTo(2000.0)
                .jsonPath("$.currentBalance").isEqualTo(2000.0)
                .jsonPath("$.status").isEqualTo(true)
                .jsonPath("$.customerId").isEqualTo(10);
    }

    @Test
    void shouldReturnNotFoundWhenAccountDoesNotExist() {
        webTestClient.get()
                .uri("/api/v1/accounts/{id}", 999999L)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void shouldGetAllAccountsSuccessfully() {
        AccountEntity account1 = buildAccount("478758", AccountType.SAVINGS, "2000.00", "2000.00", true, 10L);
        AccountEntity account2 = buildAccount("225487", AccountType.CURRENT, "100.00", "100.00", true, 10L);

        accountRepository.save(account1);
        accountRepository.save(account2);

        webTestClient.get()
                .uri("/api/v1/accounts")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2);
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoAccounts() {
        webTestClient.get()
                .uri("/api/v1/accounts")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .json("[]");
    }

    @Test
    void shouldUpdateAccountSuccessfully() {
        AccountEntity account = buildAccount("478758", AccountType.SAVINGS, "2000.00", "2000.00", true, 10L);
        AccountEntity savedAccount = accountRepository.save(account);

        String requestBody = """
                {
                  "number": "478758",
                  "type": "CURRENT",
                  "initialBalance": 3000.00,
                  "status": true,
                  "customerId": 10
                }
                """;

        webTestClient.put()
                .uri("/api/v1/accounts/{id}", savedAccount.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(savedAccount.getId())
                .jsonPath("$.number").isEqualTo("478758")
                .jsonPath("$.type").isEqualTo("CURRENT")
                .jsonPath("$.initialBalance").isEqualTo(3000.0)
                .jsonPath("$.currentBalance").isEqualTo(2000.0)
                .jsonPath("$.customerId").isEqualTo(10);
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingAccountThatDoesNotExist() {
        String requestBody = """
                {
                  "number": "478758",
                  "type": "CURRENT",
                  "initialBalance": 3000.00,
                  "status": true,
                  "customerId": 10
                }
                """;

        webTestClient.put()
                .uri("/api/v1/accounts/{id}", 999999L)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void shouldReturnConflictWhenUpdatingWithDuplicateAccountNumber() {
        AccountEntity account1 = buildAccount("478758", AccountType.SAVINGS, "2000.00", "2000.00", true, 10L);
        AccountEntity account2 = buildAccount("225487", AccountType.CURRENT, "100.00", "100.00", true, 20L);

        AccountEntity saved1 = accountRepository.save(account1);
        accountRepository.save(account2);

        String requestBody = """
                {
                  "number": "225487",
                  "type": "CURRENT",
                  "initialBalance": 3000.00,
                  "status": true,
                  "customerId": 10
                }
                """;

        webTestClient.put()
                .uri("/api/v1/accounts/{id}", saved1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.status").isEqualTo(409);
    }

    @Test
    void shouldDeleteAccountSuccessfully() {
        AccountEntity account = buildAccount("478758", AccountType.SAVINGS, "2000.00", "2000.00", true, 10L);
        AccountEntity savedAccount = accountRepository.save(account);

        webTestClient.delete()
                .uri("/api/v1/accounts/{id}", savedAccount.getId())
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void shouldReturnNotFoundWhenDeletingAccountThatDoesNotExist() {
        webTestClient.delete()
                .uri("/api/v1/accounts/{id}", 999999L)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);
    }

    private AccountEntity buildAccount(String number,
                                       AccountType type,
                                       String initialBalance,
                                       String currentBalance,
                                       boolean status,
                                       Long customerId) {
        AccountEntity account = new AccountEntity();
        account.setNumber(number);
        account.setType(type);
        account.setInitialBalance(new BigDecimal(initialBalance));
        account.setCurrentBalance(new BigDecimal(currentBalance));
        account.setStatus(status);
        account.setCustomerId(customerId);
        return account;
    }
}