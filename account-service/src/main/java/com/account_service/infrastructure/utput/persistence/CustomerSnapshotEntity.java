package com.account_service.infrastructure.utput.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "customer_snapshot")
public class CustomerSnapshotEntity {

    @Id
    private Long customerId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String identification;

    @Column(nullable = false)
    private Boolean status;
}