
package com.paypilot.backend.controller;

import com.paypilot.backend.domain.reconciliation.ReconciliationService;
import com.paypilot.backend.domain.reconciliation.dto.ReconciliationResponse;
import com.paypilot.backend.security.AuthenticatedUser;
import com.paypilot.backend.security.AuthenticatedUserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reconciliations")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;
    private final AuthenticatedUserContext authenticatedUserContext;

    public ReconciliationController(
            ReconciliationService reconciliationService,
            AuthenticatedUserContext authenticatedUserContext
    ) {
        this.reconciliationService = reconciliationService;
        this.authenticatedUserContext = authenticatedUserContext;
    }

    @GetMapping("/{reconciliationId}")
    public ReconciliationResponse getReconciliation(
            @PathVariable Long reconciliationId
    ) {
        AuthenticatedUser user =
                authenticatedUserContext.getCurrentUser();

        return reconciliationService.getReconciliation(
                user.merchantId(),
                reconciliationId
        );
    }

    @GetMapping
    public List<ReconciliationResponse> getReconciliationsBySettlement(
            @RequestParam Long settlementId
    ) {
        AuthenticatedUser user =
                authenticatedUserContext.getCurrentUser();

        return reconciliationService.getReconciliationsBySettlement(
                user.merchantId(),
                settlementId
        );
    }

    @GetMapping("/by-razorpay-entity")
    public ReconciliationResponse getReconciliationByRazorpayEntity(
            @RequestParam String razorpayEntityId
    ) {
        AuthenticatedUser user =
                authenticatedUserContext.getCurrentUser();

        return reconciliationService.getReconciliationByRazorpayEntity(
                user.merchantId(),
                razorpayEntityId
        );
    }
}
