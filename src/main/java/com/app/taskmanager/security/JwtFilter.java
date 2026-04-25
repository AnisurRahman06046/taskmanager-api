package com.app.taskmanager.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.app.taskmanager.entity.User;
import com.app.taskmanager.exception.ErrorCode;
import com.app.taskmanager.exception.UnauthorizedException;
import com.app.taskmanager.repository.UserRepository;
import com.app.taskmanager.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Validates the {@code Authorization: Bearer ...} header on every request.
 *
 * Behavior:
 *   - No header / wrong scheme  -> request continues unauthenticated; the
 *     {@link JwtAuthenticationEntryPoint} produces a 401 if the endpoint
 *     requires authentication.
 *   - Header present but token is invalid / expired -> filter short-circuits
 *     with a JSON 401 explaining why (so clients can distinguish expired vs
 *     malformed tokens and react accordingly).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final SecurityErrorWriter errorWriter;

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = req.getHeader(AUTH_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(req, res);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            errorWriter.write(req, res, ErrorCode.INVALID_TOKEN, "Bearer token is empty");
            return;
        }

        try {
            String email = jwtUtil.extractEmail(token);
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                log.warn("Token authenticated unknown user '{}'", email);
                errorWriter.write(req, res, ErrorCode.INVALID_TOKEN,
                        "Authenticated user no longer exists");
                return;
            }
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(req, res);
        } catch (UnauthorizedException ex) {
            log.warn("JWT validation failed at {} {}: [{}] {}", req.getMethod(), req.getRequestURI(),
                    ex.getErrorCode().getCode(), ex.getMessage());
            errorWriter.write(req, res, ex.getErrorCode(), ex.getMessage());
        }
    }
}
