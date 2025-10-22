package com.brokerage.core.api.auth.repository;

import com.brokerage.core.api.auth.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByJti(String jti);
    Optional<RefreshToken> findByJtiAndCustomerId(String jti, UUID customerId); // extra safety
}

