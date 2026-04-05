package com.account_service.infrastructure.messaging;

import com.account_service.infrastructure.config.RabbitConfig;
import com.account_service.infrastructure.utput.persistence.CustomerSnapshotEntity;
import com.account_service.infrastructure.utput.persistence.CustomerSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerEventListener {

    private final CustomerSnapshotRepository customerSnapshotRepository;

    @RabbitListener(queues = RabbitConfig.CUSTOMER_QUEUE)
    public void onCustomerEvent(CustomerEvent event) {
        log.info("Received customer event type={} customerId={}", event.getEventType(), event.getCustomerId());

        switch (event.getEventType()) {
            case "customer.created", "customer.updated" -> upsertSnapshot(event);
            case "customer.deleted" -> customerSnapshotRepository.deleteById(event.getCustomerId());
            default -> log.warn("Unknown customer event type={}", event.getEventType());
        }
    }

    private void upsertSnapshot(CustomerEvent event) {
        CustomerSnapshotEntity entity = new CustomerSnapshotEntity();
        entity.setCustomerId(event.getCustomerId());
        entity.setName(event.getName());
        entity.setIdentification(event.getIdentification());
        entity.setStatus(event.getStatus());
        customerSnapshotRepository.save(entity);
    }
}