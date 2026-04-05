package com.customer_service.infrastructure.utput.persistence;

import com.customer_service.domain.model.Customer;
import com.customer_service.domain.port.out.CustomerCommandPort;
import com.customer_service.domain.port.out.CustomerQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomerJpaAdapter implements CustomerCommandPort, CustomerQueryPort {

    private final CustomerRepository customerRepository;
    private final CustomerPersistenceMapper customerPersistenceMapper;

    @Override
    public Customer save(Customer customer) {
        CustomerEntity entity = customerPersistenceMapper.toEntity(customer);
        CustomerEntity saved = customerRepository.save(entity);
        return customerPersistenceMapper.toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        customerRepository.deleteById(id);
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id)
                .map(customerPersistenceMapper::toDomain);
    }

    @Override
    public List<Customer> findAll() {
        return customerRepository.findAll()
                .stream()
                .map(customerPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByIdentification(String identification) {
        return customerRepository.existsByIdentification(identification);
    }

    @Override
    public boolean existsByIdentificationAndIdNot(String identification, Long id) {
        return customerRepository.existsByIdentificationAndIdNot(identification, id);
    }
}