package com.account_service.service;

import com.account_service.application.dto.AccountResult;
import com.account_service.application.dto.CreateAccountCommand;
import com.account_service.application.dto.UpdateAccountCommand;
import com.account_service.application.service.AccountApplicationService;
import com.account_service.domain.exception.AccountNotFoundException;
import com.account_service.domain.exception.DuplicateAccountNumberException;
import com.account_service.domain.model.Account;
import com.account_service.domain.model.AccountType;
import com.account_service.domain.port.out.AccountCommandPort;
import com.account_service.domain.port.out.AccountQueryPort;
import com.account_service.infrastructure.utput.persistence.AccountPersistenceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountApplicationServiceTest {

    @Mock
    private AccountCommandPort accountCommandPort;

    @Mock
    private AccountQueryPort accountQueryPort;

    @Mock
    private AccountPersistenceMapper mapper;

    @InjectMocks
    private AccountApplicationService accountApplicationService;

    private CreateAccountCommand createCommand;
    private UpdateAccountCommand updateCommand;
    private Account account;
    private AccountResult accountResult;

    @BeforeEach
    void setUp() {
        createCommand = CreateAccountCommand.builder()
                .number("478758")
                .type(AccountType.SAVINGS)
                .initialBalance(new BigDecimal("2000.00"))
                .status(true)
                .customerId(10L)
                .build();

        updateCommand = UpdateAccountCommand.builder()
                .number("478758")
                .type(AccountType.CURRENT)
                .initialBalance(new BigDecimal("3000.00"))
                .status(true)
                .customerId(10L)
                .build();

        account = Account.builder()
                .id(1L)
                .number("478758")
                .type(AccountType.SAVINGS)
                .initialBalance(new BigDecimal("2000.00"))
                .currentBalance(new BigDecimal("2000.00"))
                .status(true)
                .customerId(10L)
                .build();

        accountResult = AccountResult.builder()
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
    void shouldCreateAccountSuccessfully() {
        when(accountQueryPort.existsByNumber(createCommand.getNumber())).thenReturn(false);
        when(accountCommandPort.save(any(Account.class))).thenReturn(account);
        when(mapper.toAccountResult(account)).thenReturn(accountResult);

        StepVerifier.create(accountApplicationService.create(createCommand))
                .expectNextMatches(result ->
                        result.getId().equals(1L) &&
                                result.getNumber().equals("478758") &&
                                result.getCurrentBalance().compareTo(new BigDecimal("2000.00")) == 0)
                .verifyComplete();

        verify(accountQueryPort).existsByNumber(createCommand.getNumber());
        verify(accountCommandPort).save(any(Account.class));
        verify(mapper).toAccountResult(account);
    }

    @Test
    void shouldFailCreateWhenAccountNumberAlreadyExists() {
        when(accountQueryPort.existsByNumber(createCommand.getNumber())).thenReturn(true);

        StepVerifier.create(accountApplicationService.create(createCommand))
                .expectError(DuplicateAccountNumberException.class)
                .verify();

        verify(accountQueryPort).existsByNumber(createCommand.getNumber());
        verify(accountCommandPort, never()).save(any(Account.class));
        verify(mapper, never()).toAccountResult(any(Account.class));
    }

    @Test
    void shouldGetAccountByIdSuccessfully() {
        when(accountQueryPort.findById(1L)).thenReturn(Optional.of(account));
        when(mapper.toAccountResult(account)).thenReturn(accountResult);

        StepVerifier.create(accountApplicationService.getById(1L))
                .expectNextMatches(result ->
                        result.getId().equals(1L) &&
                                result.getNumber().equals("478758"))
                .verifyComplete();

        verify(accountQueryPort).findById(1L);
        verify(mapper).toAccountResult(account);
    }

    @Test
    void shouldFailGetByIdWhenAccountDoesNotExist() {
        when(accountQueryPort.findById(99L)).thenReturn(Optional.empty());

        StepVerifier.create(accountApplicationService.getById(99L))
                .expectError(AccountNotFoundException.class)
                .verify();

        verify(accountQueryPort).findById(99L);
        verify(mapper, never()).toAccountResult(any(Account.class));
    }

    @Test
    void shouldGetAllAccountsSuccessfully() {
        when(accountQueryPort.findAll()).thenReturn(List.of(account));
        when(mapper.toAccountResult(account)).thenReturn(accountResult);

        StepVerifier.create(accountApplicationService.getAll())
                .expectNextMatches(result -> result.getId().equals(1L))
                .verifyComplete();

        verify(accountQueryPort).findAll();
        verify(mapper).toAccountResult(account);
    }

    @Test
    void shouldUpdateAccountSuccessfully() {
        when(accountQueryPort.findById(1L)).thenReturn(Optional.of(account));
        when(accountQueryPort.existsByNumberAndIdNot(updateCommand.getNumber(), 1L)).thenReturn(false);

        Account updatedAccount = Account.builder()
                .id(1L)
                .number(updateCommand.getNumber())
                .type(updateCommand.getType())
                .initialBalance(updateCommand.getInitialBalance())
                .currentBalance(new BigDecimal("2000.00"))
                .status(updateCommand.getStatus())
                .customerId(updateCommand.getCustomerId())
                .build();

        AccountResult updatedResult = AccountResult.builder()
                .id(1L)
                .number(updateCommand.getNumber())
                .type(updateCommand.getType())
                .initialBalance(updateCommand.getInitialBalance())
                .currentBalance(new BigDecimal("2000.00"))
                .status(updateCommand.getStatus())
                .customerId(updateCommand.getCustomerId())
                .build();

        when(accountCommandPort.save(any(Account.class))).thenReturn(updatedAccount);
        when(mapper.toAccountResult(updatedAccount)).thenReturn(updatedResult);

        StepVerifier.create(accountApplicationService.update(1L, updateCommand))
                .expectNextMatches(result ->
                        result.getId().equals(1L) &&
                                result.getType().equals(AccountType.CURRENT) &&
                                result.getInitialBalance().compareTo(new BigDecimal("3000.00")) == 0 &&
                                result.getCurrentBalance().compareTo(new BigDecimal("2000.00")) == 0)
                .verifyComplete();

        verify(accountQueryPort, times(2)).findById(1L);
        verify(accountQueryPort).existsByNumberAndIdNot(updateCommand.getNumber(), 1L);
        verify(accountCommandPort).save(any(Account.class));
        verify(mapper).toAccountResult(updatedAccount);
    }

    @Test
    void shouldFailUpdateWhenAccountDoesNotExist() {
        when(accountQueryPort.findById(1L)).thenReturn(Optional.empty());

        StepVerifier.create(accountApplicationService.update(1L, updateCommand))
                .expectError(AccountNotFoundException.class)
                .verify();

        verify(accountQueryPort).findById(1L);
        verify(accountCommandPort, never()).save(any(Account.class));
        verify(mapper, never()).toAccountResult(any(Account.class));
    }

    @Test
    void shouldFailUpdateWhenAccountNumberAlreadyExists() {
        when(accountQueryPort.findById(1L)).thenReturn(Optional.of(account));
        when(accountQueryPort.existsByNumberAndIdNot(updateCommand.getNumber(), 1L)).thenReturn(true);

        StepVerifier.create(accountApplicationService.update(1L, updateCommand))
                .expectError(DuplicateAccountNumberException.class)
                .verify();

        verify(accountQueryPort).findById(1L);
        verify(accountQueryPort).existsByNumberAndIdNot(updateCommand.getNumber(), 1L);
        verify(accountCommandPort, never()).save(any(Account.class));
        verify(mapper, never()).toAccountResult(any(Account.class));
    }

    @Test
    void shouldDeleteAccountSuccessfully() {
        when(accountQueryPort.findById(1L)).thenReturn(Optional.of(account));
        doNothing().when(accountCommandPort).deleteById(1L);

        StepVerifier.create(accountApplicationService.delete(1L))
                .verifyComplete();

        verify(accountQueryPort).findById(1L);
        verify(accountCommandPort).deleteById(1L);
    }

    @Test
    void shouldFailDeleteWhenAccountDoesNotExist() {
        when(accountQueryPort.findById(1L)).thenReturn(Optional.empty());

        StepVerifier.create(accountApplicationService.delete(1L))
                .expectError(AccountNotFoundException.class)
                .verify();

        verify(accountQueryPort).findById(1L);
        verify(accountCommandPort, never()).deleteById(anyLong());
    }
}