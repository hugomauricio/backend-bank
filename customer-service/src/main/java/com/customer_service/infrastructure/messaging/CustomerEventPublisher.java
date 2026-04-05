package com.customer_service.infrastructure.messaging;

import com.customer_service.domain.model.Customer;
import com.customer_service.infrastructure.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class CustomerEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishCreated(Customer customer) {
        publish("customer.created", customer);
    }

    public void publishUpdated(Customer customer) {
        publish("customer.updated", customer);
    }

    public void publishDeleted(Long customerId) {
        CustomerEvent event = CustomerEvent.builder()
                .eventType("customer.deleted")
                .customerId(customerId)
                .occurredAt(OffsetDateTime.now())
                .build();

        rabbitTemplate.convertAndSend(
                RabbitConfig.CUSTOMER_EXCHANGE,
                RabbitConfig.CUSTOMER_ROUTING_KEY,
                event
        );
    }

    private void publish(String eventType, Customer customer) {
        CustomerEvent event = CustomerEvent.builder()
                .eventType(eventType)
                .customerId(customer.getId())
                .name(customer.getName())
                .identification(customer.getIdentification())
                .status(customer.getStatus())
                .occurredAt(OffsetDateTime.now())
                .build();

        rabbitTemplate.convertAndSend(
                RabbitConfig.CUSTOMER_EXCHANGE,
                RabbitConfig.CUSTOMER_ROUTING_KEY,
                event
        );
    }
}