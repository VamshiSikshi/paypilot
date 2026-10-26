package com.paypilot.backend.domain.razorpay;

public interface RazorpayOAuthService {

    String createAuthorizationUrl(Long merchantId);
}
