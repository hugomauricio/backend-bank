package com.customer_service.infrastructure.utput.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {

    boolean existsByIdentification(String identification);

    boolean existsByIdentificationAndIdNot(String identification, Long id);
}