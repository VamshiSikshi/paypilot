
package com.paypilot.backend.controller;

import com.paypilot.backend.domain.refund.RefundService;
import com.paypilot.backend.domain.refund.dto.RefundResponse;
import com.paypilot.backend.security.AuthenticatedUser;
import com.paypilot.backend.security.AuthenticatedUserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/refunds")
public class RefundController {

    private final RefundService refundService;
    private final AuthenticatedUserContext authenticatedUserContext;

    public RefundController(
            RefundService refundService,
            AuthenticatedUserContext authenticatedUserContext
    ) {
        this.refundService = refundService;
        this.authenticatedUserContext = authenticatedUserContext;
    }

    @GetMapping("/{refundId}")
    public RefundResponse getRefund(@PathVariable Long refundId) {
        AuthenticatedUser user =
                authenticatedUserContext.getCurrentUser();

        return refundService.getRefund(
                user.merchantId(),
                refundId
        );
    }
}
