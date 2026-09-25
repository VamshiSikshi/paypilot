package com.paypilot.backend.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUserContext {

    public AuthenticatedUser getCurrentUser() {
        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof AuthenticatedUser authenticatedUser) {
            return authenticatedUser;
        }

        throw new IllegalStateException("No authenticated user found");
    }
}