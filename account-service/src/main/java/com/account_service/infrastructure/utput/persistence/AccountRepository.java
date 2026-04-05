package com.account_service.infrastructure.utput.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
    boolean existsByNumber(String number);

    boolean existsByNumberAndIdNot(String number, Long id);

    List<AccountEntity> findAllByCustomerId(Long customerId);
}