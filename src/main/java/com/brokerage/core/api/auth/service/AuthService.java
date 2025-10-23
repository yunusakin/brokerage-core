package com.brokerage.core.api.auth.service;

import com.brokerage.core.api.auth.dto.AuthRequest;
import com.brokerage.core.api.auth.model.RefreshToken;
import com.brokerage.core.api.auth.repository.RefreshTokenRepository;
import com.brokerage.core.api.customer.model.Customer;
import com.brokerage.core.api.customer.respository.CustomerRepository;
import com.brokerage.core.base.constants.ErrorKeys;
import com.brokerage.core.base.enumaration.Role;
import com.brokerage.core.base.exception.BusinessException;
import com.brokerage.core.base.exception.ResourceNotFoundException;
import com.brokerage.core.base.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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

    public Map<String, Object> register(AuthRequest authRequest) {
        if (customerRepo.existsByUsername(authRequest.username())) {
            throw new RuntimeException("Username already exists!");
        }
        Customer c = Customer.builder()
                .username(authRequest.username())
                .password(passwordEncoder.encode(authRequest.password()))
                .role(Role.CUSTOMER)
                .build();
        customerRepo.save(c);
        return issuePair(c, "ROLE_CUSTOMER");
    }

    public Map<String, Object> login(AuthRequest authRequest) {
        var opt = customerRepo.findByUsername(authRequest.username());
        if (opt.isEmpty()) {
            throw new ResourceNotFoundException(ErrorKeys.USER_NOT_FOUND);
        }

        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.username(), authRequest.password()));
        } catch (BadCredentialsException ex) {
            throw new BusinessException(ErrorKeys.BAD_CREDENTIALS);
        }

        var c = opt.get();
        String role = "ROLE_" + c.getRole().name();
        return issuePair(c, role);
    }

    public Map<String, Object> refresh(String refreshToken) {
        if (jwt.isExpired(refreshToken, true)) throw new RuntimeException("Refresh token expired");
        String username = jwt.extractUsername(refreshToken, true);
        String jti = jwt.extractJti(refreshToken);

        Customer c = customerRepo.findByUsername(username).orElseThrow();

        var stored = refreshRepo.findByJtiAndCustomerId(jti, c.getId())
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token invalid");
        }

        stored.setRevoked(true);
        refreshRepo.save(stored);

        String role = "ROLE_" + c.getRole().name();
        return issuePair(c, role);
    }

    public void logout(String refreshToken) {
        try {
            String jti = jwt.extractJti(refreshToken);
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