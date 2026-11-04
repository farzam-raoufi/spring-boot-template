package com.github.farzam_raoufi.template.validation.regex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;


class RegexPatternTest {

    private static boolean matches(RegexPattern pattern, String value) {
        return value.matches(pattern.getPattern());
    }

    // ---------------------------------------------------------------
    // تست‌های ساختاری روی کل enum
    // ---------------------------------------------------------------

    @ParameterizedTest(name = "{0}: regex قابل compile است")
    @EnumSource(RegexPattern.class)
    void everyPattern_isValidRegex(RegexPattern pattern) {
        assertThatCode(() -> Pattern.compile(pattern.getPattern())).doesNotThrowAnyException();
    }

    @ParameterizedTest(name = "{0}: messageKey در هر دو فایل messages تعریف شده")
    @EnumSource(RegexPattern.class)
    void everyPattern_hasMessageInBothLanguages(RegexPattern pattern) throws IOException {
        Properties en = load("/messages/messages.properties");
        Properties fa = load("/messages/messages_fa.properties");

        assertThat(en.getProperty(pattern.getMessageKey()))
                .as("English message for %s", pattern.getMessageKey()).isNotBlank();
        assertThat(fa.getProperty(pattern.getMessageKey()))
                .as("Persian message for %s", pattern.getMessageKey()).isNotBlank();
    }

    private static Properties load(String path) throws IOException {
        try (InputStream in = RegexPatternTest.class.getResourceAsStream(path)) {
            assertThat(in).as("resource %s", path).isNotNull();
            Properties props = new Properties();
            props.load(in);
            return props;
        }
    }


    @Nested
    @DisplayName("EN_ALPHANUMERIC_REGEX")
    class EnAlphanumeric {

        @ParameterizedTest(name = "\"{0}\" معتبر است")
        @ValueSource(strings = {"ali", "Ali123", "A1", "123"})
        void accepts(String value) {
            assertThat(matches(RegexPattern.EN_ALPHANUMERIC_REGEX, value)).isTrue();
        }

        @ParameterizedTest(name = "\"{0}\" نامعتبر است")
        @ValueSource(strings = {"ali_1", "ali-1", "ali 1", "علی"})
        @EmptySource
        void rejects(String value) {
            assertThat(matches(RegexPattern.EN_ALPHANUMERIC_REGEX, value)).isFalse();
        }
    }

    @Nested
    @DisplayName("ALPHANUMERIC_REGEX (فارسی + انگلیسی)")
    class Alphanumeric {

        @ParameterizedTest(name = "\"{0}\" معتبر است")
        @ValueSource(strings = {"علی", "محمد", "Ali۱۲۳", "سلام۱", "ali"})
        void accepts(String value) {
            assertThat(matches(RegexPattern.ALPHANUMERIC_REGEX, value)).isTrue();
        }

        @ParameterizedTest(name = "\"{0}\" نامعتبر است")
        @ValueSource(strings = {"علی!", "ali ali", "علی رضا"})
        @EmptySource
        void rejects(String value) {
            assertThat(matches(RegexPattern.ALPHANUMERIC_REGEX, value)).isFalse();
        }
    }

    @Nested
    @DisplayName("DIGITS_ONLY_REGEX")
    class DigitsOnly {

        @ParameterizedTest(name = "\"{0}\" معتبر است")
        @ValueSource(strings = {"0", "123", "0012"})
        void accepts(String value) {
            assertThat(matches(RegexPattern.DIGITS_ONLY_REGEX, value)).isTrue();
        }

        @ParameterizedTest(name = "\"{0}\" نامعتبر است")
        @ValueSource(strings = {"12a", "-1", "1.5", "1 2"})
        @EmptySource
        void rejects(String value) {
            assertThat(matches(RegexPattern.DIGITS_ONLY_REGEX, value)).isFalse();
        }

        @Test
        @DisplayName("ارقام فارسی را قبول نمی‌کند (\\d در جاوا فقط ASCII است)")
        void persianDigits_areRejected() {
            assertThat(matches(RegexPattern.DIGITS_ONLY_REGEX, "۱۲۳")).isFalse();
        }
    }

    @Nested
    @DisplayName("IRAN_MOBILE")
    class IranMobile {

        @ParameterizedTest(name = "\"{0}\" معتبر است")
        @ValueSource(strings = {"09123456789", "09000000000"})
        void accepts(String value) {
            assertThat(matches(RegexPattern.IRAN_MOBILE, value)).isTrue();
        }

        @ParameterizedTest(name = "\"{0}\" نامعتبر است")
        @ValueSource(strings = {
                "9123456789",     // بدون صفر اول
                "0912345678",     // ۱۰ رقم (کم)
                "091234567890",   // ۱۲ رقم (زیاد)
                "08123456789",    // شروع با 08
                "09a23456789"     // حرف وسط عدد
        })
        @EmptySource
        void rejects(String value) {
            assertThat(matches(RegexPattern.IRAN_MOBILE, value)).isFalse();
        }
    }

    @Nested
    @DisplayName("NATIONAL_CODE")
    class NationalCode {

        @ParameterizedTest(name = "\"{0}\" معتبر است")
        @ValueSource(strings = {"0012345678", "1234567890"})
        void accepts(String value) {
            assertThat(matches(RegexPattern.NATIONAL_CODE, value)).isTrue();
        }

        @ParameterizedTest(name = "\"{0}\" نامعتبر است")
        @ValueSource(strings = {"123456789", "12345678901", "12345abcde"})
        @EmptySource
        void rejects(String value) {
            assertThat(matches(RegexPattern.NATIONAL_CODE, value)).isFalse();
        }
    }

    @Nested
    @DisplayName("PASSWORD_STRONG")
    class PasswordStrong {

        @ParameterizedTest(name = "\"{0}\" معتبر است")
        @ValueSource(strings = {"Abcdef12", "Passw0rdLong", "Abc12345!@#"})
        void accepts(String value) {
            assertThat(matches(RegexPattern.PASSWORD_STRONG, value)).isTrue();
        }

        @ParameterizedTest(name = "\"{0}\" نامعتبر است")
        @ValueSource(strings = {
                "abcdefg1",   // حرف بزرگ ندارد
                "ABCDEFG1",   // حرف کوچک ندارد
                "Abcdefgh",   // عدد ندارد
                "Ab1"         // کوتاه است
        })
        @EmptySource
        void rejects(String value) {
            assertThat(matches(RegexPattern.PASSWORD_STRONG, value)).isFalse();
        }

        @Test
        @DisplayName("دقیقاً ۸ کاراکتر مرز معتبر است و ۷ کاراکتر نامعتبر")
        void lengthBoundary() {
            assertThat(matches(RegexPattern.PASSWORD_STRONG, "Abcdef1")).isFalse(); // 7
            assertThat(matches(RegexPattern.PASSWORD_STRONG, "Abcdef12")).isTrue();  // 8
        }
    }

    @Nested
    @DisplayName("EMAIL")
    class Email {

        @ParameterizedTest(name = "\"{0}\" معتبر است")
        @ValueSource(strings = {"a@b.com", "first.last+tag@example.co", "user_1@mail-server.org"})
        void accepts(String value) {
            assertThat(matches(RegexPattern.EMAIL, value)).isTrue();
        }

        @ParameterizedTest(name = "\"{0}\" نامعتبر است")
        @ValueSource(strings = {"abc", "@b.com", "a@", "a b@c.com", "a@b@c.com"})
        @EmptySource
        void rejects(String value) {
            assertThat(matches(RegexPattern.EMAIL, value)).isFalse();
        }

        @Test
        @DisplayName("regex ایمیل سخت‌گیر نیست: دامنه بدون TLD و حتی \"a@..\" را قبول می‌کند")
        void isLenient_documentsCurrentBehavior() {
            assertThat(matches(RegexPattern.EMAIL, "user@localhost")).isTrue();
            assertThat(matches(RegexPattern.EMAIL, "a@..")).isTrue();
        }
    }
}