package com.brokerage.core.api.customer.respository;

import com.brokerage.core.api.customer.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByUsername(String username);
    boolean existsByUsername(String username);
}
