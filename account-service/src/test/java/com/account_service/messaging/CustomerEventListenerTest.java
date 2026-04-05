package com.account_service.messaging;

import com.account_service.infrastructure.messaging.CustomerEvent;
import com.account_service.infrastructure.messaging.CustomerEventListener;
import com.account_service.infrastructure.utput.persistence.CustomerSnapshotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.mockito.Mockito.*;

class CustomerEventListenerTest {

    private CustomerSnapshotRepository customerSnapshotRepository;
    private CustomerEventListener customerEventListener;

    @BeforeEach
    void setUp() {
        customerSnapshotRepository = mock(CustomerSnapshotRepository.class);
        customerEventListener = new CustomerEventListener(customerSnapshotRepository);
    }

    @Test
    void shouldCreateOrUpdateSnapshotWhenCustomerCreatedEventIsReceived() {
        CustomerEvent event = CustomerEvent.builder()
                .eventType("customer.created")
                .customerId(1L)
                .name("Jose Lema")
                .identification("12345")
                .status(true)
                .occurredAt(OffsetDateTime.now())
                .build();

        customerEventListener.onCustomerEvent(event);

        verify(customerSnapshotRepository).save(any());
    }

    @Test
    void shouldDeleteSnapshotWhenCustomerDeletedEventIsReceived() {
        CustomerEvent event = CustomerEvent.builder()
                .eventType("customer.deleted")
                .customerId(1L)
                .occurredAt(OffsetDateTime.now())
                .build();

        customerEventListener.onCustomerEvent(event);

        verify(customerSnapshotRepository).deleteById(1L);
    }
}