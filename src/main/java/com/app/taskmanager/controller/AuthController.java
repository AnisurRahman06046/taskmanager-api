package com.app.taskmanager.controller;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.taskmanager.common.dto.ApiResponse;
import com.app.taskmanager.dto.AuthResponse;
import com.app.taskmanager.dto.LoginRequest;
import com.app.taskmanager.dto.RegisterRequest;
import com.app.taskmanager.entity.User;
import com.app.taskmanager.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    public ApiResponse<User> register(@Valid @RequestBody RegisterRequest req) {
        User user = userService.register(req);
        return ApiResponse.<User>builder()
                .success(true)
                .message("User is registered")
                .timestamp(LocalDateTime.now())
                .data(null)
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        AuthResponse result = userService.login(req);
        return ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Login successfull")
                .data(result)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
