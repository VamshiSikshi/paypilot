
package com.paypilot.backend.controller;

import com.paypilot.backend.domain.settlement.SettlementService;
import com.paypilot.backend.domain.settlement.dto.SettlementResponse;
import com.paypilot.backend.security.AuthenticatedUser;
import com.paypilot.backend.security.AuthenticatedUserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/settlements")
public class SettlementController {

    private final SettlementService settlementService;
    private final AuthenticatedUserContext authenticatedUserContext;

    public SettlementController(
            SettlementService settlementService,
            AuthenticatedUserContext authenticatedUserContext
    ) {
        this.settlementService = settlementService;
        this.authenticatedUserContext = authenticatedUserContext;
    }

    @GetMapping("/{settlementId}")
    public SettlementResponse getSettlement(
            @PathVariable Long settlementId
    ) {
        AuthenticatedUser user =
                authenticatedUserContext.getCurrentUser();

        return settlementService.getSettlement(
                user.merchantId(),
                settlementId
        );
    }
}
