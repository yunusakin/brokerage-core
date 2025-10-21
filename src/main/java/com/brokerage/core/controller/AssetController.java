package com.brokerage.core.controller;


import com.brokerage.core.controller.dto.AssetDto;
import com.brokerage.core.controller.mapper.AssetMapper;
import com.brokerage.core.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;
    private final AssetMapper assetMapper;

    @GetMapping
    public ResponseEntity<List<AssetDto>> getAssets(@RequestParam UUID customerId) {
        var assets = assetService.getAssetsByCustomer(customerId);
        return ResponseEntity.ok(assetMapper.toDtoList(assets));
    }
}

