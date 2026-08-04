package com.github.farzam_raoufi.template.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public enum ErrorMessage {
    // Template Errors
    TEMPLATE_NOT_FOUND("error.template.notfound", HttpStatus.NOT_FOUND),
    // General Errors
    INTERNAL_SERVER_ERROR("error.internal.server", HttpStatus.INTERNAL_SERVER_ERROR),
    BAD_REQUEST("error.bad.request", HttpStatus.BAD_REQUEST),
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
