package com.brokerage.core.api.asset.controller;


import com.brokerage.core.base.constants.SuccessKeys;
import com.brokerage.core.base.response.BaseResponse;
import com.brokerage.core.api.asset.service.AssetService;
import org.springframework.context.MessageSource;
import org.springframework.security.access.AccessDeniedException;
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
    private final com.brokerage.core.base.security.CurrentUserService currentUser;

    public AssetController(MessageSource messageSource, AssetService assetService, com.brokerage.core.base.security.CurrentUserService currentUser) {
        super(messageSource);
        this.assetService = assetService;
        this.currentUser = currentUser;
    }

    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @GetMapping("/{customerId}")
    public ResponseEntity<?> getAssets(@PathVariable UUID customerId) {
        if (!currentUser.isAdmin()) {
            UUID self = currentUser.currentCustomerId();
            if (self == null || !self.equals(customerId)) {
                throw new AccessDeniedException(com.brokerage.core.base.constants.ErrorKeys.ACCESS_DENIED);
            }
        }
        var result = assetService.getAssetsByCustomer(customerId);
        return ok(SuccessKeys.ASSET_FETCHED, result);
    }
}

