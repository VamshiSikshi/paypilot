package com.paypilot.backend.controller;

import com.paypilot.backend.domain.merchant.MerchantService;
import com.paypilot.backend.domain.merchant.dto.MerchantResponse;
import com.paypilot.backend.domain.merchant.dto.UpdateMerchantStatusRequest;
import com.paypilot.backend.security.AuthenticatedUser;
import com.paypilot.backend.security.AuthenticatedUserContext;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/merchant")
public class MerchantController {

    private final MerchantService merchantService;
    private final AuthenticatedUserContext authenticatedUserContext;

    public MerchantController(
            MerchantService merchantService,
            AuthenticatedUserContext authenticatedUserContext
    ) {
        this.merchantService = merchantService;
        this.authenticatedUserContext = authenticatedUserContext;
    }

    @GetMapping
    public MerchantResponse getMerchant() {
        AuthenticatedUser user = authenticatedUserContext.getCurrentUser();

        return merchantService.getMerchant(user.merchantId());
    }

    @PatchMapping("/status")
    @PreAuthorize("hasRole('OWNER')")
    public MerchantResponse updateStatus(
            @Valid @RequestBody UpdateMerchantStatusRequest request
    ) {
        AuthenticatedUser user = authenticatedUserContext.getCurrentUser();

        return merchantService.updateStatus(
                user.merchantId(),
                user.userId(),
                request.status()
        );
    }
}