package com.app.taskmanager.exception;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.app.taskmanager.common.dto.ErrorResponse;
import com.app.taskmanager.common.dto.FieldErrorDetail;
import com.app.taskmanager.common.web.RequestTraceFilter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

/**
 * Centralized translation of exceptions to API error responses.
 *
 * The handler is intentionally exhaustive: every well-known framework
 * exception is mapped to a stable {@link ErrorCode} so clients always see a
 * consistent JSON shape. Unknown exceptions fall through to a 500 with the
 * cause logged but the message redacted from the response.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---- Application typed exceptions --------------------------------------

    @ExceptionHandler(BaseApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(BaseApiException ex, HttpServletRequest request) {
        HttpStatus status = ex.getStatus();
        if (status.is5xxServerError()) {
            log.error("API exception [{}] at {} {}: {}", ex.getErrorCode().getCode(),
                    request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        } else {
            log.warn("API exception [{}] at {} {}: {}", ex.getErrorCode().getCode(),
                    request.getMethod(), request.getRequestURI(), ex.getMessage());
        }
        return build(ex.getErrorCode(), ex.getMessage(), request, null);
    }

    // ---- Bean Validation ---------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                           HttpServletRequest request) {
        List<FieldErrorDetail> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> FieldErrorDetail.builder()
                        .field(fe.getField())
                        .rejectedValue(fe.getRejectedValue())
                        .message(fe.getDefaultMessage())
                        .build())
                .toList();
        log.warn("Validation failed at {} {}: {} field error(s)", request.getMethod(),
                request.getRequestURI(), fieldErrors.size());
        return build(ErrorCode.VALIDATION_FAILED, ErrorCode.VALIDATION_FAILED.getDefaultMessage(),
                request, fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
                                                                    HttpServletRequest request) {
        List<FieldErrorDetail> fieldErrors = ex.getConstraintViolations().stream()
                .map(v -> FieldErrorDetail.builder()
                        .field(v.getPropertyPath().toString())
                        .rejectedValue(v.getInvalidValue())
                        .message(v.getMessage())
                        .build())
                .toList();
        log.warn("Constraint violation at {} {}: {}", request.getMethod(), request.getRequestURI(),
                ex.getMessage());
        return build(ErrorCode.VALIDATION_FAILED, ErrorCode.VALIDATION_FAILED.getDefaultMessage(),
                request, fieldErrors);
    }

    // ---- Request parsing / binding ----------------------------------------

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex,
                                                           HttpServletRequest request) {
        log.warn("Malformed request body at {} {}: {}", request.getMethod(), request.getRequestURI(),
                ex.getMostSpecificCause().getMessage());
        return build(ErrorCode.MALFORMED_JSON, ErrorCode.MALFORMED_JSON.getDefaultMessage(),
                request, null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex,
                                                             HttpServletRequest request) {
        String message = "Required parameter '%s' of type %s is missing"
                .formatted(ex.getParameterName(), ex.getParameterType());
        log.warn("Missing parameter at {} {}: {}", request.getMethod(), request.getRequestURI(), message);
        return build(ErrorCode.MISSING_PARAMETER, message, request, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                             HttpServletRequest request) {
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        String message = "Parameter '%s' should be of type %s".formatted(ex.getName(), requiredType);
        log.warn("Type mismatch at {} {}: {}", request.getMethod(), request.getRequestURI(), message);
        return build(ErrorCode.TYPE_MISMATCH, message, request, null);
    }

    // ---- Routing -----------------------------------------------------------

    @ExceptionHandler({ NoResourceFoundException.class, NoHandlerFoundException.class })
    public ResponseEntity<ErrorResponse> handleNotFound(Exception ex, HttpServletRequest request) {
        log.warn("No handler for {} {}", request.getMethod(), request.getRequestURI());
        return build(ErrorCode.ENDPOINT_NOT_FOUND, ErrorCode.ENDPOINT_NOT_FOUND.getDefaultMessage(),
                request, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex,
                                                                 HttpServletRequest request) {
        log.warn("Method not allowed at {} {}: {}", request.getMethod(), request.getRequestURI(),
                ex.getMessage());
        return build(ErrorCode.METHOD_NOT_ALLOWED, ex.getMessage(), request, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex,
                                                                     HttpServletRequest request) {
        log.warn("Unsupported media type at {} {}: {}", request.getMethod(), request.getRequestURI(),
                ex.getMessage());
        return build(ErrorCode.UNSUPPORTED_MEDIA_TYPE, ex.getMessage(), request, null);
    }

    // ---- Spring Security ---------------------------------------------------

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex,
                                                             HttpServletRequest request) {
        log.warn("Access denied at {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return build(ErrorCode.ACCESS_DENIED, ErrorCode.ACCESS_DENIED.getDefaultMessage(), request, null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex,
                                                               HttpServletRequest request) {
        log.warn("Authentication failure at {} {}: {}", request.getMethod(), request.getRequestURI(),
                ex.getMessage());
        return build(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.getDefaultMessage(), request, null);
    }

    // ---- Persistence -------------------------------------------------------

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
                                                              HttpServletRequest request) {
        log.warn("Data integrity violation at {} {}: {}", request.getMethod(), request.getRequestURI(),
                ex.getMostSpecificCause().getMessage());
        return build(ErrorCode.DATA_INTEGRITY_VIOLATION,
                ErrorCode.DATA_INTEGRITY_VIOLATION.getDefaultMessage(), request, null);
    }

    // ---- Fallback ----------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAny(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {} {}: {}", request.getMethod(), request.getRequestURI(),
                ex.getMessage(), ex);
        return build(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getDefaultMessage(), request, null);
    }

    // ---- helpers -----------------------------------------------------------

    private ResponseEntity<ErrorResponse> build(ErrorCode errorCode,
                                                 String message,
                                                 HttpServletRequest request,
                                                 List<FieldErrorDetail> fieldErrors) {
        ErrorResponse body = ErrorResponse.builder()
                .errorCode(errorCode.getCode())
                .message(message != null ? message : errorCode.getDefaultMessage())
                .status(errorCode.getStatus().value())
                .path(request.getRequestURI())
                .method(request.getMethod())
                .timestamp(LocalDateTime.now())
                .traceId(traceId(request))
                .fieldErrors(fieldErrors == null || fieldErrors.isEmpty() ? null : fieldErrors)
                .build();
        return ResponseEntity.status(errorCode.getStatus()).body(body);
    }

    private String traceId(HttpServletRequest request) {
        Object attr = request.getAttribute(RequestTraceFilter.TRACE_ID_REQUEST_ATTR);
        return attr != null ? attr.toString() : null;
    }
}
