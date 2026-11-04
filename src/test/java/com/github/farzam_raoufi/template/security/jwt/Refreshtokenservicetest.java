package com.github.farzam_raoufi.template.security.jwt;

import com.github.farzam_raoufi.template.dto.auth.RefreshTokenDTO;
import com.github.farzam_raoufi.template.exception.BusinessException;
import com.github.farzam_raoufi.template.exception.ErrorMessage;
import com.github.farzam_raoufi.template.model.RefreshToken;
import com.github.farzam_raoufi.template.model.Role;
import com.github.farzam_raoufi.template.model.User;
import com.github.farzam_raoufi.template.repository.RefreshTokenRepository;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    private static final long EXPIRATION_MS = Duration.ofDays(30).toMillis();
    private static final long ROTATE_MS = Duration.ofDays(7).toMillis();

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RefreshTokenService service;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "refreshExpirationMs", EXPIRATION_MS);
        ReflectionTestUtils.setField(service, "refreshRotateMs", ROTATE_MS);

        user = User.builder().id(1L).username("ali").role(Role.USER).build();
    }


    private static String sha256(String raw) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * رکوردی که «issuedAgo» پیش صادر شده؛ یعنی expiry = زمان صدور + مدت انقضا.
     */
    private RefreshToken storedToken(String rawToken, Duration issuedAgo) {
        return RefreshToken.builder()
                .id(10L)
                .tokenHash(sha256(rawToken))
                .user(user)
                .expiryDate(Instant.now().minus(issuedAgo).plusMillis(EXPIRATION_MS))
                .revoked(false)
                .build();
    }

    private static void assertBusinessError(ThrowingCallable call, ErrorMessage expected) {
        assertThatThrownBy(call)
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorMessage", expected);
    }

    // ---------------------------------------------------------------
    // createToken
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("createToken")
    class CreateToken {

        @Test
        @DisplayName("توکن خام ۶۴ بایتی (۸۶ کاراکتر URL-safe) برمی‌گرداند")
        void returnsUrlSafeRawToken() {
            String raw = service.createToken(user);

            assertThat(raw).hasSize(86).matches("[A-Za-z0-9_-]+");
        }

        @Test
        @DisplayName("فقط hash توکن ذخیره می‌شود، نه توکن خام")
        void savesHashNotRawToken() {
            String raw = service.createToken(user);

            ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
            verify(refreshTokenRepository).save(captor.capture());
            RefreshToken saved = captor.getValue();

            assertThat(saved.getTokenHash()).isEqualTo(sha256(raw));
            assertThat(saved.getTokenHash()).isNotEqualTo(raw);
            assertThat(saved.getUser()).isSameAs(user);
            assertThat(saved.isRevoked()).isFalse();
        }

        @Test
        @DisplayName("تاریخ انقضا = الان + مدت انقضا")
        void setsExpiryDate() {
            Instant before = Instant.now();
            service.createToken(user);
            Instant after = Instant.now();

            ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
            verify(refreshTokenRepository).save(captor.capture());

            assertThat(captor.getValue().getExpiryDate())
                    .isBetween(before.plusMillis(EXPIRATION_MS), after.plusMillis(EXPIRATION_MS));
        }

        @Test
        @DisplayName("هر بار توکن متفاوتی تولید می‌شود")
        void generatesDifferentTokens() {
            assertThat(service.createToken(user)).isNotEqualTo(service.createToken(user));
        }
    }

    // ---------------------------------------------------------------
    // rotate
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("rotate")
    class Rotate {

        @Test
        @DisplayName("توکنی که در دیتابیس نیست: REFRESH_TOKEN_INVALID")
        void unknownToken_throwsInvalid() {
            when(refreshTokenRepository.findByTokenHash(sha256("unknown"))).thenReturn(Optional.empty());

            assertBusinessError(() -> service.rotate("unknown"), ErrorMessage.REFRESH_TOKEN_INVALID);

            verify(refreshTokenRepository, never()).save(any());
            verify(refreshTokenRepository, never()).delete(any());
        }

        @Test
        @DisplayName("توکن منقضی: REFRESH_TOKEN_EXPIRED و رکورد حذف می‌شود")
        void expiredToken_throwsExpiredAndDeletesRecord() {
            RefreshToken expired = storedToken("old", Duration.ofDays(31)); // ۳۱ روز پیش صادر شده
            when(refreshTokenRepository.findByTokenHash(sha256("old"))).thenReturn(Optional.of(expired));

            assertBusinessError(() -> service.rotate("old"), ErrorMessage.REFRESH_TOKEN_EXPIRED);
            verify(refreshTokenRepository).delete(expired);
            verify(refreshTokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("توکن revoke شده: REFRESH_TOKEN_EXPIRED و رکورد حذف می‌شود")
        void revokedToken_throwsExpiredAndDeletesRecord() {
            RefreshToken revoked = storedToken("old", Duration.ofDays(1));
            revoked.setRevoked(true);
            when(refreshTokenRepository.findByTokenHash(sha256("old"))).thenReturn(Optional.of(revoked));

            assertBusinessError(() -> service.rotate("old"), ErrorMessage.REFRESH_TOKEN_EXPIRED);

            verify(refreshTokenRepository).delete(revoked);
        }

        @Test
        @DisplayName("توکن تازه (کمتر از ۷ روز از صدورش): همان توکن برمی‌گردد و چیزی تغییر نمی‌کند")
        void freshToken_isReturnedAsIs() {
            RefreshToken fresh = storedToken("fresh", Duration.ofDays(1));
            when(refreshTokenRepository.findByTokenHash(sha256("fresh"))).thenReturn(Optional.of(fresh));

            RefreshTokenDTO result = service.rotate("fresh");

            assertThat(result.rawToken()).isEqualTo("fresh");
            assertThat(result.user()).isSameAs(user);

            // فقط یک جستجو انجام شده؛ نه save، نه delete
            verify(refreshTokenRepository).findByTokenHash(sha256("fresh"));
            verifyNoMoreInteractions(refreshTokenRepository);
        }

        @Test
        @DisplayName("توکن قدیمی‌تر از ۷ روز: رکورد قبلی حذف و توکن جدید صادر می‌شود")
        void agedToken_isRotated() {
            RefreshToken aged = storedToken("aged", Duration.ofDays(8)); // ۸ روز پیش صادر شده
            when(refreshTokenRepository.findByTokenHash(sha256("aged"))).thenReturn(Optional.of(aged));

            RefreshTokenDTO result = service.rotate("aged");

            verify(refreshTokenRepository).delete(aged);

            ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
            verify(refreshTokenRepository).save(captor.capture());

            assertThat(result.rawToken()).isNotEqualTo("aged");
            assertThat(result.user()).isSameAs(user);
            // رکورد جدیدی که ذخیره شده باید hash همین توکنی باشد که به کاربر داده می‌شود
            assertThat(captor.getValue().getTokenHash()).isEqualTo(sha256(result.rawToken()));
            assertThat(captor.getValue().getUser()).isSameAs(user);
        }
    }

    // ---------------------------------------------------------------
    // revoke / revokeAllForUser
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("revoke")
    class Revoke {

        @Test
        @DisplayName("توکن موجود حذف می‌شود")
        void existingToken_isDeleted() {
            RefreshToken stored = storedToken("abc", Duration.ofDays(1));
            when(refreshTokenRepository.findByTokenHash(sha256("abc"))).thenReturn(Optional.of(stored));

            service.revoke("abc");

            verify(refreshTokenRepository).delete(stored);
        }

        @Test
        @DisplayName("توکن ناموجود: بدون خطا، و چیزی حذف نمی‌شود (logout idempotent است)")
        void unknownToken_isIgnored() {
            when(refreshTokenRepository.findByTokenHash(sha256("abc"))).thenReturn(Optional.empty());

            service.revoke("abc");

            verify(refreshTokenRepository, never()).delete(any());
        }
    }

    @Test
    @DisplayName("revokeAllForUser: همه‌ی توکن‌های کاربر حذف می‌شوند")
    void revokeAllForUser_deletesAllTokensOfUser() {
        service.revokeAllForUser(user);

        verify(refreshTokenRepository).deleteAllByUser(user);
    }
}