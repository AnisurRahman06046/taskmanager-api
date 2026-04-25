package com.app.taskmanager.exception;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.app.taskmanager.common.dto.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<?> handleRuntime(RuntimeException ex){
        return ApiResponse.builder()
        .success(false)
        .message(ex.getMessage())
        .data(null)
        .timestamp(LocalDateTime.now())
        .build();
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<?> handleForbidden(){
        return ResponseEntity.status(403).body("forbidden");
    }
}
