package com.customer_service.infrastructure.utput.persistence;

import com.customer_service.application.dto.CustomerResult;
import com.customer_service.domain.model.Customer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerPersistenceMapper {

    CustomerEntity toEntity(Customer customer);

    Customer toDomain(CustomerEntity entity);

    CustomerResult toResult(Customer customer);
}