package com.brokerage.core.controller;

import com.brokerage.core.controller.dto.CustomerDto;
import com.brokerage.core.controller.mapper.CustomerMapper;
import com.brokerage.core.model.Customer;
import com.brokerage.core.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerMapper customerMapper;

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> getCustomer(@PathVariable UUID id) {
        var customer = customerService.findById(id);
        return ResponseEntity.ok(customerMapper.toDto(customer));
    }

    @PostMapping
    public ResponseEntity<CustomerDto> createCustomer(@RequestBody Customer customer) {
        var saved = customerService.save(customer);
        return ResponseEntity.ok(customerMapper.toDto(saved));
    }
}

