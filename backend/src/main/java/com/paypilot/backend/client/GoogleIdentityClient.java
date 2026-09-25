package com.paypilot.backend.client;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.paypilot.backend.dto.auth.GoogleIdentity;
import org.springframework.stereotype.Component;

@Component
public class GoogleIdentityClient {

    private final GoogleIdTokenVerifier verifier;

    public GoogleIdentityClient(GoogleIdTokenVerifier verifier) {
        this.verifier = verifier;
    }

    public GoogleIdentity verify(String idToken) {
        try {
            GoogleIdToken verifiedToken = verifier.verify(idToken);

            if (verifiedToken == null) {
                throw new IllegalArgumentException("Invalid Google ID token");
            }

            GoogleIdToken.Payload payload = verifiedToken.getPayload();

            String subject = payload.getSubject();
            String email = payload.getEmail();
            Boolean emailVerified = payload.getEmailVerified();
            String displayName = (String) payload.get("name");

            if (subject == null
                    || email == null
                    || !Boolean.TRUE.equals(emailVerified)) {
                throw new IllegalArgumentException(
                        "Google identity is incomplete or email is not verified"
                );
            }

            return new GoogleIdentity(
                    subject,
                    email,
                    true,
                    displayName
            );
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "Google ID token verification failed",
                    exception
            );
        }
    }
}