package com.app.taskmanager.exception;

public class ResourceNotFoundException extends BaseApiException {

    public ResourceNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ResourceNotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public static ResourceNotFoundException of(String resource, Object id) {
        return new ResourceNotFoundException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "%s with id '%s' not found".formatted(resource, id));
    }
}
