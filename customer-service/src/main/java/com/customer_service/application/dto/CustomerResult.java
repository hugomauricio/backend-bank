package com.customer_service.application.dto;

import com.customer_service.domain.model.Gender;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class CustomerResult {
    private final Long id;
    private final String name;
    private final Gender gender;
    private final String identification;
    private final String address;
    private final String phone;
    private final Boolean status;
}
