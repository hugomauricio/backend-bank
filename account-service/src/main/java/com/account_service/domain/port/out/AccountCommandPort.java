package com.account_service.domain.port.out;


import com.account_service.domain.model.Account;

public interface AccountCommandPort {
    Account save(Account account);

    void deleteById(Long id);
}
