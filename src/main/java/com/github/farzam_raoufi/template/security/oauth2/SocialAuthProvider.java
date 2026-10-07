package com.github.farzam_raoufi.template.security.oauth2;

import com.github.farzam_raoufi.template.dto.auth.SocialUserInfo;
import com.github.farzam_raoufi.template.model.AuthProvider;

public interface SocialAuthProvider {

    AuthProvider getProviderType();

    SocialUserInfo verify(String token);
}