package com.brokerage.core.api.admin;

import com.brokerage.core.api.asset.model.Asset;
import com.brokerage.core.api.asset.repository.AssetRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/setup/assets")
@RequiredArgsConstructor
@Profile({"dev","test"})
public class AdminAssetsController {

    private final AssetRepository assetRepository;

    public record UpsertAssetRequest(
            @NotNull UUID customerId,
            @NotBlank String assetName,
            @NotNull BigDecimal size,
            @NotNull BigDecimal usableSize
    ) {}

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/upsert")
    public ResponseEntity<?> upsert(@RequestBody UpsertAssetRequest req) {
        Asset asset = assetRepository.findByCustomerIdAndAssetName(req.customerId(), req.assetName())
                .orElseGet(() -> Asset.builder()
                        .customerId(req.customerId())
                        .assetName(req.assetName())
                        .size(BigDecimal.ZERO)
                        .usableSize(BigDecimal.ZERO)
                        .build());
        asset.setSize(req.size());
        asset.setUsableSize(req.usableSize());
        assetRepository.save(asset);
        return ResponseEntity.ok(Map.of("success", true));
    }
}

