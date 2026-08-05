package com.github.farzam_raoufi.template.validation.annotation;

import com.github.farzam_raoufi.template.validation.regex.RegexPattern;
import com.github.farzam_raoufi.template.validation.validator.PatternValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PatternValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPattern {
    String message() default "";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    RegexPattern regexPattern();

    @Target({ElementType.FIELD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        ValidPattern[] value();
    }
}
