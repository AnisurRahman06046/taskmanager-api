package com.app.taskmanager.security;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.app.taskmanager.common.dto.ErrorResponse;
import com.app.taskmanager.common.web.RequestTraceFilter;
import com.app.taskmanager.exception.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

/**
 * Writes a JSON {@link ErrorResponse} directly to the servlet response.
 *
 * Used by the security filter chain when there is no opportunity for the
 * regular {@code @ControllerAdvice} to run (unauthenticated requests, JWT
 * decode failures inside a filter).
 */
@Component
@RequiredArgsConstructor
public class SecurityErrorWriter {

    private final ObjectMapper objectMapper;

    public void write(HttpServletRequest request,
                      HttpServletResponse response,
                      ErrorCode errorCode,
                      String message) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        ErrorResponse body = ErrorResponse.builder()
                .errorCode(errorCode.getCode())
                .message(message != null ? message : errorCode.getDefaultMessage())
                .status(errorCode.getStatus().value())
                .path(request.getRequestURI())
                .method(request.getMethod())
                .timestamp(LocalDateTime.now())
                .traceId(traceId(request))
                .build();

        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }

    private String traceId(HttpServletRequest request) {
        Object attr = request.getAttribute(RequestTraceFilter.TRACE_ID_REQUEST_ATTR);
        return attr != null ? attr.toString() : null;
    }
}
