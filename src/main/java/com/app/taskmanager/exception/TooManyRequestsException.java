package com.app.taskmanager.exception;

public class TooManyRequestsException extends BaseApiException {

    public TooManyRequestsException() {
        super(ErrorCode.TOO_MANY_REQUESTS);
    }

    public TooManyRequestsException(String message) {
        super(ErrorCode.TOO_MANY_REQUESTS, message);
    }
}
