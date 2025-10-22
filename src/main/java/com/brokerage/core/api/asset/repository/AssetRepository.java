package com.brokerage.core.api.asset.repository;

import com.brokerage.core.api.asset.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetRepository extends JpaRepository<Asset, UUID> {

    List<Asset> findByCustomerId(UUID customerId);
    Optional<Asset> findByCustomerIdAndAssetName(UUID customerId, String assetName);
}
