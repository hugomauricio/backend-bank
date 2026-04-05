package com.account_service.domain.port.in;

import com.account_service.application.dto.AccountResult;
import com.account_service.application.dto.UpdateAccountCommand;
import reactor.core.publisher.Mono;

public interface UpdateAccountUseCase {
    Mono<AccountResult> update(Long id, UpdateAccountCommand command);
}
