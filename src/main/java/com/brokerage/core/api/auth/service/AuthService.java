package com.brokerage.core.api.auth.service;

import com.brokerage.core.api.auth.model.RefreshToken;
import com.brokerage.core.api.auth.repository.RefreshTokenRepository;
import com.brokerage.core.api.customer.model.Customer;
import com.brokerage.core.api.customer.respository.CustomerRepository;
import com.brokerage.core.base.enumaration.Role;
import com.brokerage.core.base.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CustomerRepository customerRepo;
    private final RefreshTokenRepository refreshRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwt;
    private final AuthenticationManager authManager;

    public Map<String, Object> register(String username, String password, String fullName) {
        if (customerRepo.existsByUsername(username)) {
            throw new RuntimeException("Username already exists!");
        }
        Customer c = Customer.builder()
                .username(username)
                .password(passwordEncoder.encode(password)) // 🔒 hashed in DB
                .role(Role.CUSTOMER)
                .build();
        customerRepo.save(c);
        return issuePair(c, "ROLE_CUSTOMER");
    }

    public Map<String, Object> login(String username, String password) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        Customer c = customerRepo.findByUsername(username).orElseThrow();
        String role = "ROLE_" + c.getRole().name();
        return issuePair(c, role);
    }

    public Map<String, Object> refresh(String refreshToken) {
        // 1) Validate token cryptographically / expiry
        if (jwt.isExpired(refreshToken, true)) throw new RuntimeException("Refresh token expired");
        String username = jwt.extractUsername(refreshToken, true);
        String jti = jwt.extractJti(refreshToken);

        // 2) Resolve customer by username (authoritative)
        Customer c = customerRepo.findByUsername(username).orElseThrow();

        // 3) Check stored token record (by jti + customerId) and state
        var stored = refreshRepo.findByJtiAndCustomerId(jti, c.getId())
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token invalid");
        }

        // 4) Rotate: revoke old, issue new pair
        stored.setRevoked(true);
        refreshRepo.save(stored);

        String role = "ROLE_" + c.getRole().name();
        return issuePair(c, role);
    }

    public void logout(String refreshToken) {
        try {
            String jti = jwt.extractJti(refreshToken);
            // if you want stricter logout, also compare username→customerId as above
            refreshRepo.findByJti(jti).ifPresent(t -> { t.setRevoked(true); refreshRepo.save(t); });
        } catch (Exception ignored) {}
    }

    private Map<String, Object> issuePair(Customer c, String role) {
        String access  = jwt.generateAccessToken(c.getUsername(), role);
        String newJti  = UUID.randomUUID().toString();
        String refresh = jwt.generateRefreshToken(c.getUsername(), role, newJti);

        refreshRepo.save(RefreshToken.builder()
                .jti(newJti)
                .customerId(c.getId())
                .expiresAt(Instant.now().plusSeconds(7 * 24 * 3600))
                .revoked(false)
                .build());

        return Map.of(
                "username", c.getUsername(),
                "role", role,
                "customerId", c.getId(),
                "accessToken", access,
                "refreshToken", refresh
        );
    }
}