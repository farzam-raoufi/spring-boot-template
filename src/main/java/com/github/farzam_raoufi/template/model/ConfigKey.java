package com.github.farzam_raoufi.template.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.farzam_raoufi.template.model.ConfigValueType.*;

@Getter
public enum ConfigKey {

    NOTIFICATION_SMS_PROVIDER("notification.sms.provider", STRING, "log", false,
            "Active SMS provider name (e.g. log, kavenegar)"),
    NOTIFICATION_SMS_API_KEY("notification.sms.api-key", STRING, "", true,
            "API key of the SMS provider"),
    NOTIFICATION_SMS_SENDER_NUMBER("notification.sms.sender-number", STRING, "", false,
            "Sender line number of the SMS provider"),
    NOTIFICATION_SMS_OTP_EXPIRY_SECONDS("notification.sms.otp-expiry-seconds", INTEGER, "120", false,
            "OTP validity in seconds"),

    NOTIFICATION_EMAIL_PROVIDER("notification.email.provider", STRING, "log", false,
            "Active email provider name (e.g. log, smtp)"),
    NOTIFICATION_EMAIL_FROM("notification.email.from", STRING, "no-reply@example.com", false,
            "Sender address of outgoing emails"),

    NOTIFICATION_PUSH_ENABLED("notification.push.enabled", BOOLEAN, "false", false,
            "Enable push notifications (Firebase)");

    private final String key;
    private final ConfigValueType type;
    private final String defaultValue;
    private final boolean sensitive;
    private final String description;

    ConfigKey(String key, ConfigValueType type, String defaultValue, boolean sensitive, String description) {
        this.key = key;
        this.type = type;
        this.defaultValue = defaultValue;
        this.sensitive = sensitive;
        this.description = description;
    }

    private static final Map<String, ConfigKey> BY_KEY =
            Arrays.stream(values()).collect(Collectors.toMap(ConfigKey::getKey, Function.identity()));

    public static Optional<ConfigKey> fromKey(String key) {
        return Optional.ofNullable(BY_KEY.get(key));
    }
}