package com.github.farzam_raoufi.template.security.oauth2;

import com.github.farzam_raoufi.template.dto.auth.SocialUserInfo;
import com.github.farzam_raoufi.template.exception.BusinessException;
import com.github.farzam_raoufi.template.exception.ErrorMessage;
import com.github.farzam_raoufi.template.model.AuthProvider;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Component
public class GoogleAuthProvider implements SocialAuthProvider {

    private final GoogleIdTokenVerifier verifier;

    public GoogleAuthProvider(@Value("${app.oauth2.google.client-id}") String clientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    @Override
    public AuthProvider getProviderType() {
        return AuthProvider.GOOGLE;
    }

    @Override
    public SocialUserInfo verify(String token) {
        try {
            GoogleIdToken idToken = verifier.verify(token);
            if (idToken == null) {
                throw new BusinessException(ErrorMessage.SOCIAL_TOKEN_INVALID);
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            return SocialUserInfo.builder()
                    .provider(AuthProvider.GOOGLE)
                    .providerId(payload.getSubject())
                    .email(payload.getEmail())
                    .name((String) payload.get("name"))
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            throw new BusinessException(ErrorMessage.SOCIAL_TOKEN_INVALID);
        }
    }
}