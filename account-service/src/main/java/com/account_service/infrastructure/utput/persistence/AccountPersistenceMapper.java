package com.account_service.infrastructure.utput.persistence;

import com.account_service.application.dto.AccountResult;
import com.account_service.application.dto.MovementResult;
import com.account_service.domain.model.Account;
import com.account_service.domain.model.Movement;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountPersistenceMapper {
    AccountEntity toEntity(Account account);

    Account toDomain(AccountEntity entity);

    AccountResult toAccountResult(Account account);

    MovementEntity toEntity(Movement movement);

    Movement toDomain(MovementEntity entity);

    MovementResult toMovementResult(Movement movement);
}