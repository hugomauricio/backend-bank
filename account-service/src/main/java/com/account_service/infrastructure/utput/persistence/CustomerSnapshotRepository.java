package com.account_service.infrastructure.utput.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerSnapshotRepository extends JpaRepository<CustomerSnapshotEntity, Long> {
}