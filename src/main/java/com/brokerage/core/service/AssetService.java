package com.brokerage.core.service;

import com.brokerage.core.model.Asset;
import com.brokerage.core.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;

    public List<Asset> getAssetsByCustomer(UUID customerId) {
        return assetRepository.findByCustomerId(customerId);
    }

    public Asset getAsset(UUID customerId, String assetName) {
        return assetRepository.findByCustomerIdAndAssetName(customerId, assetName)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found: " + assetName));
    }

    @Transactional
    public void updateUsableSize(UUID customerId, String assetName, BigDecimal delta) {
        Asset asset = getAsset(customerId, assetName);
        asset.setUsableSize(asset.getUsableSize().add(delta));
        assetRepository.save(asset);
    }

    @Transactional
    public void updateSize(UUID customerId, String assetName, BigDecimal delta) {
        Asset asset = getAsset(customerId, assetName);
        asset.setSize(asset.getSize().add(delta));
        assetRepository.save(asset);
    }
}

