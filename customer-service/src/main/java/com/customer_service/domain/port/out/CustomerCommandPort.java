package com.customer_service.domain.port.out;

import com.customer_service.domain.model.Customer;

public interface CustomerCommandPort {
    Customer save(Customer customer);
    void deleteById(Long id);
}