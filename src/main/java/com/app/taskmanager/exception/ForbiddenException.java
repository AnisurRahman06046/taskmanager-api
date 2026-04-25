package com.app.taskmanager.exception;

public class ForbiddenException extends BaseApiException {

    public ForbiddenException() {
        super(ErrorCode.ACCESS_DENIED);
    }

    public ForbiddenException(String message) {
        super(ErrorCode.ACCESS_DENIED, message);
    }

    public ForbiddenException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
