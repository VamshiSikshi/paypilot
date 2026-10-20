package com.paypilot.backend.controller;

import com.paypilot.backend.config.SecurityConfig;
import com.paypilot.backend.domain.user.UserRole;
import com.paypilot.backend.domain.user.UserService;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        AuthenticatedUserContext.class,
        UserSecurityTest.FilterTestConfiguration.class
})
class UserSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    private String bearerToken(UserRole role) {
        return role.name().toLowerCase() + "-user-test-token";
    }

    private void authenticateAs(UserRole role) {
        Claims claims = mock(Claims.class);

        given(claims.getSubject()).willReturn("5");
        given(claims.get("merchantId", Long.class)).willReturn(10L);
        given(claims.get("role", String.class)).willReturn(role.name());

        given(jwtService.parseToken(bearerToken(role))).willReturn(claims);
    }

    @Test
    void shouldRejectUserRequestWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/v1/users/20"))
                .andExpect(status().isUnauthorized());

        then(userService).shouldHaveNoInteractions();
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void shouldDenyNonOwnerRolesForUserRead(UserRole role)
            throws Exception {
        if (role == UserRole.OWNER) {
            return;
        }

        authenticateAs(role);

        mockMvc.perform(get("/api/v1/users/20")
                        .header("Authorization", "Bearer " + bearerToken(role)))
                .andExpect(status().isForbidden());

        then(userService).shouldHaveNoInteractions();
    }

    @Test
    void shouldAllowOwnerToReachUserReadService() throws Exception {
        authenticateAs(UserRole.OWNER);

        mockMvc.perform(get("/api/v1/users/20")
                        .header("Authorization", "Bearer " + bearerToken(UserRole.OWNER)))
                .andExpect(status().isOk());

        then(userService).should()
                .getUser(10L, 20L);
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void shouldDenyNonOwnerRolesForAllUserMutations(UserRole role)
            throws Exception {
        if (role == UserRole.OWNER) {
            return;
        }

        authenticateAs(role);
        String token = bearerToken(role);

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "newuser@example.com",
                                  "displayName": "New User",
                                  "role": "VIEWER"
                                }
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/v1/users/20")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "updated@example.com",
                                  "displayName": "Updated User"
                                }
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/v1/users/20/role")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role": "FINANCE"}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/v1/users/20/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "SUSPENDED"}
                                """))
                .andExpect(status().isForbidden());

        then(userService).shouldHaveNoInteractions();
    }

    @Test
    void shouldAllowOwnerToCreateUser() throws Exception {
        authenticateAs(UserRole.OWNER);

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization",
                                "Bearer " + bearerToken(UserRole.OWNER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "newuser@example.com",
                              "displayName": "New User",
                              "role": "VIEWER"
                            }
                            """))
                .andExpect(status().isOk());

        then(userService).should().createUser(
                org.mockito.ArgumentMatchers.eq(10L),
                org.mockito.ArgumentMatchers.any(
                        com.paypilot.backend.domain.user.dto.CreateUserRequest.class)
        );
    }

    @Test
    void shouldAllowOwnerToUpdateUser() throws Exception {
        authenticateAs(UserRole.OWNER);

        mockMvc.perform(put("/api/v1/users/20")
                        .header("Authorization",
                                "Bearer " + bearerToken(UserRole.OWNER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "updated@example.com",
                              "displayName": "Updated User"
                            }
                            """))
                .andExpect(status().isOk());

        then(userService).should().updateUser(
                org.mockito.ArgumentMatchers.eq(10L),
                org.mockito.ArgumentMatchers.eq(20L),
                org.mockito.ArgumentMatchers.any(
                        com.paypilot.backend.domain.user.dto.UpdateUserRequest.class)
        );
    }

    @Test
    void shouldAllowOwnerToChangeUserRole() throws Exception {
        authenticateAs(UserRole.OWNER);

        mockMvc.perform(patch("/api/v1/users/20/role")
                        .header("Authorization",
                                "Bearer " + bearerToken(UserRole.OWNER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"role": "FINANCE"}
                            """))
                .andExpect(status().isOk());

        then(userService).should().updateRole(
                org.mockito.ArgumentMatchers.eq(10L),
                org.mockito.ArgumentMatchers.eq(20L),
                org.mockito.ArgumentMatchers.any(
                        com.paypilot.backend.domain.user.dto.UpdateUserRoleRequest.class)
        );
    }

    @Test
    void shouldAllowOwnerToChangeUserStatus() throws Exception {
        authenticateAs(UserRole.OWNER);

        mockMvc.perform(patch("/api/v1/users/20/status")
                        .header("Authorization",
                                "Bearer " + bearerToken(UserRole.OWNER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"status": "SUSPENDED"}
                            """))
                .andExpect(status().isOk());

        then(userService).should().updateStatus(
                org.mockito.ArgumentMatchers.eq(10L),
                org.mockito.ArgumentMatchers.eq(20L),
                org.mockito.ArgumentMatchers.any(
                        com.paypilot.backend.domain.user.dto.UpdateUserStatusRequest.class)
        );
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