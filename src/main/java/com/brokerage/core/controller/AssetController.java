package com.brokerage.core.controller;


import com.brokerage.core.constants.SuccessKeys;
import com.brokerage.core.response.BaseResponse;
import com.brokerage.core.service.AssetService;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/{customerId}")
    public ResponseEntity<?> getAssets(@PathVariable UUID customerId) {
        var result = assetService.getAssetsByCustomer(customerId);
        return ok(SuccessKeys.ASSET_FETCHED, result);
    }
}

