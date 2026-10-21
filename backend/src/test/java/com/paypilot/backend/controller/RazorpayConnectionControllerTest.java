package com.paypilot.backend.controller;

import com.paypilot.backend.config.SecurityConfig;
import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.razorpay.RazorpayConnection;
import com.paypilot.backend.domain.razorpay.RazorpayConnectionService;
import com.paypilot.backend.domain.user.UserRole;
import com.paypilot.backend.security.AuthenticatedUser;
import com.paypilot.backend.security.AuthenticatedUserContext;
import com.paypilot.backend.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RazorpayConnectionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
class RazorpayConnectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RazorpayConnectionService connectionService;

    @MockitoBean
    private AuthenticatedUserContext authenticatedUserContext;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldReturnConnectionForAuthenticatedMerchant() throws Exception {
        Long merchantId = 10L;

        Merchant merchant = new Merchant(
                "acc_merchant_test",
                "Test Business",
                "IN",
                "INR",
                "Asia/Kolkata"
        );

        RazorpayConnection connection =
                new RazorpayConnection(merchant, "acct_external_test");

        given(authenticatedUserContext.getCurrentUser())
                .willReturn(new AuthenticatedUser(
                        5L, merchantId, UserRole.OWNER
                ));

        given(connectionService.getConnection(merchantId))
                .willReturn(connection);

        mockMvc.perform(get("/api/v1/razorpay/connection"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalAccountId")
                        .value("acct_external_test"))
                .andExpect(jsonPath("$.status").value("CONNECTED"))
                .andExpect(jsonPath("$.accessTokenReference").doesNotExist())
                .andExpect(jsonPath("$.refreshTokenReference").doesNotExist());

        then(connectionService).should().getConnection(merchantId);
    }

    @Test
    @WithMockUser(roles = "OWNER")
    void shouldDisconnectConnectionForOwner() throws Exception {
        Long merchantId = 10L;

        given(authenticatedUserContext.getCurrentUser())
                .willReturn(new AuthenticatedUser(
                        5L, merchantId, UserRole.OWNER
                ));

        mockMvc.perform(delete("/api/v1/razorpay/connection"))
                .andExpect(status().isNoContent());

        then(connectionService).should().disconnect(merchantId);
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void shouldRejectDisconnectForViewer() throws Exception {
        given(authenticatedUserContext.getCurrentUser())
                .willReturn(new AuthenticatedUser(
                        5L, 10L, UserRole.VIEWER
                ));

        org.junit.jupiter.api.Assertions.assertThrows(
                jakarta.servlet.ServletException.class,
                () -> mockMvc.perform(
                        delete("/api/v1/razorpay/connection")
                )
        );

        then(connectionService).shouldHaveNoInteractions();
    }
}