package com.github.farzam_raoufi.template.validation.regex;

public enum RegexPattern {

    ALPHANUMERIC_REGEX(
            "^[a-zA-Z0-9\\dآابپتثجچحخدذرزژسشصضطظعغفقکگلمنوهیءأإؤئ۰۱۲۳۴۵۶۷۸۹]+$",
            "validation.alphanumeric"
    ),
    DIGITS_ONLY_REGEX(
            "^\\d+",
            "validation.digits_only"
    ),

    IRAN_MOBILE(
            "^09\\d{9}$",
            "validation.iran_mobile"
    ),

    NATIONAL_CODE(
            "^\\d{10}$",
            "validation.national_code"
    ),

    PASSWORD_STRONG(
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$",
            "validation.password_strong"
    ),

    EMAIL(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$",
            "validation.email"
    ),
    NOT_BLANK(
            ".*\\S.*",
            "validation.not_blank"
    );

    private final String pattern;
    private final String messageKey;

    RegexPattern(String pattern, String messageKey) {
        this.pattern = pattern;
        this.messageKey = messageKey;
    }

    public String getPattern() {
        return pattern;
    }

    public String getMessageKey() {
        return messageKey;
    }
}