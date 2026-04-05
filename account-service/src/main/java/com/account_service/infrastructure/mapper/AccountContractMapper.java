package com.account_service.infrastructure.mapper;

import com.account_service.application.dto.*;
import com.account_service.domain.model.AccountType;
import com.account_service.domain.model.MovementType;
import com.bank.customer.generated.model.AccountRequest;
import com.bank.customer.generated.model.AccountResponse;
import com.bank.customer.generated.model.MovementRequest;
import com.bank.customer.generated.model.MovementResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring", imports = {AccountType.class, MovementType.class, BigDecimal.class})
public interface AccountContractMapper {

    @Mapping(target = "type", expression = "java(AccountType.valueOf(request.getType().getValue()))")
    @Mapping(target = "initialBalance", expression = "java(BigDecimal.valueOf(request.getInitialBalance()))")
    CreateAccountCommand toCreateAccountCommand(AccountRequest request);

    @Mapping(target = "type", expression = "java(AccountType.valueOf(request.getType().getValue()))")
    @Mapping(target = "initialBalance", expression = "java(BigDecimal.valueOf(request.getInitialBalance()))")
    UpdateAccountCommand toUpdateAccountCommand(AccountRequest request);

    @Mapping(target = "type", expression = "java(AccountResponse.TypeEnum.valueOf(result.getType().name()))")
    @Mapping(target = "initialBalance", expression = "java(result.getInitialBalance().doubleValue())")
    @Mapping(target = "currentBalance", expression = "java(result.getCurrentBalance().doubleValue())")
    AccountResponse toAccountResponse(AccountResult result);

    @Mapping(target = "type", expression = "java(MovementType.valueOf(request.getType().getValue()))")
    @Mapping(target = "value", expression = "java(BigDecimal.valueOf(request.getValue()))")
    RegisterMovementCommand toRegisterMovementCommand(MovementRequest request);

    @Mapping(target = "type", expression = "java(MovementResponse.TypeEnum.valueOf(result.getType().name()))")
    @Mapping(target = "value", expression = "java(result.getValue().doubleValue())")
    @Mapping(target = "balance", expression = "java(result.getBalance().doubleValue())")
    MovementResponse toMovementResponse(MovementResult result);

    default OffsetDateTime map(LocalDateTime value) {
        return value != null ? value.atOffset(ZoneOffset.UTC) : null;
    }
}