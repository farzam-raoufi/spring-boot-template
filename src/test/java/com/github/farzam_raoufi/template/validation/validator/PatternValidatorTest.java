package com.github.farzam_raoufi.template.validation.validator;

import com.github.farzam_raoufi.template.validation.annotation.ValidPattern;
import com.github.farzam_raoufi.template.validation.regex.RegexPattern;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class PatternValidatorTest {

    private static final String DEFAULT_MESSAGE = "Invalid value";

    @Mock
    private MessageSource messageSource;
    @Mock
    private ValidPattern annotation;
    @Mock
    private ConstraintValidatorContext context;
    @Mock
    private ConstraintViolationBuilder violationBuilder;

    private PatternValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PatternValidator(messageSource);
    }


    @AfterEach
    void resetLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    private void initializeWith(RegexPattern pattern) {
        when(annotation.regexPattern()).thenReturn(pattern);
        validator.initialize(annotation);
    }

    private void stubLocalizedMessage(RegexPattern pattern, Locale locale, String message) {
        when(messageSource.getMessage(eq(pattern.getMessageKey()), isNull(), eq(DEFAULT_MESSAGE), eq(locale)))
                .thenReturn(message);
        when(context.buildConstraintViolationWithTemplate(message)).thenReturn(violationBuilder);
    }

    // ---------------------------------------------------------------
    // حالت‌های معتبر
    // ---------------------------------------------------------------

    @Test
    @DisplayName("مقدار null معتبر است (بررسی null بودن با @NotNull/@NotBlank است) و پیامی ساخته نمی‌شود")
    void nullValue_isValid() {
        initializeWith(RegexPattern.EN_ALPHANUMERIC_REGEX);

        assertThat(validator.isValid(null, context)).isTrue();

        verifyNoInteractions(messageSource, context);
    }

    @ParameterizedTest(name = "\"{0}\" معتبر است")
    @ValueSource(strings = {"ali", "Ali123"})
    void matchingValue_isValid_andNoMessageIsBuilt(String value) {
        initializeWith(RegexPattern.EN_ALPHANUMERIC_REGEX);

        assertThat(validator.isValid(value, context)).isTrue();

        verifyNoInteractions(messageSource, context);
    }

    // ---------------------------------------------------------------
    // حالت‌های نامعتبر
    // ---------------------------------------------------------------

    @Test
    @DisplayName("مقدار نامعتبر: false برمی‌گرداند و پیام پیش‌فرض را با پیام ترجمه‌شده جایگزین می‌کند")
    void nonMatchingValue_replacesDefaultMessage() {
        initializeWith(RegexPattern.EN_ALPHANUMERIC_REGEX);
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        stubLocalizedMessage(RegexPattern.EN_ALPHANUMERIC_REGEX, Locale.ENGLISH, "English only please");

        boolean result = validator.isValid("ali_1", context);

        assertThat(result).isFalse();
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("English only please");
        verify(violationBuilder).addConstraintViolation();
    }

    @Test
    @DisplayName("پیام با locale فعلی درخواست (مثلاً فارسی) از MessageSource گرفته می‌شود")
    void messageIsResolvedWithCurrentLocale() {
        Locale persian = Locale.forLanguageTag("fa");
        initializeWith(RegexPattern.EN_ALPHANUMERIC_REGEX);
        LocaleContextHolder.setLocale(persian);

        stubLocalizedMessage(RegexPattern.EN_ALPHANUMERIC_REGEX, persian, "پیام فارسی");

        assertThat(validator.isValid("علی", context)).isFalse();

        verify(context).buildConstraintViolationWithTemplate("پیام فارسی");
    }

    @ParameterizedTest(name = "{0}: برای مقدار خالی نامعتبر است و messageKey خودش را استفاده می‌کند")
    @EnumSource(RegexPattern.class)
    void everyPattern_rejectsEmptyString_usingItsOwnMessageKey(RegexPattern pattern) {
        initializeWith(pattern);
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        stubLocalizedMessage(pattern, Locale.ENGLISH, "msg for " + pattern.name());

        assertThat(validator.isValid("", context)).isFalse();

        verify(context).buildConstraintViolationWithTemplate("msg for " + pattern.name());
    }
}