package com.brokerage.core.api.asset.controller;


import com.brokerage.core.base.constants.SuccessKeys;
import com.brokerage.core.base.response.BaseResponse;
import com.brokerage.core.api.asset.service.AssetService;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
@RequestMapping("/api/assets")
public class AssetController extends BaseResponse {

    private final AssetService assetService;

    public AssetController(MessageSource messageSource, AssetService assetService) {
        super(messageSource);
        this.assetService = assetService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{customerId}")
    public ResponseEntity<?> getAssets(@PathVariable UUID customerId) {
        var result = assetService.getAssetsByCustomer(customerId);
        return ok(SuccessKeys.ASSET_FETCHED, result);
    }
}

