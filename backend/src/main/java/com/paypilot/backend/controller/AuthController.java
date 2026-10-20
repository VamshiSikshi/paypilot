
package com.paypilot.backend.controller;

import com.paypilot.backend.dto.auth.AuthResponse;
import com.paypilot.backend.dto.auth.CurrentUserResponse;
import com.paypilot.backend.dto.auth.GoogleLoginRequest;
import com.paypilot.backend.dto.auth.LoginRequest;
import com.paypilot.backend.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/google")
    public AuthResponse loginWithGoogle(
            @RequestBody GoogleLoginRequest request
    ) {
        return authService.loginWithGoogle(request);
    }

    @GetMapping("/me")
    public CurrentUserResponse getCurrentUser() {
        return authService.getCurrentUser();
    }
}
