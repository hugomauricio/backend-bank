package com.customer_service.infrastructure.mapper;

import com.bank.customer.generated.model.CustomerRequest;
import com.bank.customer.generated.model.CustomerResponse;
import com.customer_service.application.dto.CreateCustomerCommand;
import com.customer_service.application.dto.CustomerResult;
import com.customer_service.application.dto.UpdateCustomerCommand;
import com.customer_service.domain.model.Gender;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = Gender.class)
public interface CustomerContractMapper {

    @Mapping(target = "gender", expression = "java(Gender.valueOf(request.getGender().getValue()))")
    CreateCustomerCommand toCreateCommand(CustomerRequest request);

    @Mapping(target = "gender", expression = "java(Gender.valueOf(request.getGender().getValue()))")
    UpdateCustomerCommand toUpdateCommand(CustomerRequest request);

    @Mapping(target = "gender", expression = "java(CustomerResponse.GenderEnum.valueOf(result.getGender().name()))")
    CustomerResponse toResponse(CustomerResult result);
}