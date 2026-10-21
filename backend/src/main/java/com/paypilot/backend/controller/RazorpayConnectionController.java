package com.paypilot.backend.controller;

import com.paypilot.backend.domain.razorpay.RazorpayConnectionService;
import com.paypilot.backend.domain.razorpay.dto.RazorpayConnectionResponse;
import com.paypilot.backend.security.AuthenticatedUser;
import com.paypilot.backend.security.AuthenticatedUserContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/razorpay/connection")
public class RazorpayConnectionController {

    private final RazorpayConnectionService connectionService;
    private final AuthenticatedUserContext authenticatedUserContext;

    public RazorpayConnectionController(
            RazorpayConnectionService connectionService,
            AuthenticatedUserContext authenticatedUserContext
    ) {
        this.connectionService = connectionService;
        this.authenticatedUserContext = authenticatedUserContext;
    }

    @GetMapping
    public RazorpayConnectionResponse getConnection() {
        AuthenticatedUser user =
                authenticatedUserContext.getCurrentUser();

        return RazorpayConnectionResponse.from(
                connectionService.getConnection(user.merchantId())
        );
    }

    @DeleteMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> disconnect() {
        AuthenticatedUser user =
                authenticatedUserContext.getCurrentUser();

        connectionService.disconnect(user.merchantId());

        return ResponseEntity.noContent().build();
    }
}