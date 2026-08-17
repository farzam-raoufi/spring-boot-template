package com.github.farzam_raoufi.template.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public enum ErrorMessage {
    // Template Errors
    TEMPLATE_NOT_FOUND("error.template.notfound", HttpStatus.NOT_FOUND),
    // Auth Errors
    USER_NOT_FOUND("error.user.notfound", HttpStatus.NOT_FOUND),
    USERNAME_ALREADY_EXISTS("error.username.exists", HttpStatus.CONFLICT),
    INVALID_CREDENTIALS("error.invalid.credentials", HttpStatus.UNAUTHORIZED),
    // General Errors
    INTERNAL_SERVER_ERROR("error.internal.server", HttpStatus.INTERNAL_SERVER_ERROR),
    BAD_REQUEST("error.bad.request", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("error.unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("error.forbidden", HttpStatus.FORBIDDEN),
    ;

    @Getter
    private final String key;
    @Getter
    private final HttpStatus status;

    ErrorMessage(String key,HttpStatus status) {
        this.key = key;
        this.status = status;
    }
}
