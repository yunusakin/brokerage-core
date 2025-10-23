package com.brokerage.core.api.admin;

import com.brokerage.core.api.admin.dto.AdminCreateRequest;
import com.brokerage.core.api.customer.model.Customer;
import com.brokerage.core.api.customer.repository.CustomerRepository;
import com.brokerage.core.base.enumaration.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/setup")
@RequiredArgsConstructor
@Profile({"dev", "local", "test"})
public class AdminSetupController {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/admin")
    public ResponseEntity<?> createAdmin(@Valid @RequestBody AdminCreateRequest request) {

        if (customerRepository.existsByUsername(request.username())) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Admin already exists"));
        }

        Customer admin = Customer.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ADMIN)
                .build();

        customerRepository.save(admin);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "username", request.username(),
                "password", request.password(),
                "id", admin.getId(),
                "message", "Admin user created successfully for test use"
        ));
    }
}

