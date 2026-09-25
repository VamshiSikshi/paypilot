package com.paypilot.backend.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class GoogleConfig {

    @Bean
    public GoogleIdTokenVerifier googleIdTokenVerifier(
            GoogleProperties properties
    ) {
        return new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance()
        )
                .setAudience(
                        Collections.singletonList(properties.clientId())
                )
                .build();
    }
}