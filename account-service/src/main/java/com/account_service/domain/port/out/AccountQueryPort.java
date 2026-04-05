package com.account_service.domain.port.out;

import com.account_service.domain.model.Account;

import java.util.List;
import java.util.Optional;

public interface AccountQueryPort {
    Optional<Account> findById(Long id);

    List<Account> findAll();

    boolean existsByNumber(String number);

    boolean existsByNumberAndIdNot(String number, Long id);

    List<Account> findAllByCustomerId(Long customerId);
}
