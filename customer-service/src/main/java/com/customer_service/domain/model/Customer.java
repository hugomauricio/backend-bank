package com.customer_service.domain.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class Customer extends Person {
    private final Long id;
    private final String password;
    private final Boolean status;
}
