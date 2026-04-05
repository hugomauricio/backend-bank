package com.account_service.service;

import com.account_service.application.dto.MovementResult;
import com.account_service.application.dto.RegisterMovementCommand;
import com.account_service.application.service.MovementApplicationService;
import com.account_service.domain.exception.AccountNotFoundException;
import com.account_service.domain.exception.InsufficientBalanceException;
import com.account_service.domain.exception.InvalidMovementValueException;
import com.account_service.domain.model.Account;
import com.account_service.domain.model.AccountType;
import com.account_service.domain.model.Movement;
import com.account_service.domain.model.MovementType;
import com.account_service.domain.port.out.AccountCommandPort;
import com.account_service.domain.port.out.AccountQueryPort;
import com.account_service.domain.port.out.MovementCommandPort;
import com.account_service.domain.strategy.MovementStrategy;
import com.account_service.domain.strategy.MovementStrategyFactory;
import com.account_service.infrastructure.utput.persistence.AccountPersistenceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovementApplicationServiceTest {

    @Mock
    private AccountQueryPort accountQueryPort;
    @Mock
    private AccountCommandPort accountCommandPort;
    @Mock
    private MovementCommandPort movementCommandPort;

    @Mock
    private MovementStrategyFactory movementStrategyFactory;
    @Mock
    private AccountPersistenceMapper mapper;

    @InjectMocks
    private MovementApplicationService movementApplicationService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(1L)
                .number("478758")
                .type(AccountType.SAVINGS)
                .initialBalance(new BigDecimal("2000.00"))
                .currentBalance(new BigDecimal("2000.00"))
                .status(true)
                .customerId(10L)
                .build();
    }

    @Test
    void shouldCreateCreditMovementSuccessfully() {
        RegisterMovementCommand command = RegisterMovementCommand.builder()
                .accountId(1L)
                .type(MovementType.CREDIT)
                .value(new BigDecimal("500.00"))
                .build();

        MovementStrategy creditStrategy = mock(MovementStrategy.class);

        when(accountQueryPort.findById(1L)).thenReturn(Optional.of(account));
        when(movementStrategyFactory.get(MovementType.CREDIT)).thenReturn(creditStrategy);
        when(creditStrategy.calculate(new BigDecimal("2000.00"), new BigDecimal("500.00")))
                .thenReturn(new BigDecimal("2500.00"));
        when(accountCommandPort.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Movement savedMovement = Movement.builder()
                .id(1L)
                .accountId(1L)
                .type(MovementType.CREDIT)
                .value(new BigDecimal("500.00"))
                .balance(new BigDecimal("2500.00"))
                .build();

        when(movementCommandPort.save(any(Movement.class))).thenReturn(savedMovement);

        MovementResult movementResult = MovementResult.builder()
                .id(1L)
                .accountId(1L)
                .type(MovementType.CREDIT)
                .value(new BigDecimal("500.00"))
                .balance(new BigDecimal("2500.00"))
                .build();

        when(mapper.toMovementResult(savedMovement)).thenReturn(movementResult);

        StepVerifier.create(movementApplicationService.create(command))
                .expectNextMatches(result ->
                        result.getId().equals(1L) &&
                                result.getBalance().compareTo(new BigDecimal("2500.00")) == 0)
                .verifyComplete();
    }

    @Test
    void shouldCreateDebitMovementSuccessfully() {
        RegisterMovementCommand command = RegisterMovementCommand.builder()
                .accountId(1L)
                .type(MovementType.DEBIT)
                .value(new BigDecimal("500.00"))
                .build();

        MovementStrategy debitStrategy = mock(MovementStrategy.class);

        when(accountQueryPort.findById(1L)).thenReturn(Optional.of(account));
        when(movementStrategyFactory.get(MovementType.DEBIT)).thenReturn(debitStrategy);
        when(debitStrategy.calculate(new BigDecimal("2000.00"), new BigDecimal("500.00")))
                .thenReturn(new BigDecimal("1500.00"));
        when(accountCommandPort.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Movement savedMovement = Movement.builder()
                .id(1L)
                .accountId(1L)
                .type(MovementType.DEBIT)
                .value(new BigDecimal("500.00"))
                .balance(new BigDecimal("1500.00"))
                .build();

        when(movementCommandPort.save(any(Movement.class))).thenReturn(savedMovement);

        MovementResult movementResult = MovementResult.builder()
                .id(1L)
                .accountId(1L)
                .type(MovementType.DEBIT)
                .value(new BigDecimal("500.00"))
                .balance(new BigDecimal("1500.00"))
                .build();

        when(mapper.toMovementResult(savedMovement)).thenReturn(movementResult);

        StepVerifier.create(movementApplicationService.create(command))
                .expectNextMatches(result ->
                        result.getBalance().compareTo(new BigDecimal("1500.00")) == 0)
                .verifyComplete();
    }

    @Test
    void shouldFailWhenMovementValueIsZero() {
        RegisterMovementCommand command = RegisterMovementCommand.builder()
                .accountId(1L)
                .type(MovementType.DEBIT)
                .value(BigDecimal.ZERO)
                .build();

        StepVerifier.create(movementApplicationService.create(command))
                .expectError(InvalidMovementValueException.class)
                .verify();
    }

    @Test
    void shouldFailWhenAccountDoesNotExist() {
        RegisterMovementCommand command = RegisterMovementCommand.builder()
                .accountId(99L)
                .type(MovementType.DEBIT)
                .value(new BigDecimal("100.00"))
                .build();

        when(accountQueryPort.findById(99L)).thenReturn(Optional.empty());

        StepVerifier.create(movementApplicationService.create(command))
                .expectError(AccountNotFoundException.class)
                .verify();
    }

    @Test
    void shouldFailWhenBalanceIsInsufficient() {
        RegisterMovementCommand command = RegisterMovementCommand.builder()
                .accountId(1L)
                .type(MovementType.DEBIT)
                .value(new BigDecimal("5000.00"))
                .build();

        MovementStrategy debitStrategy = mock(MovementStrategy.class);

        when(accountQueryPort.findById(1L)).thenReturn(Optional.of(account));
        when(movementStrategyFactory.get(MovementType.DEBIT)).thenReturn(debitStrategy);
        when(debitStrategy.calculate(new BigDecimal("2000.00"), new BigDecimal("5000.00")))
                .thenThrow(new InsufficientBalanceException());

        StepVerifier.create(movementApplicationService.create(command))
                .expectError(InsufficientBalanceException.class)
                .verify();
    }
}