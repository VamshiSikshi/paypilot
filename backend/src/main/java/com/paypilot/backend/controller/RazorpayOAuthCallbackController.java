package com.paypilot.backend.controller;

import com.paypilot.backend.config.RazorpayOAuthProperties;
import com.paypilot.backend.domain.razorpay.RazorpayOAuthConnectionService;
import com.paypilot.backend.domain.razorpay.RazorpayOAuthStateService;
import com.paypilot.backend.domain.razorpay.RazorpayOAuthTokenExchangeService;
import com.paypilot.backend.domain.razorpay.dto.RazorpayOAuthTokenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

@RestController
public class RazorpayOAuthCallbackController {

    private final RazorpayOAuthStateService stateService;
    private final RazorpayOAuthTokenExchangeService tokenExchangeService;
    private final RazorpayOAuthConnectionService connectionService;
    private final RazorpayOAuthProperties properties;

    public RazorpayOAuthCallbackController(
            RazorpayOAuthStateService stateService,
            RazorpayOAuthTokenExchangeService tokenExchangeService,
            RazorpayOAuthConnectionService connectionService,
            RazorpayOAuthProperties properties
    ) {
        this.stateService = stateService;
        this.tokenExchangeService = tokenExchangeService;
        this.connectionService = connectionService;
        this.properties = properties;
    }

    @GetMapping("/api/v1/razorpay/callback")
    public ResponseEntity<String> handleCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error
    ) {
        if (state == null || state.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Missing OAuth state"
            );
        }

        Optional<Long> merchantId =
                stateService.consumeStateAndGetMerchantId(state);

        if (merchantId.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid, expired, or already-used OAuth state"
            );
        }

        if (error != null && !error.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Razorpay authorization was not completed"
            );
        }

        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Missing authorization code"
            );
        }

        RazorpayOAuthTokenResponse tokenResponse;

        try {
            tokenResponse =
                    tokenExchangeService.exchangeAuthorizationCode(code);
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to complete Razorpay authorization"
            );
        }

        Instant tokenExpiresAt = tokenResponse.expiresIn() == null
                ? null
                : Instant.now().plusSeconds(tokenResponse.expiresIn());

        String grantedScopes =
                tokenResponse.scope() == null
                        || tokenResponse.scope().isBlank()
                        ? properties.scope()
                        : tokenResponse.scope();

        try {
            connectionService.completeAuthorization(
                    merchantId.get(),
                    tokenResponse.razorpayAccountId(),
                    tokenResponse.accessToken(),
                    tokenResponse.refreshToken(),
                    tokenExpiresAt,
                    grantedScopes
            );
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to save the Razorpay connection"
            );
        }

        return ResponseEntity.ok(
                "Razorpay account connected successfully."
        );
    }
}