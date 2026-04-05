package com.customer_service.infrastructure.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerEvent {

    private String eventType;
    private Long customerId;
    private String name;
    private String identification;
    private Boolean status;
    private OffsetDateTime occurredAt;
}