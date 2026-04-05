package com.account_service.infrastructure.input.rest;

import com.account_service.domain.port.in.CreateAccountUseCase;
import com.account_service.domain.port.in.DeleteAccountUseCase;
import com.account_service.domain.port.in.GetAccountUseCase;
import com.account_service.domain.port.in.UpdateAccountUseCase;
import com.account_service.infrastructure.mapper.AccountContractMapper;
import com.bank.customer.generated.api.AccountsApi;
import com.bank.customer.generated.model.AccountRequest;
import com.bank.customer.generated.model.AccountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class AccountController implements AccountsApi {

    private final CreateAccountUseCase createAccountUseCase;
    private final UpdateAccountUseCase updateAccountUseCase;
    private final GetAccountUseCase getAccountUseCase;
    private final DeleteAccountUseCase deleteAccountUseCase;
    private final AccountContractMapper mapper;

    @Override
    public Mono<ResponseEntity<AccountResponse>> createAccount(Mono<AccountRequest> accountRequest, ServerWebExchange exchange) {
        return accountRequest
                .map(mapper::toCreateAccountCommand)
                .flatMap(createAccountUseCase::create)
                .map(mapper::toAccountResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteAccount(Long accountId, ServerWebExchange exchange) {
        return deleteAccountUseCase.delete(accountId)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @Override
    public Mono<ResponseEntity<AccountResponse>> getAccountById(Long accountId, ServerWebExchange exchange) {
        return getAccountUseCase.getById(accountId)
                .map(mapper::toAccountResponse)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Flux<AccountResponse>>> getAccounts(ServerWebExchange exchange) {
        Flux<AccountResponse> response = getAccountUseCase.getAll().map(mapper::toAccountResponse);
        return Mono.just(ResponseEntity.ok(response));
    }

    @Override
    public Mono<ResponseEntity<AccountResponse>> updateAccount(Long accountId, Mono<AccountRequest> accountRequest, ServerWebExchange exchange) {
        return accountRequest
                .map(mapper::toUpdateAccountCommand)
                .flatMap(command -> updateAccountUseCase.update(accountId, command))
                .map(mapper::toAccountResponse)
                .map(ResponseEntity::ok);
    }
}