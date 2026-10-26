package com.paypilot.backend.domain.razorpay;

import com.paypilot.backend.domain.razorpay.dto.RazorpayOAuthTokenResponse;

public interface RazorpayOAuthTokenExchangeService {

    RazorpayOAuthTokenResponse exchangeAuthorizationCode(String code);
}
