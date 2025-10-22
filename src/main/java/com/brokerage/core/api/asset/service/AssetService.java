package com.brokerage.core.api.asset.service;

import com.brokerage.core.base.constants.ErrorKeys;
import com.brokerage.core.api.asset.dto.AssetDto;
import com.brokerage.core.api.asset.mapper.AssetMapper;
import com.brokerage.core.base.exception.ResourceNotFoundException;
import com.brokerage.core.api.asset.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final AssetMapper assetMapper;

    public List<AssetDto> getAssetsByCustomer(UUID customerId) {
        var assets = assetRepository.findByCustomerId(customerId);
        if (assets.isEmpty()) {
            throw new ResourceNotFoundException(ErrorKeys.ASSET_NOT_FOUND);
        }
        return assetMapper.toDtoList(assets);
    }
}