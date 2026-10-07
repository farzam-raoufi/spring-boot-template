package com.github.farzam_raoufi.template.security.oauth2;

import com.github.farzam_raoufi.template.exception.BusinessException;
import com.github.farzam_raoufi.template.exception.ErrorMessage;
import com.github.farzam_raoufi.template.model.AuthProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SocialAuthProviderFactory {

    private final Map<AuthProvider, SocialAuthProvider> providers;

    public SocialAuthProviderFactory(List<SocialAuthProvider> providers) {
        this.providers = providers.stream()
                .collect(Collectors.toMap(SocialAuthProvider::getProviderType, Function.identity()));
    }

    public SocialAuthProvider getProvider(AuthProvider type) {
        SocialAuthProvider provider = providers.get(type);
        if (provider == null) {
            throw new BusinessException(ErrorMessage.SOCIAL_PROVIDER_NOT_SUPPORTED, type);
        }
        return provider;
    }
}