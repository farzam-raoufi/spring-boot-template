package com.github.farzam_raoufi.template.exception;

public class BusinessException extends RuntimeException {

    private final ErrorMessage errorMessage;
    private final Object[] args;

    public BusinessException(ErrorMessage errorMessage, Object... args) {
        this.errorMessage = errorMessage;
        this.args = args;
    }

    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }

    public Object[] getArgs() {
        return args;
    }
}