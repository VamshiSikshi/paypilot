package com.paypilot.backend.client;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.paypilot.backend.dto.auth.GoogleIdentity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoogleIdentityClientTest {

    @Test
    void shouldVerifyGoogleIdentity() throws Exception {
        GoogleIdTokenVerifier verifier = mock(GoogleIdTokenVerifier.class);
        GoogleIdToken token = mock(GoogleIdToken.class);
        GoogleIdToken.Payload payload = mock(GoogleIdToken.Payload.class);

        when(verifier.verify("valid-token")).thenReturn(token);
        when(token.getPayload()).thenReturn(payload);

        when(payload.getSubject()).thenReturn("google-user-123");
        when(payload.getEmail()).thenReturn("user@example.com");
        when(payload.getEmailVerified()).thenReturn(true);
        when(payload.get("name")).thenReturn("Test User");

        GoogleIdentityClient client = new GoogleIdentityClient(verifier);

        GoogleIdentity identity = client.verify("valid-token");

        assertEquals("google-user-123", identity.subject());
        assertEquals("user@example.com", identity.email());
        assertTrue(identity.emailVerified());
        assertEquals("Test User", identity.displayName());

        verify(verifier).verify("valid-token");
    }

    @Test
    void shouldRejectInvalidGoogleToken() throws Exception {
        GoogleIdTokenVerifier verifier = mock(GoogleIdTokenVerifier.class);

        when(verifier.verify("invalid-token")).thenReturn(null);

        GoogleIdentityClient client = new GoogleIdentityClient(verifier);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> client.verify("invalid-token")
        );

        assertEquals(
                "Google ID token verification failed",
                exception.getMessage()
        );

        verify(verifier).verify("invalid-token");
    }

    @Test
    void shouldRejectUnverifiedEmail() throws Exception {
        GoogleIdTokenVerifier verifier = mock(GoogleIdTokenVerifier.class);
        GoogleIdToken token = mock(GoogleIdToken.class);
        GoogleIdToken.Payload payload = mock(GoogleIdToken.Payload.class);

        when(verifier.verify("unverified-token")).thenReturn(token);
        when(token.getPayload()).thenReturn(payload);

        when(payload.getSubject()).thenReturn("google-user-123");
        when(payload.getEmail()).thenReturn("user@example.com");
        when(payload.getEmailVerified()).thenReturn(false);
        when(payload.get("name")).thenReturn("Test User");

        GoogleIdentityClient client = new GoogleIdentityClient(verifier);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> client.verify("unverified-token")
        );

        assertEquals(
                "Google ID token verification failed",
                exception.getMessage()
        );
    }
}