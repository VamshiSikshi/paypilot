package com.paypilot.backend.controller;

import com.paypilot.backend.config.SecurityConfig;
import com.paypilot.backend.domain.notification.NotificationService;
import com.paypilot.backend.domain.user.UserRole;
import com.paypilot.backend.security.AuthenticatedUserContext;
import com.paypilot.backend.security.JwtAuthenticationFilter;
import com.paypilot.backend.security.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        AuthenticatedUserContext.class,
        NotificationSecurityTest.FilterTestConfiguration.class
})
class NotificationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private JwtService jwtService;

    private String bearerToken(UserRole role) {
        return role.name().toLowerCase() + "-notification-test-token";
    }

    private void authenticateAs(UserRole role) {
        Claims claims = mock(Claims.class);

        given(claims.getSubject()).willReturn("5");
        given(claims.get("merchantId", Long.class)).willReturn(10L);
        given(claims.get("role", String.class)).willReturn(role.name());

        given(jwtService.parseToken(bearerToken(role))).willReturn(claims);
    }

    @Test
    void shouldRejectNotificationReadWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isUnauthorized());

        then(notificationService).shouldHaveNoInteractions();
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void shouldAllowAllAuthenticatedRolesToReadNotifications(UserRole role)
            throws Exception {
        authenticateAs(role);

        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + bearerToken(role)))
                .andExpect(status().isOk());

        then(notificationService).should()
                .getNotifications(10L, 5L);
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void shouldAllowAllAuthenticatedRolesToReadUnreadNotifications(
            UserRole role
    ) throws Exception {
        authenticateAs(role);

        mockMvc.perform(get("/api/v1/notifications/unread")
                        .header("Authorization", "Bearer " + bearerToken(role)))
                .andExpect(status().isOk());

        then(notificationService).should()
                .getUnreadNotifications(10L, 5L);
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void shouldAllowAuthenticatedUsersToMarkTheirNotificationAsRead(
            UserRole role
    ) throws Exception {
        authenticateAs(role);

        mockMvc.perform(patch("/api/v1/notifications/25/read")
                        .header("Authorization", "Bearer " + bearerToken(role)))
                .andExpect(status().isOk());

        then(notificationService).should()
                .markAsRead(10L, 5L, 25L);
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void shouldAllowAuthenticatedUsersToArchiveTheirNotification(
            UserRole role
    ) throws Exception {
        authenticateAs(role);

        mockMvc.perform(patch("/api/v1/notifications/25/archive")
                        .header("Authorization", "Bearer " + bearerToken(role)))
                .andExpect(status().isOk());

        then(notificationService).should()
                .archiveNotification(10L, 5L, 25L);
    }

    @Test
    void shouldRejectNotificationMutationWithoutJwt() throws Exception {
        mockMvc.perform(patch("/api/v1/notifications/25/read"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/api/v1/notifications/25/archive"))
                .andExpect(status().isUnauthorized());

        then(notificationService).shouldHaveNoInteractions();
    }

    @TestConfiguration
    static class FilterTestConfiguration {

        @Bean
        FilterRegistrationBean<JwtAuthenticationFilter>
        jwtFilterServletRegistration(JwtAuthenticationFilter filter) {
            FilterRegistrationBean<JwtAuthenticationFilter> registration =
                    new FilterRegistrationBean<>(filter);
            registration.setEnabled(false);
            return registration;
        }
    }
}