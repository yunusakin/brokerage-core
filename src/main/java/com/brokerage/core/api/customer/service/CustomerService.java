package com.brokerage.core.api.customer.service;

import com.brokerage.core.base.constants.ErrorKeys;
import com.brokerage.core.api.customer.dto.CustomerDto;
import com.brokerage.core.api.customer.mapper.CustomerMapper;
import com.brokerage.core.base.exception.ResourceNotFoundException;
import com.brokerage.core.api.customer.model.Customer;
import com.brokerage.core.api.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final PasswordEncoder passwordEncoder;

    public CustomerDto getCustomer(UUID id) {
        var c = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorKeys.CUSTOMER_NOT_FOUND));
        return customerMapper.toDto(c);
    }

    public CustomerDto createCustomer(CustomerDto customerDto) {
        Customer toSave = customerMapper.toEntity(customerDto);
        toSave.setPassword(passwordEncoder.encode(toSave.getPassword()));
        if (toSave.getRole() == null) {
            toSave.setRole(com.brokerage.core.base.enumaration.Role.CUSTOMER);
        }
        var saved = customerRepository.save(toSave);
        return customerMapper.toDto(saved);
    }
}