package com.app.taskmanager.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * Root of the application's typed exception hierarchy.
 *
 * Every domain exception thrown from services should extend this class so that
 * {@link GlobalExceptionHandler} can translate it into a consistent error
 * response with a stable {@link ErrorCode} and HTTP status.
 */
@Getter
public abstract class BaseApiException extends RuntimeException {

    private final ErrorCode errorCode;

    protected BaseApiException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    protected BaseApiException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected BaseApiException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return errorCode.getStatus();
    }
}
