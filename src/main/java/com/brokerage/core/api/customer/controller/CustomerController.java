package com.brokerage.core.api.customer.controller;

import com.brokerage.core.api.customer.dto.CustomerDto;
import com.brokerage.core.api.customer.service.CustomerService;
import com.brokerage.core.base.constants.SuccessKeys;
import com.brokerage.core.base.response.BaseResponse;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
public class CustomerController extends BaseResponse {

    private final CustomerService customerService;

    public CustomerController(MessageSource messageSource, CustomerService customerService) {
        super(messageSource);
        this.customerService = customerService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomer(@PathVariable UUID id) {
        return ok(SuccessKeys.GENERIC_SUCCESS, customerService.getCustomer(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> createCustomer(@Valid @RequestBody CustomerDto customerDto) {
        return created(SuccessKeys.CUSTOMER_CREATED, customerService.createCustomer(customerDto));
    }
}

