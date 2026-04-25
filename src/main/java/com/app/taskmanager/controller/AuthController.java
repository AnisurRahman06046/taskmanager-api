package com.app.taskmanager.controller;

import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.taskmanager.common.dto.ApiResponse;
import com.app.taskmanager.common.security.LoginRateLimiter;
import com.app.taskmanager.dto.AuthResponse;
import com.app.taskmanager.dto.LoginRequest;
import com.app.taskmanager.dto.RegisterRequest;
import com.app.taskmanager.entity.User;
import com.app.taskmanager.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final LoginRateLimiter loginRateLimiter;

    @PostMapping("/register")
    public ApiResponse<User> register(@Valid @RequestBody RegisterRequest req) {
        userService.register(req);
        return ApiResponse.<User>builder()
                .success(true)
                .message("User is registered")
                .timestamp(LocalDateTime.now())
                .data(null)
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest req,
                                            HttpServletRequest httpRequest) {
        String clientIp = clientIp(httpRequest);
        String email = req.getEmail() == null ? "" : req.getEmail().trim().toLowerCase(Locale.ROOT);

        // Limit per IP and per email independently so neither a single client
        // hammering many accounts nor many clients hammering one account can
        // get past the budget.
        loginRateLimiter.check("ip:" + clientIp);
        loginRateLimiter.check("email:" + email);

        AuthResponse result = userService.login(req);
        return ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Login successfull")
                .data(result)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Best-effort client IP extraction. Honors {@code X-Forwarded-For} so that
     * when running behind a reverse proxy the real client is rate-limited
     * rather than the proxy. Only the first hop is used because the rest of
     * the chain is attacker-controlled.
     *
     * <p>If you deploy behind a proxy you do not trust to set this header,
     * configure Spring's {@code ForwardedHeaderFilter} or strip the header
     * at the edge instead of relying on this method.
     */
    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            return (comma >= 0 ? forwarded.substring(0, comma) : forwarded).trim();
        }
        return request.getRemoteAddr();
    }
}
