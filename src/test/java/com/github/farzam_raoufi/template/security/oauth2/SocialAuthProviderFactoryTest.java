package com.github.farzam_raoufi.template.security.oauth2;

import com.github.farzam_raoufi.template.exception.BusinessException;
import com.github.farzam_raoufi.template.exception.ErrorMessage;
import com.github.farzam_raoufi.template.model.AuthProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SocialAuthProviderFactoryTest {

    @Mock
    private SocialAuthProvider googleProvider;

    @Mock
    private SocialAuthProvider anotherGoogleProvider;

    @Test
    @DisplayName("provider هم‌نوع با نوع درخواستی برگردانده می‌شود")
    void returnsProviderForRequestedType() {
        when(googleProvider.getProviderType()).thenReturn(AuthProvider.GOOGLE);
        SocialAuthProviderFactory factory = new SocialAuthProviderFactory(List.of(googleProvider));

        assertThat(factory.getProvider(AuthProvider.GOOGLE)).isSameAs(googleProvider);
    }

    @Test
    @DisplayName("نوعی که provider ندارد: SOCIAL_PROVIDER_NOT_SUPPORTED همراه با نام نوع")
    void unsupportedType_throwsNotSupported() {
        when(googleProvider.getProviderType()).thenReturn(AuthProvider.GOOGLE);
        SocialAuthProviderFactory factory = new SocialAuthProviderFactory(List.of(googleProvider));

        assertThatThrownBy(() -> factory.getProvider(AuthProvider.LOCAL))
                .isInstanceOfSatisfying(BusinessException.class, ex -> {
                    assertThat(ex.getErrorMessage()).isEqualTo(ErrorMessage.SOCIAL_PROVIDER_NOT_SUPPORTED);
                    assertThat(ex.getArgs()).containsExactly(AuthProvider.LOCAL);
                });
    }

    @Test
    @DisplayName("لیست provider خالی: هر نوعی پشتیبانی‌نشده است")
    void noProviders_everyTypeIsUnsupported() {
        SocialAuthProviderFactory factory = new SocialAuthProviderFactory(List.of());

        assertThatThrownBy(() -> factory.getProvider(AuthProvider.GOOGLE))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorMessage", ErrorMessage.SOCIAL_PROVIDER_NOT_SUPPORTED);
    }

    @Test
    @DisplayName("دو provider برای یک نوع: همان لحظه‌ی ساخت factory خطا می‌دهد (fail-fast هنگام استارت)")
    void duplicateProviderTypes_failAtConstruction() {
        when(googleProvider.getProviderType()).thenReturn(AuthProvider.GOOGLE);
        when(anotherGoogleProvider.getProviderType()).thenReturn(AuthProvider.GOOGLE);

        assertThatThrownBy(() -> new SocialAuthProviderFactory(List.of(googleProvider, anotherGoogleProvider)))
                .isInstanceOf(IllegalStateException.class);
    }
}