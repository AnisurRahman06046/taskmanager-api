package com.app.taskmanager.common.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;

/**
 * Standardized error response payload returned for every failure across the
 * API. Follows a stable shape so clients can rely on the same fields whether
 * the failure originated in a controller, the security layer, or a fallback
 * handler.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    @Builder.Default
    private final boolean success = false;

    private final String errorCode;
    private final String message;
    private final int status;
    private final String path;
    private final String method;
    private final LocalDateTime timestamp;
    private final String traceId;
    private final List<FieldErrorDetail> fieldErrors;
}
