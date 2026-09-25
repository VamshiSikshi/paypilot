package com.paypilot.backend.service.impl;

import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.domain.user.User;
import com.paypilot.backend.domain.user.UserAuthAccount;
import com.paypilot.backend.domain.user.UserAuthAccountRepository;
import com.paypilot.backend.domain.user.UserRole;
import com.paypilot.backend.dto.auth.AuthResponse;
import com.paypilot.backend.dto.auth.LoginRequest;
import com.paypilot.backend.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.paypilot.backend.exception.InvalidCredentialsException;
import com.paypilot.backend.domain.user.AuthProvider;
import com.paypilot.backend.domain.user.UserStatus;
import com.paypilot.backend.domain.merchant.Merchant;
import com.paypilot.backend.client.GoogleIdentityClient;
import com.paypilot.backend.dto.auth.GoogleIdentity;
import com.paypilot.backend.dto.auth.GoogleLoginRequest;
import com.paypilot.backend.domain.user.UserRepository;
import com.paypilot.backend.dto.auth.CurrentUserResponse;
import com.paypilot.backend.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    @Test
    void shouldLoginWithValidLocalCredentials() {
        UserAuthAccountRepository repository =
                mock(UserAuthAccountRepository.class);
        UserRepository userRepository =
                mock(UserRepository.class);
        PasswordEncoder passwordEncoder =
                mock(PasswordEncoder.class);
        JwtService jwtService =
                mock(JwtService.class);
        GoogleIdentityClient googleIdentityClient =
                mock(GoogleIdentityClient.class);

        Merchant merchant = mock(Merchant.class);
        User user = mock(User.class);
        UserAuthAccount account = mock(UserAuthAccount.class);

        when(repository
                .findByUserMerchantRazorpayAccountIdAndUserEmail(
                        "acc_test_merchant",
                        "user@example.com"
                ))
                .thenReturn(Optional.of(account));

        when(account.getProvider())
                .thenReturn(com.paypilot.backend.domain.user.AuthProvider.LOCAL);

        when(account.getUser()).thenReturn(user);
        when(account.getPasswordHash()).thenReturn("hashed-password");

        when(user.getId()).thenReturn(1L);
        when(user.getMerchant()).thenReturn(merchant);
        when(user.getRole()).thenReturn(UserRole.OWNER);
        when(user.getStatus())
                .thenReturn(com.paypilot.backend.domain.user.UserStatus.ACTIVE);
        when(user.getEmail()).thenReturn("user@example.com");
        when(user.getDisplayName()).thenReturn("Test User");

        when(merchant.getId()).thenReturn(10L);

        when(passwordEncoder.matches(
                "correct-password",
                "hashed-password"
        )).thenReturn(true);

        when(jwtService.generateToken(
                1L,
                10L,
                "OWNER"
        )).thenReturn("test-jwt");

        when(jwtService.getExpiration()).thenReturn(86400000L);

        AuthServiceImpl service = new AuthServiceImpl(
                repository,
                userRepository,
                passwordEncoder,
                jwtService,
                googleIdentityClient
        );

        AuthResponse response = service.login(
                new LoginRequest(
                        "acc_test_merchant",
                        "user@example.com",
                        "correct-password"
                )
        );

        assertEquals("test-jwt", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(86400000L, response.expiresIn());

        assertEquals(1L, response.user().id());
        assertEquals(10L, response.user().merchantId());
        assertEquals("user@example.com", response.user().email());
        assertEquals("Test User", response.user().displayName());
        assertEquals(UserRole.OWNER, response.user().role());

        verify(passwordEncoder).matches(
                "correct-password",
                "hashed-password"
        );

        verify(jwtService).generateToken(
                1L,
                10L,
                "OWNER"
        );
    }

    @Test
    void shouldRejectWrongPassword() {
        UserAuthAccountRepository repository =
                mock(UserAuthAccountRepository.class);
        UserRepository userRepository =
                mock(UserRepository.class);
        PasswordEncoder passwordEncoder =
                mock(PasswordEncoder.class);
        JwtService jwtService =
                mock(JwtService.class);
        GoogleIdentityClient googleIdentityClient =
                mock(GoogleIdentityClient.class);

        UserAuthAccount account = mock(UserAuthAccount.class);
        User user = mock(User.class);

        when(repository
                .findByUserMerchantRazorpayAccountIdAndUserEmail(
                        "acc_test_merchant",
                        "user@example.com"
                ))
                .thenReturn(Optional.of(account));

        when(account.getProvider())
                .thenReturn(com.paypilot.backend.domain.user.AuthProvider.LOCAL);

        when(account.getUser()).thenReturn(user);
        when(account.getPasswordHash()).thenReturn("hashed-password");

        when(user.getStatus())
                .thenReturn(com.paypilot.backend.domain.user.UserStatus.ACTIVE);

        when(passwordEncoder.matches(
                "wrong-password",
                "hashed-password"
        )).thenReturn(false);

        AuthServiceImpl service = new AuthServiceImpl(
                repository,
                userRepository,
                passwordEncoder,
                jwtService,
                googleIdentityClient
        );

        assertThrows(
                InvalidCredentialsException.class,
                () -> service.login(
                        new LoginRequest(
                                "acc_test_merchant",
                                "user@example.com",
                                "wrong-password"
                        )
                )
        );

        verify(jwtService, never())
                .generateToken(anyLong(), anyLong(), anyString());
    }

    @Test
    void shouldRejectInactiveUser() {
        UserAuthAccountRepository repository =
                mock(UserAuthAccountRepository.class);
        UserRepository userRepository =
                mock(UserRepository.class);
        PasswordEncoder passwordEncoder =
                mock(PasswordEncoder.class);
        JwtService jwtService =
                mock(JwtService.class);
        GoogleIdentityClient googleIdentityClient =
                mock(GoogleIdentityClient.class);

        UserAuthAccount account = mock(UserAuthAccount.class);
        User user = mock(User.class);

        when(repository
                .findByUserMerchantRazorpayAccountIdAndUserEmail(
                        "acc_test_merchant",
                        "user@example.com"
                ))
                .thenReturn(Optional.of(account));

        when(account.getProvider())
                .thenReturn(com.paypilot.backend.domain.user.AuthProvider.LOCAL);

        when(account.getUser()).thenReturn(user);

        when(user.getStatus())
                .thenReturn(com.paypilot.backend.domain.user.UserStatus.SUSPENDED);

        AuthServiceImpl service = new AuthServiceImpl(
                repository,
                userRepository,
                passwordEncoder,
                jwtService,
                googleIdentityClient
        );

        assertThrows(
                InvalidCredentialsException.class,
                () -> service.login(
                        new LoginRequest(
                                "acc_test_merchant",
                                "user@example.com",
                                "correct-password"
                        )
                )
        );

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());

        verify(jwtService, never())
                .generateToken(anyLong(), anyLong(), anyString());
    }

    @Test
    void shouldUseMerchantAccountIdForAuthenticationLookup() {
        UserAuthAccountRepository repository =
                mock(UserAuthAccountRepository.class);
        UserRepository userRepository =
                mock(UserRepository.class);
        PasswordEncoder passwordEncoder =
                mock(PasswordEncoder.class);
        JwtService jwtService =
                mock(JwtService.class);
        GoogleIdentityClient googleIdentityClient =
                mock(GoogleIdentityClient.class);

        UserAuthAccount account = mock(UserAuthAccount.class);
        User user = mock(User.class);
        Merchant merchant = mock(Merchant.class);

        when(repository
                .findByUserMerchantRazorpayAccountIdAndUserEmail(
                        "acc_test_merchant",
                        "user@example.com"
                ))
                .thenReturn(Optional.of(account));

        when(account.getProvider())
                .thenReturn(AuthProvider.LOCAL);
        when(account.getUser()).thenReturn(user);
        when(account.getPasswordHash()).thenReturn("hashed-password");

        when(user.getStatus())
                .thenReturn(UserStatus.ACTIVE);
        when(user.getId()).thenReturn(1L);
        when(user.getMerchant()).thenReturn(merchant);
        when(user.getRole()).thenReturn(UserRole.OWNER);
        when(user.getEmail()).thenReturn("user@example.com");
        when(user.getDisplayName()).thenReturn("Test User");

        when(merchant.getId()).thenReturn(10L);

        when(passwordEncoder.matches(
                "correct-password",
                "hashed-password"
        )).thenReturn(true);

        when(jwtService.generateToken(
                1L,
                10L,
                "OWNER"
        )).thenReturn("test-jwt");

        when(jwtService.getExpiration()).thenReturn(86400000L);

        AuthServiceImpl service = new AuthServiceImpl(
                repository,
                userRepository,
                passwordEncoder,
                jwtService,
                googleIdentityClient
        );

        service.login(
                new LoginRequest(
                        "acc_test_merchant",
                        "user@example.com",
                        "correct-password"
                )
        );

        verify(repository).findByUserMerchantRazorpayAccountIdAndUserEmail(
                "acc_test_merchant",
                "user@example.com"
        );
    }

    @Test
    void shouldLoginWithExistingGoogleAccount() {
        UserAuthAccountRepository repository =
                mock(UserAuthAccountRepository.class);
        UserRepository userRepository =
                mock(UserRepository.class);
        PasswordEncoder passwordEncoder =
                mock(PasswordEncoder.class);
        JwtService jwtService =
                mock(JwtService.class);
        GoogleIdentityClient googleIdentityClient =
                mock(GoogleIdentityClient.class);

        Merchant merchant = mock(Merchant.class);
        User user = mock(User.class);
        UserAuthAccount account = mock(UserAuthAccount.class);

        GoogleIdentity identity = new GoogleIdentity(
                "google-user-123",
                "user@example.com",
                true,
                "Test User"
        );

        when(googleIdentityClient.verify("google-id-token"))
                .thenReturn(identity);

        when(repository
                .findByUserMerchantRazorpayAccountIdAndProviderAndProviderUserId(
                        "acc_test_merchant",
                        AuthProvider.GOOGLE,
                        "google-user-123"
                ))
                .thenReturn(Optional.of(account));

        when(account.getUser()).thenReturn(user);

        when(user.getStatus())
                .thenReturn(UserStatus.ACTIVE);
        when(user.getId()).thenReturn(1L);
        when(user.getMerchant()).thenReturn(merchant);
        when(user.getRole()).thenReturn(UserRole.OWNER);
        when(user.getEmail()).thenReturn("user@example.com");
        when(user.getDisplayName()).thenReturn("Test User");

        when(merchant.getId()).thenReturn(10L);

        when(jwtService.generateToken(
                1L,
                10L,
                "OWNER"
        )).thenReturn("google-test-jwt");

        when(jwtService.getExpiration())
                .thenReturn(86400000L);

        AuthServiceImpl service = new AuthServiceImpl(
                repository,
                userRepository,
                passwordEncoder,
                jwtService,
                googleIdentityClient
        );

        AuthResponse response = service.loginWithGoogle(
                new GoogleLoginRequest(
                        "acc_test_merchant",
                        "google-id-token"
                )
        );

        assertEquals(
                "google-test-jwt",
                response.accessToken()
        );

        assertEquals(
                "Bearer",
                response.tokenType()
        );

        assertEquals(
                86400000L,
                response.expiresIn()
        );

        assertEquals(
                1L,
                response.user().id()
        );

        assertEquals(
                10L,
                response.user().merchantId()
        );

        assertEquals(
                "user@example.com",
                response.user().email()
        );

        assertEquals(
                UserRole.OWNER,
                response.user().role()
        );

        verify(googleIdentityClient)
                .verify("google-id-token");

        verify(repository)
                .findByUserMerchantRazorpayAccountIdAndProviderAndProviderUserId(
                        "acc_test_merchant",
                        AuthProvider.GOOGLE,
                        "google-user-123"
                );

        verify(jwtService)
                .generateToken(
                        1L,
                        10L,
                        "OWNER"
                );
    }

    @Test
    void shouldRejectUnknownGoogleAccount() {
        UserAuthAccountRepository repository =
                mock(UserAuthAccountRepository.class);
        UserRepository userRepository =
                mock(UserRepository.class);
        PasswordEncoder passwordEncoder =
                mock(PasswordEncoder.class);
        JwtService jwtService =
                mock(JwtService.class);
        GoogleIdentityClient googleIdentityClient =
                mock(GoogleIdentityClient.class);

        GoogleIdentity identity = new GoogleIdentity(
                "google-user-999",
                "unknown@example.com",
                true,
                "Unknown User"
        );

        when(googleIdentityClient.verify("google-id-token"))
                .thenReturn(identity);

        when(repository
                .findByUserMerchantRazorpayAccountIdAndProviderAndProviderUserId(
                        "acc_test_merchant",
                        AuthProvider.GOOGLE,
                        "google-user-999"
                ))
                .thenReturn(Optional.empty());

        AuthServiceImpl service = new AuthServiceImpl(
                repository,
                userRepository,
                passwordEncoder,
                jwtService,
                googleIdentityClient
        );

        assertThrows(
                InvalidCredentialsException.class,
                () -> service.loginWithGoogle(
                        new GoogleLoginRequest(
                                "acc_test_merchant",
                                "google-id-token"
                        )
                )
        );

        verify(jwtService, never())
                .generateToken(anyLong(), anyLong(), anyString());
    }

    @Test
    void shouldRejectSuspendedGoogleUser() {
        UserAuthAccountRepository repository =
                mock(UserAuthAccountRepository.class);
        UserRepository userRepository =
                mock(UserRepository.class);
        PasswordEncoder passwordEncoder =
                mock(PasswordEncoder.class);
        JwtService jwtService =
                mock(JwtService.class);
        GoogleIdentityClient googleIdentityClient =
                mock(GoogleIdentityClient.class);

        Merchant merchant = mock(Merchant.class);
        User user = mock(User.class);
        UserAuthAccount account = mock(UserAuthAccount.class);

        GoogleIdentity identity = new GoogleIdentity(
                "google-user-123",
                "user@example.com",
                true,
                "Test User"
        );

        when(googleIdentityClient.verify("google-id-token"))
                .thenReturn(identity);

        when(repository
                .findByUserMerchantRazorpayAccountIdAndProviderAndProviderUserId(
                        "acc_test_merchant",
                        AuthProvider.GOOGLE,
                        "google-user-123"
                ))
                .thenReturn(Optional.of(account));

        when(account.getUser()).thenReturn(user);

        when(user.getStatus())
                .thenReturn(UserStatus.SUSPENDED);

        AuthServiceImpl service = new AuthServiceImpl(
                repository,
                userRepository,
                passwordEncoder,
                jwtService,
                googleIdentityClient
        );

        assertThrows(
                InvalidCredentialsException.class,
                () -> service.loginWithGoogle(
                        new GoogleLoginRequest(
                                "acc_test_merchant",
                                "google-id-token"
                        )
                )
        );

        verify(jwtService, never())
                .generateToken(anyLong(), anyLong(), anyString());
    }

    @Test
    void shouldGetCurrentUser() {
        UserAuthAccountRepository repository =
                mock(UserAuthAccountRepository.class);
        UserRepository userRepository =
                mock(UserRepository.class);
        PasswordEncoder passwordEncoder =
                mock(PasswordEncoder.class);
        JwtService jwtService =
                mock(JwtService.class);
        GoogleIdentityClient googleIdentityClient =
                mock(GoogleIdentityClient.class);

        Merchant merchant = mock(Merchant.class);
        User user = mock(User.class);

        when(userRepository.findByMerchantIdAndId(10L, 1L))
                .thenReturn(Optional.of(user));

        when(user.getId()).thenReturn(1L);
        when(user.getMerchant()).thenReturn(merchant);
        when(user.getEmail()).thenReturn("user@example.com");
        when(user.getDisplayName()).thenReturn("Test User");
        when(user.getRole()).thenReturn(UserRole.OWNER);
        when(user.getStatus()).thenReturn(UserStatus.ACTIVE);

        when(merchant.getId()).thenReturn(10L);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(authentication.getPrincipal())
                .thenReturn(new AuthenticatedUser(1L, 10L, UserRole.OWNER));

        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        AuthServiceImpl service = new AuthServiceImpl(
                repository,
                userRepository,
                passwordEncoder,
                jwtService,
                googleIdentityClient
        );

        CurrentUserResponse response = service.getCurrentUser();

        assertEquals(1L, response.id());
        assertEquals(10L, response.merchantId());
        assertEquals("user@example.com", response.email());
        assertEquals("Test User", response.displayName());
        assertEquals(UserRole.OWNER, response.role());
        assertEquals(UserStatus.ACTIVE, response.status());

        verify(userRepository)
                .findByMerchantIdAndId(10L, 1L);

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRejectInactiveCurrentUser() {
        UserAuthAccountRepository repository =
                mock(UserAuthAccountRepository.class);
        UserRepository userRepository =
                mock(UserRepository.class);
        PasswordEncoder passwordEncoder =
                mock(PasswordEncoder.class);
        JwtService jwtService =
                mock(JwtService.class);
        GoogleIdentityClient googleIdentityClient =
                mock(GoogleIdentityClient.class);

        User user = mock(User.class);

        when(userRepository.findByMerchantIdAndId(10L, 1L))
                .thenReturn(Optional.of(user));

        when(user.getStatus())
                .thenReturn(UserStatus.SUSPENDED);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(authentication.getPrincipal())
                .thenReturn(new AuthenticatedUser(1L, 10L, UserRole.OWNER));

        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        AuthServiceImpl service = new AuthServiceImpl(
                repository,
                userRepository,
                passwordEncoder,
                jwtService,
                googleIdentityClient
        );

        assertThrows(
                InvalidCredentialsException.class,
                service::getCurrentUser
        );

        verify(userRepository)
                .findByMerchantIdAndId(10L, 1L);

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRejectUnknownCurrentUser() {
        UserAuthAccountRepository repository =
                mock(UserAuthAccountRepository.class);
        UserRepository userRepository =
                mock(UserRepository.class);
        PasswordEncoder passwordEncoder =
                mock(PasswordEncoder.class);
        JwtService jwtService =
                mock(JwtService.class);
        GoogleIdentityClient googleIdentityClient =
                mock(GoogleIdentityClient.class);

        when(userRepository.findByMerchantIdAndId(10L, 1L))
                .thenReturn(Optional.empty());

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(authentication.getPrincipal())
                .thenReturn(new AuthenticatedUser(1L, 10L, UserRole.OWNER));

        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        AuthServiceImpl service = new AuthServiceImpl(
                repository,
                userRepository,
                passwordEncoder,
                jwtService,
                googleIdentityClient
        );

        assertThrows(
                InvalidCredentialsException.class,
                service::getCurrentUser
        );

        verify(userRepository)
                .findByMerchantIdAndId(10L, 1L);

        SecurityContextHolder.clearContext();
    }
}