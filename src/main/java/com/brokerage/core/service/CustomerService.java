package com.brokerage.core.service;

import com.brokerage.core.constants.ErrorKeys;
import com.brokerage.core.controller.dto.CustomerDto;
import com.brokerage.core.controller.mapper.CustomerMapper;
import com.brokerage.core.exception.ResourceNotFoundException;
import com.brokerage.core.model.Customer;
import com.brokerage.core.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerDto getCustomer(UUID id) {
        var c = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorKeys.CUSTOMER_NOT_FOUND));
        return customerMapper.toDto(c);
    }

    public CustomerDto createCustomer(Customer customer) {
        var saved = customerRepository.save(customer);
        return customerMapper.toDto(saved);
    }
}