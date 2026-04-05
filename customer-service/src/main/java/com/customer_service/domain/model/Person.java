package com.customer_service.domain.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class Person {
    private final String name;
    private final Gender gender;
    private final String identification;
    private final String address;
    private final String phone;
}