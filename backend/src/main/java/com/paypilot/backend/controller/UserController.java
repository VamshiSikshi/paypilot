package com.paypilot.backend.controller;

import com.paypilot.backend.domain.user.UserService;
import com.paypilot.backend.domain.user.dto.CreateUserRequest;
import com.paypilot.backend.domain.user.dto.UpdateUserRequest;
import com.paypilot.backend.domain.user.dto.UpdateUserRoleRequest;
import com.paypilot.backend.domain.user.dto.UpdateUserStatusRequest;
import com.paypilot.backend.domain.user.dto.UserResponse;
import com.paypilot.backend.security.AuthenticatedUser;
import com.paypilot.backend.security.AuthenticatedUserContext;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('OWNER')")
public class UserController {

    private final UserService userService;
    private final AuthenticatedUserContext authenticatedUserContext;

    public UserController(
            UserService userService,
            AuthenticatedUserContext authenticatedUserContext
    ) {
        this.userService = userService;
        this.authenticatedUserContext = authenticatedUserContext;
    }

    @GetMapping("/{userId}")
    public UserResponse getUser(@PathVariable Long userId) {
        AuthenticatedUser user =
                authenticatedUserContext.getCurrentUser();

        return userService.getUser(user.merchantId(), userId);
    }

    @PostMapping
    public UserResponse createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        AuthenticatedUser user =
                authenticatedUserContext.getCurrentUser();

        return userService.createUser(user.merchantId(), request);
    }

    @PutMapping("/{userId}")
    public UserResponse updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        AuthenticatedUser user =
                authenticatedUserContext.getCurrentUser();

        return userService.updateUser(
                user.merchantId(),
                userId,
                request
        );
    }

    @PatchMapping("/{userId}/role")
    public UserResponse updateRole(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        AuthenticatedUser user =
                authenticatedUserContext.getCurrentUser();

        return userService.updateRole(
                user.merchantId(),
                userId,
                request
        );
    }

    @PatchMapping("/{userId}/status")
    public UserResponse updateStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        AuthenticatedUser user =
                authenticatedUserContext.getCurrentUser();

        return userService.updateStatus(
                user.merchantId(),
                userId,
                user.userId(),
                request
        );
    }
}