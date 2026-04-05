package com.account_service.infrastructure.utput.persistence;

import com.account_service.domain.model.Account;
import com.account_service.domain.port.out.AccountCommandPort;
import com.account_service.domain.port.out.AccountQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AccountJpaAdapter implements AccountCommandPort, AccountQueryPort {

    private final AccountRepository accountRepository;
    private final AccountPersistenceMapper mapper;

    @Override
    public Account save(Account account) {
        return mapper.toDomain(accountRepository.save(mapper.toEntity(account)));
    }

    @Override
    public void deleteById(Long id) {
        accountRepository.deleteById(id);
    }

    @Override
    public Optional<Account> findById(Long id) {
        return accountRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Account> findAll() {
        return accountRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByNumber(String number) {
        return accountRepository.existsByNumber(number);
    }

    @Override
    public boolean existsByNumberAndIdNot(String number, Long id) {
        return accountRepository.existsByNumberAndIdNot(number, id);
    }

    @Override
    public List<Account> findAllByCustomerId(Long customerId) {
        return accountRepository.findAllByCustomerId(customerId).stream().map(mapper::toDomain).toList();
    }
}