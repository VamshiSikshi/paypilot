package com.paypilot.backend.controller;

import com.paypilot.backend.domain.payment.PaymentService;
import com.paypilot.backend.domain.payment.dto.PaymentResponse;
import com.paypilot.backend.security.AuthenticatedUser;
import com.paypilot.backend.security.AuthenticatedUserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final AuthenticatedUserContext authenticatedUserContext;

    public PaymentController(
            PaymentService paymentService,
            AuthenticatedUserContext authenticatedUserContext
    ) {
        this.paymentService = paymentService;
        this.authenticatedUserContext = authenticatedUserContext;
    }

    @GetMapping("/{paymentId}")
    public PaymentResponse getPayment(
            @PathVariable Long paymentId
    ) {
        AuthenticatedUser user =
                authenticatedUserContext.getCurrentUser();

        return paymentService.getPayment(
                user.merchantId(),
                paymentId
        );
    }
}
