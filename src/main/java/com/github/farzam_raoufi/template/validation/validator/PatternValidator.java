package com.github.farzam_raoufi.template.validation.validator;

import com.github.farzam_raoufi.template.validation.annotation.ValidPattern;
import com.github.farzam_raoufi.template.validation.regex.RegexPattern;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

public class PatternValidator implements ConstraintValidator<ValidPattern, String> {

    private final MessageSource messageSource;
    private String regex;
    private String messageKey;

    @Autowired
    public PatternValidator(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public void initialize(ValidPattern constraintAnnotation) {
        RegexPattern regexPattern = constraintAnnotation.regexPattern();
        regex = regexPattern.getPattern();
        messageKey = regexPattern.getMessageKey();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;   // Let @NotNull
        }

        boolean isValid = value.matches(regex);

        if (!isValid) {
            String localizedMessage = messageSource.getMessage(
                    messageKey,
                    null,
                    "Invalid value",
                    LocaleContextHolder.getLocale()
            );

            // Disable default message and use our custom one
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(localizedMessage)
                    .addConstraintViolation();
        }

        return isValid;
    }
}
