package com.account_service.domain.port.in;

import com.account_service.application.dto.AccountResult;
import com.account_service.application.dto.CreateAccountCommand;
import reactor.core.publisher.Mono;

public interface CreateAccountUseCase {
    Mono<AccountResult> create(CreateAccountCommand command);
}
