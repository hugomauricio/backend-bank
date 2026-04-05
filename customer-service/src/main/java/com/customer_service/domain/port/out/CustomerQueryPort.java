package com.customer_service.domain.port.out;

import com.customer_service.domain.model.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerQueryPort {
    Optional<Customer> findById(Long id);
    List<Customer> findAll();
    boolean existsByIdentification(String identification);
    boolean existsByIdentificationAndIdNot(String identification, Long id);
}
