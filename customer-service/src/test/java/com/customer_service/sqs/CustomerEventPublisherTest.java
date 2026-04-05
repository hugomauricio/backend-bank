package com.customer_service.sqs;

import com.customer_service.domain.model.Customer;
import com.customer_service.domain.model.Gender;
import com.customer_service.infrastructure.config.RabbitConfig;
import com.customer_service.infrastructure.messaging.CustomerEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Optional;

import static org.mockito.Mockito.*;

class CustomerEventPublisherTest {

    private RabbitTemplate rabbitTemplate;
    private CustomerEventPublisher customerEventPublisher;

    @BeforeEach
    void setUp() {
        rabbitTemplate = mock(RabbitTemplate.class);
        customerEventPublisher = new CustomerEventPublisher(rabbitTemplate);
    }

    @Test
    void shouldPublishCreatedEvent() {
        Customer customer = Customer.builder()
                .id(1L)
                .name("Jose Lema")
                .gender(Gender.MALE)
                .identification("12345")
                .address("Otavalo")
                .phone("098254785")
                .password("1234")
                .status(true)
                .build();

        customerEventPublisher.publishCreated(customer);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitConfig.CUSTOMER_EXCHANGE),
                eq(RabbitConfig.CUSTOMER_ROUTING_KEY),
                Optional.ofNullable(any())
        );
    }

    @Test
    void shouldPublishDeletedEvent() {
        customerEventPublisher.publishDeleted(1L);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitConfig.CUSTOMER_EXCHANGE),
                eq(RabbitConfig.CUSTOMER_ROUTING_KEY),
                Optional.ofNullable(any())
        );
    }
}