package com.github.farzam_raoufi.template.security.jwt;

import com.github.farzam_raoufi.template.model.Role;
import com.github.farzam_raoufi.template.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class JwtServiceTest {

    private static final String SECRET = "GTuR5e/Sy2/3zHrxmJ/c8LPz1hhLLtIq0puJi6lt8eM=";
    private static final String OTHER_SECRET = "CuyIzJE6vxSv+a4VPqYgeEyHSS9hKD/91brNydJOpIU=";
    private static final long ONE_HOUR_MS = 3_600_000L;

    private JwtService jwtService;
    private User user1;

    @BeforeEach
    void setUp() {
        jwtService = newJwtService(SECRET, ONE_HOUR_MS);
        user1 = User.builder().id(1L).username("user1").role(Role.USER).build();
    }

    private static JwtService newJwtService(String secret, long expirationMs) {
        JwtService service = new JwtService();
        ReflectionTestUtils.setField(service, "secretKey", secret);
        ReflectionTestUtils.setField(service, "jwtExpirationMs", expirationMs);
        return service;
    }


    private static SecretKey testKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
    }

    // ---------------------------------------------------------------
    // generateToken
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("generateToken")
    class GenerateToken {

        @Test
        @DisplayName("توکن JWT با سه بخش (header.payload.signature) می‌سازد")
        void returnsTokenWithThreeParts() {
            String token = jwtService.generateToken(user1);

            assertThat(token).isNotBlank();
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("subject توکن برابر id کاربر است")
        void subjectIsUserId() {
            String token = jwtService.generateToken(user1);

            String subject = jwtService.extractClaim(token, Claims::getSubject);

            assertThat(subject).isEqualTo("1");
        }

        @Test
        @DisplayName("extraClaims داخل توکن قرار می‌گیرند")
        void includesExtraClaims() {
            String token = jwtService.generateToken(Map.of("department", "engineering"), user1);

            String department = jwtService.extractClaim(token, claims -> claims.get("department", String.class));

            assertThat(department).isEqualTo("engineering");
        }
    }

    // ---------------------------------------------------------------
    // extractUserId
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("extractUserId")
    class ExtractUserId {

        @Test
        @DisplayName("همان id کاربر داخل توکن را برمی‌گرداند")
        void returnsUserId() {
            String token = jwtService.generateToken(user1);

            assertThat(jwtService.extractUserId(token)).isEqualTo(1L);
        }

        @Test
        @DisplayName("برای کاربری که username ندارد (فقط گوگل) هم کار می‌کند")
        void worksForUserWithoutUsername() {
            User googleOnly = User.builder().id(9L).email("a@example.com").role(Role.USER).build();

            String token = jwtService.generateToken(googleOnly);

            assertThat(jwtService.extractUserId(token)).isEqualTo(9L);
        }

        @Test
        @DisplayName("توکن منقضی: ExpiredJwtException")
        void expiredToken_throwsExpiredJwtException() {
            // مدت اعتبار منفی = توکنی که همان لحظه‌ی ساخته شدن منقضی است
            String token = newJwtService(SECRET, -1000L).generateToken(user1);

            assertThatThrownBy(() -> jwtService.extractUserId(token))
                    .isInstanceOf(ExpiredJwtException.class);
        }

        @Test
        @DisplayName("توکن امضا شده با کلید دیگر: SignatureException")
        void tokenSignedWithOtherKey_throwsSignatureException() {
            String forged = newJwtService(OTHER_SECRET, ONE_HOUR_MS).generateToken(user1);

            assertThatThrownBy(() -> jwtService.extractUserId(forged))
                    .isInstanceOf(SignatureException.class);
        }

        @Test
        @DisplayName("payload دست‌کاری شده (id عوض شده): SignatureException")
        void tamperedPayload_throwsSignatureException() {
            String token = jwtService.generateToken(user1);
            String[] parts = token.split("\\.");

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            String tamperedJson = payloadJson.replace("\"sub\":\"1\"", "\"sub\":\"2\"");
            String tamperedPayload = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(tamperedJson.getBytes(StandardCharsets.UTF_8));
            String tamperedToken = parts[0] + "." + tamperedPayload + "." + parts[2];

            assertThat(tamperedJson).contains("\"sub\":\"2\"");

            assertThatThrownBy(() -> jwtService.extractUserId(tamperedToken))
                    .isInstanceOf(SignatureException.class);
        }

        @Test
        @DisplayName("رشته‌ی بی‌معنی به‌جای توکن: MalformedJwtException")
        void garbageToken_throwsMalformedJwtException() {
            assertThatThrownBy(() -> jwtService.extractUserId("this.is.not-a-jwt"))
                    .isInstanceOf(MalformedJwtException.class);
        }

        @Test
        @DisplayName("توکن معتبر ولی با subject غیرعددی: MalformedJwtException")
        void nonNumericSubject_throwsMalformedJwtException() {
            String token = Jwts.builder().subject("user1").signWith(testKey()).compact();

            assertThatThrownBy(() -> jwtService.extractUserId(token))
                    .isInstanceOf(MalformedJwtException.class);
        }

        @Test
        @DisplayName("توکن معتبر ولی بدون subject: MalformedJwtException")
        void missingSubject_throwsMalformedJwtException() {
            String token = Jwts.builder().claim("foo", "bar").signWith(testKey()).compact();

            assertThatThrownBy(() -> jwtService.extractUserId(token))
                    .isInstanceOf(MalformedJwtException.class);
        }
    }

    @Nested
    @DisplayName("isTokenValid")
    class IsTokenValid {

        @Test
        @DisplayName("توکن سالم و کاربر درست: true")
        void matchingUser_returnsTrue() {
            String token = jwtService.generateToken(user1);

            assertThat(jwtService.isTokenValid(token, user1)).isTrue();
        }

        @Test
        @DisplayName("توکن مال کاربر دیگری است: false")
        void differentUser_returnsFalse() {
            String token = jwtService.generateToken(user1);
            User reza = User.builder().id(2L).username("reza").role(Role.USER).build();

            assertThat(jwtService.isTokenValid(token, reza)).isFalse();
        }

        @Test
        @DisplayName("کاربر بدون username: با id او معتبر است")
        void userWithoutUsername_returnsTrue() {
            User googleOnly = User.builder().id(9L).email("a@example.com").role(Role.USER).build();
            String token = jwtService.generateToken(googleOnly);

            assertThat(jwtService.isTokenValid(token, googleOnly)).isTrue();
        }

        @Test
        @DisplayName("توکن منقضی: false (exception پرتاب نمی‌شود)")
        void expiredToken_returnsFalse() {
            String token = newJwtService(SECRET, -1000L).generateToken(user1);

            assertThat(jwtService.isTokenValid(token, user1)).isFalse();
        }

        @Test
        @DisplayName("توکن امضا شده با کلید دیگر: false")
        void tokenSignedWithOtherKey_returnsFalse() {
            String forged = newJwtService(OTHER_SECRET, ONE_HOUR_MS).generateToken(user1);

            assertThat(jwtService.isTokenValid(forged, user1)).isFalse();
        }

        @ParameterizedTest(name = "ورودی \"{0}\": false")
        @ValueSource(strings = {"garbage", "this.is.not-a-jwt"})
        @NullAndEmptySource
        void invalidInput_returnsFalse(String token) {
            assertThat(jwtService.isTokenValid(token, user1)).isFalse();
        }

        @Test
        @DisplayName("توکن با subject غیرعددی: false")
        void nonNumericSubject_returnsFalse() {
            String token = Jwts.builder().subject("user1").signWith(testKey()).compact();

            assertThat(jwtService.isTokenValid(token, user1)).isFalse();
        }
    }
}