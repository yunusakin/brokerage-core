package com.brokerage.core.api.customer.controller;

import com.brokerage.core.base.constants.SuccessKeys;
import com.brokerage.core.api.customer.model.Customer;
import com.brokerage.core.base.response.BaseResponse;
import com.brokerage.core.api.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomer(@PathVariable UUID id) {
        var result = customerService.getCustomer(id);
        return ok(SuccessKeys.GENERIC_SUCCESS, result);
    }

    @PostMapping
    public ResponseEntity<?> createCustomer(@Valid @RequestBody Customer customer) {
        var result = customerService.createCustomer(customer);
        return created(SuccessKeys.CUSTOMER_CREATED, result);
    }
}

