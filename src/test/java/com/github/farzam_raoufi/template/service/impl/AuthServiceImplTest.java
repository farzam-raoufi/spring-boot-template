package com.github.farzam_raoufi.template.service.impl;

import com.github.farzam_raoufi.template.dto.auth.AuthDTO;
import com.github.farzam_raoufi.template.dto.auth.LoginDTO;
import com.github.farzam_raoufi.template.dto.auth.RefreshTokenDTO;
import com.github.farzam_raoufi.template.dto.auth.RegisterDTO;
import com.github.farzam_raoufi.template.dto.auth.SocialUserInfo;
import com.github.farzam_raoufi.template.exception.BusinessException;
import com.github.farzam_raoufi.template.exception.ErrorMessage;
import com.github.farzam_raoufi.template.model.AuthProvider;
import com.github.farzam_raoufi.template.model.Role;
import com.github.farzam_raoufi.template.model.User;
import com.github.farzam_raoufi.template.repository.UserRepository;
import com.github.farzam_raoufi.template.security.jwt.JwtService;
import com.github.farzam_raoufi.template.security.jwt.RefreshTokenService;
import com.github.farzam_raoufi.template.security.oauth2.SocialAuthProvider;
import com.github.farzam_raoufi.template.security.oauth2.SocialAuthProviderFactory;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final String ACCESS_TOKEN = "access-token";
    private static final String REFRESH_TOKEN = "refresh-token";

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private SocialAuthProviderFactory socialAuthProviderFactory;

    @Mock
    private SocialAuthProvider googleProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private static User userWithId(Long id, String username) {
        return User.builder().id(id).username(username).role(Role.USER).build();
    }

    /** برای کاربری که از قبل داریم: jwt و refresh token مشخص برمی‌گردد. */
    private void stubTokensFor(User user) {
        when(jwtService.generateToken(user)).thenReturn(ACCESS_TOKEN);
        when(refreshTokenService.createToken(user)).thenReturn(REFRESH_TOKEN);
    }

    /** برای کاربری که داخل خود سرویس ساخته می‌شود (instance آن را از قبل نداریم). */
    private void stubTokensForAnyUser() {
        when(jwtService.generateToken(any(User.class))).thenReturn(ACCESS_TOKEN);
        when(refreshTokenService.createToken(any(User.class))).thenReturn(REFRESH_TOKEN);
    }

    private void stubSaveAssigningId(Long id) {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(id);
            return u;
        });
    }

    private void stubGoogleVerify(String idToken, SocialUserInfo info) {
        when(socialAuthProviderFactory.getProvider(AuthProvider.GOOGLE)).thenReturn(googleProvider);
        when(googleProvider.verify(idToken)).thenReturn(info);
    }

    private static void assertBusinessError(ThrowingCallable call, ErrorMessage expected) {
        assertThatThrownBy(call)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorMessage", expected);
    }

    // ---------------------------------------------------------------
    // register
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("کاربر جدید با رمز hash شده و نقش USER ذخیره می‌شود و توکن‌ها برمی‌گردند")
        void success() {
            RegisterDTO dto = RegisterDTO.builder().username("ali").password("Passw0rd1").build();
            when(userRepository.existsByUsername("ali")).thenReturn(false);
            when(passwordEncoder.encode("Passw0rd1")).thenReturn("hashed-password");
            stubSaveAssigningId(5L);
            stubTokensForAnyUser();

            AuthDTO result = authService.register(dto);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            User saved = captor.getValue();

            assertThat(saved.getUsername()).isEqualTo("ali");
            assertThat(saved.getPassword()).isEqualTo("hashed-password").isNotEqualTo("Passw0rd1");
            assertThat(saved.getRole()).isEqualTo(Role.USER);
            assertThat(saved.getProvider()).isEqualTo(AuthProvider.LOCAL);
            assertThat(saved.isEnabled()).isTrue();

            assertThat(result).usingRecursiveComparison().isEqualTo(
                    AuthDTO.builder()
                            .accessToken(ACCESS_TOKEN)
                            .refreshToken(REFRESH_TOKEN)
                            .tokenType("Bearer")
                            .userId(5L)
                            .username("ali")
                            .role(Role.USER)
                            .build());
        }

        @Test
        @DisplayName("username تکراری: USERNAME_ALREADY_EXISTS و هیچ چیزی ذخیره نمی‌شود")
        void duplicateUsername_throwsConflict() {
            RegisterDTO dto = RegisterDTO.builder().username("ali").password("Passw0rd1").build();
            when(userRepository.existsByUsername("ali")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(dto))
                    .isInstanceOfSatisfying(BusinessException.class, ex -> {
                        assertThat(ex.getErrorMessage()).isEqualTo(ErrorMessage.USERNAME_ALREADY_EXISTS);
                        assertThat(ex.getArgs()).containsExactly("ali");
                    });

            verify(userRepository, never()).save(any());
            verifyNoInteractions(passwordEncoder, jwtService, refreshTokenService);
        }
    }

    // ---------------------------------------------------------------
    // login
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("اعتبارسنجی با AuthenticationManager انجام می‌شود و توکن‌ها برمی‌گردند")
        void success() {
            LoginDTO dto = LoginDTO.builder().username("ali").password("Passw0rd1").build();
            User ali = userWithId(1L, "ali");
            when(userRepository.findByUsername("ali")).thenReturn(Optional.of(ali));
            stubTokensFor(ali);

            AuthDTO result = authService.login(dto);

            ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
            verify(authenticationManager).authenticate(captor.capture());
            assertThat(captor.getValue().getPrincipal()).isEqualTo("ali");
            assertThat(captor.getValue().getCredentials()).isEqualTo("Passw0rd1");

            assertThat(result).usingRecursiveComparison().isEqualTo(
                    AuthDTO.builder()
                            .accessToken(ACCESS_TOKEN)
                            .refreshToken(REFRESH_TOKEN)
                            .tokenType("Bearer")
                            .userId(1L)
                            .username("ali")
                            .role(Role.USER)
                            .build());
        }

        @Test
        @DisplayName("رمز اشتباه: exception عبور می‌کند و هیچ توکنی صادر نمی‌شود")
        void badCredentials_propagatesAndIssuesNothing() {
            LoginDTO dto = LoginDTO.builder().username("ali").password("wrong").build();
            when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

            assertThatThrownBy(() -> authService.login(dto)).isInstanceOf(BadCredentialsException.class);

            verifyNoInteractions(userRepository, jwtService, refreshTokenService);
        }

        @Test
        @DisplayName("اعتبارسنجی موفق ولی کاربر در دیتابیس نیست: USER_NOT_FOUND")
        void authenticatedButUserMissing_throwsNotFound() {
            LoginDTO dto = LoginDTO.builder().username("ghost").password("Passw0rd1").build();
            when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

            assertBusinessError(() -> authService.login(dto), ErrorMessage.USER_NOT_FOUND);

            verifyNoInteractions(jwtService, refreshTokenService);
        }
    }

    // ---------------------------------------------------------------
    // loginWithSocial
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("loginWithSocial")
    class LoginWithSocial {

        private static final String ID_TOKEN = "google-id-token";

        private final SocialUserInfo googleInfo = SocialUserInfo.builder()
                .provider(AuthProvider.GOOGLE)
                .providerId("g-123")
                .email("ali@example.com")
                .name("Ali")
                .build();
        @Test
        @DisplayName("کاربری که قبلاً با همین گوگل وارد شده: فقط توکن صادر می‌شود و username او حفظ است")
        void existingSocialUser_justIssuesTokens() {
            User existing = User.builder().id(7L).username("ali").email("ali@example.com")
                    .provider(AuthProvider.GOOGLE).providerId("g-123").role(Role.USER).build();
            stubGoogleVerify(ID_TOKEN, googleInfo);
            when(userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "g-123"))
                    .thenReturn(Optional.of(existing));
            stubTokensFor(existing);

            AuthDTO result = authService.loginWithSocial(AuthProvider.GOOGLE, ID_TOKEN);

            assertThat(result.getUserId()).isEqualTo(7L);
            assertThat(result.getUsername()).isEqualTo("ali");
            assertThat(result.getAccessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(result.getRefreshToken()).isEqualTo(REFRESH_TOKEN);

            verify(userRepository, never()).findByEmail(any());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("کاربر کاملاً جدید: فقط با ایمیل ساخته می‌شود؛ بدون username و بدون رمز")
        void newUser_isCreatedWithoutUsernameAndPassword() {
            stubGoogleVerify(ID_TOKEN, googleInfo);
            when(userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "g-123"))
                    .thenReturn(Optional.empty());
            when(userRepository.findByEmail("ali@example.com")).thenReturn(Optional.empty());
            stubSaveAssigningId(9L);
            stubTokensForAnyUser();

            AuthDTO result = authService.loginWithSocial(AuthProvider.GOOGLE, ID_TOKEN);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            User saved = captor.getValue();

            assertThat(saved.getEmail()).isEqualTo("ali@example.com");
            assertThat(saved.getProvider()).isEqualTo(AuthProvider.GOOGLE);
            assertThat(saved.getProviderId()).isEqualTo("g-123");
            assertThat(saved.getUsername()).isNull();
            assertThat(saved.getPassword()).isNull();
            assertThat(saved.getRole()).isEqualTo(Role.USER);
            assertThat(saved.isEnabled()).isTrue();

            assertThat(result.getUserId()).isEqualTo(9L);
            assertThat(result.getUsername()).isNull();
        }

        @Test
        @DisplayName("ایمیل متعلق به کاربر محلی است: لینک می‌شود و username و رمز او حفظ می‌شود")
        void existingLocalUserWithSameEmail_isLinked() {

            User local = User.builder().id(3L).username("ali").email("ali@example.com")
                    .password("hashed").provider(AuthProvider.LOCAL).role(Role.USER).build();
            stubGoogleVerify(ID_TOKEN, googleInfo);
            when(userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "g-123"))
                    .thenReturn(Optional.empty());
            when(userRepository.findByEmail("ali@example.com")).thenReturn(Optional.of(local));
            when(userRepository.save(local)).thenReturn(local);
            stubTokensFor(local);

            AuthDTO result = authService.loginWithSocial(AuthProvider.GOOGLE, ID_TOKEN);

            // همان کاربر قبلی تغییر کرده، نه اینکه کاربر جدیدی ساخته شود
            assertThat(local.getProvider()).isEqualTo(AuthProvider.GOOGLE);
            assertThat(local.getProviderId()).isEqualTo("g-123");
            assertThat(local.getUsername()).isEqualTo("ali");
            assertThat(local.getPassword()).isEqualTo("hashed");
            assertThat(result.getUsername()).isEqualTo("ali");
            assertThat(result.getUserId()).isEqualTo(3L);
            verify(userRepository).save(local);
        }

        @Test
        @DisplayName("توکن گوگل نامعتبر: exception عبور می‌کند و به دیتابیس دست نمی‌زند")
        void invalidSocialToken_propagatesAndTouchesNothing() {
            when(socialAuthProviderFactory.getProvider(AuthProvider.GOOGLE)).thenReturn(googleProvider);
            when(googleProvider.verify("bad-token"))
                    .thenThrow(new BusinessException(ErrorMessage.SOCIAL_TOKEN_INVALID));

            assertBusinessError(() -> authService.loginWithSocial(AuthProvider.GOOGLE, "bad-token"),
                    ErrorMessage.SOCIAL_TOKEN_INVALID);

            verifyNoInteractions(userRepository, jwtService, refreshTokenService);
        }

        @Test
        @DisplayName("provider پشتیبانی‌نشده: SOCIAL_PROVIDER_NOT_SUPPORTED")
        void unsupportedProvider_throws() {
            when(socialAuthProviderFactory.getProvider(AuthProvider.LOCAL))
                    .thenThrow(new BusinessException(ErrorMessage.SOCIAL_PROVIDER_NOT_SUPPORTED, AuthProvider.LOCAL));

            assertBusinessError(() -> authService.loginWithSocial(AuthProvider.LOCAL, "x"),
                    ErrorMessage.SOCIAL_PROVIDER_NOT_SUPPORTED);

            verifyNoInteractions(userRepository, jwtService, refreshTokenService);
        }
    }

    // ---------------------------------------------------------------
    // refreshToken / logout
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("refreshToken")
    class RefreshToken {

        @Test
        @DisplayName("access token جدید می‌سازد و refresh token را همان‌طور که rotate داده برمی‌گرداند")
        void success() {
            User ali = userWithId(1L, "ali");
            when(refreshTokenService.rotate("old-refresh"))
                    .thenReturn(new RefreshTokenDTO(ali, "new-refresh"));
            when(jwtService.generateToken(ali)).thenReturn(ACCESS_TOKEN);

            AuthDTO result = authService.refreshToken("old-refresh");

            assertThat(result).usingRecursiveComparison().isEqualTo(
                    AuthDTO.builder()
                            .accessToken(ACCESS_TOKEN)
                            .refreshToken("new-refresh")
                            .tokenType("Bearer")
                            .userId(1L)
                            .username("ali")
                            .role(Role.USER)
                            .build());
        }

        @Test
        @DisplayName("refresh token نامعتبر: exception عبور می‌کند و access token ساخته نمی‌شود")
        void invalidToken_propagates() {
            when(refreshTokenService.rotate("bad"))
                    .thenThrow(new BusinessException(ErrorMessage.REFRESH_TOKEN_INVALID));

            assertBusinessError(() -> authService.refreshToken("bad"), ErrorMessage.REFRESH_TOKEN_INVALID);

            verifyNoInteractions(jwtService);
        }
    }

    @Test
    @DisplayName("logout: refresh token را revoke می‌کند")
    void logout_revokesRefreshToken() {
        authService.logout("some-refresh-token");

        verify(refreshTokenService).revoke("some-refresh-token");
    }
}