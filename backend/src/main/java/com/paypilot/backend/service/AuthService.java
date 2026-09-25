package com.paypilot.backend.service;

import com.paypilot.backend.dto.auth.AuthResponse;
import com.paypilot.backend.dto.auth.CurrentUserResponse;
import com.paypilot.backend.dto.auth.GoogleLoginRequest;
import com.paypilot.backend.dto.auth.LoginRequest;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse loginWithGoogle(GoogleLoginRequest request);

    CurrentUserResponse getCurrentUser();
}