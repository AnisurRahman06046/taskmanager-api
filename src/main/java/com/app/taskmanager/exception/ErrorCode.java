package com.app.taskmanager.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * Central registry of all application error codes.
 *
 * Each code maps to a stable string identifier (used by API clients), an HTTP
 * status (default for that error), and a default human-readable message that
 * can be overridden when the exception is thrown.
 */
@Getter
public enum ErrorCode {

    // 400 - Bad Request
    VALIDATION_FAILED("VALIDATION_FAILED", HttpStatus.BAD_REQUEST, "Request validation failed"),
    BAD_REQUEST("BAD_REQUEST", HttpStatus.BAD_REQUEST, "Bad request"),
    INVALID_INPUT("INVALID_INPUT", HttpStatus.BAD_REQUEST, "Invalid input"),
    MALFORMED_JSON("MALFORMED_JSON", HttpStatus.BAD_REQUEST, "Malformed JSON request body"),
    MISSING_PARAMETER("MISSING_PARAMETER", HttpStatus.BAD_REQUEST, "Required parameter is missing"),
    TYPE_MISMATCH("TYPE_MISMATCH", HttpStatus.BAD_REQUEST, "Parameter type mismatch"),
    INVALID_SORT_FIELD("INVALID_SORT_FIELD", HttpStatus.BAD_REQUEST, "Invalid sort field"),
    INVALID_STATUS("INVALID_STATUS", HttpStatus.BAD_REQUEST, "Invalid status value"),

    // 401 - Unauthorized
    UNAUTHORIZED("UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "Authentication required"),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED, "Invalid email or password"),
    INVALID_TOKEN("INVALID_TOKEN", HttpStatus.UNAUTHORIZED, "Invalid authentication token"),
    EXPIRED_TOKEN("EXPIRED_TOKEN", HttpStatus.UNAUTHORIZED, "Authentication token has expired"),
    MISSING_TOKEN("MISSING_TOKEN", HttpStatus.UNAUTHORIZED, "Authentication token is missing"),

    // 403 - Forbidden
    ACCESS_DENIED("ACCESS_DENIED", HttpStatus.FORBIDDEN, "Access denied"),

    // 404 - Not Found
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND, "Resource not found"),
    USER_NOT_FOUND("USER_NOT_FOUND", HttpStatus.NOT_FOUND, "User not found"),
    TASK_NOT_FOUND("TASK_NOT_FOUND", HttpStatus.NOT_FOUND, "Task not found"),
    ENDPOINT_NOT_FOUND("ENDPOINT_NOT_FOUND", HttpStatus.NOT_FOUND, "Endpoint not found"),

    // 405 / 415
    METHOD_NOT_ALLOWED("METHOD_NOT_ALLOWED", HttpStatus.METHOD_NOT_ALLOWED, "HTTP method not allowed"),
    UNSUPPORTED_MEDIA_TYPE("UNSUPPORTED_MEDIA_TYPE", HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported media type"),

    // 409 - Conflict
    CONFLICT("CONFLICT", HttpStatus.CONFLICT, "Resource conflict"),
    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", HttpStatus.CONFLICT, "Email already exists"),
    DATA_INTEGRITY_VIOLATION("DATA_INTEGRITY_VIOLATION", HttpStatus.CONFLICT, "Data integrity violation"),

    // 429 - Too Many Requests
    TOO_MANY_REQUESTS("TOO_MANY_REQUESTS", HttpStatus.TOO_MANY_REQUESTS, "Too many requests"),

    // 500
    INTERNAL_ERROR("INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");

    private final String code;
    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(String code, HttpStatus status, String defaultMessage) {
        this.code = code;
        this.status = status;
        this.defaultMessage = defaultMessage;
    }
}
