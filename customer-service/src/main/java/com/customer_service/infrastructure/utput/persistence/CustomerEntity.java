package com.customer_service.infrastructure.utput.persistence;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "customers")
@PrimaryKeyJoinColumn(name = "customer_id")
public class CustomerEntity extends PersonEntity {

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Boolean status;
}
