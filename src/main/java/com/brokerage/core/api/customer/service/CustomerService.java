package com.brokerage.core.api.customer.service;

import com.brokerage.core.base.constants.ErrorKeys;
import com.brokerage.core.api.customer.dto.CustomerDto;
import com.brokerage.core.api.customer.mapper.CustomerMapper;
import com.brokerage.core.base.exception.ResourceNotFoundException;
import com.brokerage.core.api.customer.model.Customer;
import com.brokerage.core.api.customer.respository.CustomerRepository;
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