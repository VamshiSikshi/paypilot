package com.paypilot.backend.service.impl;

import com.paypilot.backend.domain.user.AuthProvider;
import com.paypilot.backend.domain.user.User;
import com.paypilot.backend.domain.user.UserStatus;
import com.paypilot.backend.domain.user.UserAuthAccount;
import com.paypilot.backend.dto.auth.AuthResponse;
import com.paypilot.backend.dto.auth.CurrentUserResponse;
import com.paypilot.backend.dto.auth.GoogleLoginRequest;
import com.paypilot.backend.dto.auth.LoginRequest;
import com.paypilot.backend.domain.user.UserAuthAccountRepository;
import com.paypilot.backend.security.AuthenticatedUser;
import com.paypilot.backend.security.JwtService;
import com.paypilot.backend.service.AuthService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.paypilot.backend.exception.InvalidCredentialsException;
import com.paypilot.backend.client.GoogleIdentityClient;
import com.paypilot.backend.domain.user.UserRepository;


@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserAuthAccountRepository userAuthAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GoogleIdentityClient googleIdentityClient;
    private final UserRepository userRepository;

    public AuthServiceImpl(
            UserAuthAccountRepository userAuthAccountRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            GoogleIdentityClient googleIdentityClient
    ) {
        this.userAuthAccountRepository = userAuthAccountRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.googleIdentityClient = googleIdentityClient;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        UserAuthAccount account =
                userAuthAccountRepository
                        .findByUserMerchantRazorpayAccountIdAndUserEmail(
                                request.merchantAccountId(),
                                request.email()
                        )
                        .orElseThrow(this::invalidCredentials);

        if (account.getProvider() != AuthProvider.LOCAL) {
            throw invalidCredentials();
        }

        User user = account.getUser();

        if (user.getStatus() != com.paypilot.backend.domain.user.UserStatus.ACTIVE) {
            throw invalidCredentials();
        }

        if (!passwordEncoder.matches(
                request.password(),
                account.getPasswordHash()
        )) {
            throw invalidCredentials();
        }

        String accessToken = jwtService.generateToken(
                user.getId(),
                user.getMerchant().getId(),
                user.getRole().name()
        );

        return new AuthResponse(
                accessToken,
                "Bearer",
                jwtService.getExpiration(),
                toCurrentUserResponse(user)
        );
    }

    @Override
    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        var identity = googleIdentityClient.verify(request.idToken());

        UserAuthAccount account =
                userAuthAccountRepository
                        .findByUserMerchantRazorpayAccountIdAndProviderAndProviderUserId(
                                request.merchantAccountId(),
                                AuthProvider.GOOGLE,
                                identity.subject()
                        )
                        .orElseThrow(this::invalidCredentials);

        User user = account.getUser();

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw invalidCredentials();
        }

        String accessToken = jwtService.generateToken(
                user.getId(),
                user.getMerchant().getId(),
                user.getRole().name()
        );

        return new AuthResponse(
                accessToken,
                "Bearer",
                jwtService.getExpiration(),
                toCurrentUserResponse(user)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser() {
        AuthenticatedUser authenticatedUser =
                (AuthenticatedUser) SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();

        User user = userRepository
                .findByMerchantIdAndId(
                        authenticatedUser.merchantId(),
                        authenticatedUser.userId()
                )
                .orElseThrow(this::invalidCredentials);

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw invalidCredentials();
        }

        return toCurrentUserResponse(user);
    }

    private CurrentUserResponse toCurrentUserResponse(User user) {
        return new CurrentUserResponse(
                user.getId(),
                user.getMerchant().getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole(),
                user.getStatus()
        );
    }

    private InvalidCredentialsException invalidCredentials() {
        return new InvalidCredentialsException();
    }
}