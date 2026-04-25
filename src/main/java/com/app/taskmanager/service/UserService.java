package com.app.taskmanager.service;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.taskmanager.dto.AuthResponse;
import com.app.taskmanager.dto.LoginRequest;
import com.app.taskmanager.dto.RegisterRequest;
import com.app.taskmanager.entity.Role;
import com.app.taskmanager.entity.User;
import com.app.taskmanager.exception.ConflictException;
import com.app.taskmanager.exception.ErrorCode;
import com.app.taskmanager.exception.UnauthorizedException;
import com.app.taskmanager.repository.UserRepository;
import com.app.taskmanager.util.JwtUtil;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Pre-computed BCrypt hash used as a decoy when a login is attempted
     * against a non-existent email. Comparing the supplied password against
     * this hash makes the missing-user path take the same time as the
     * wrong-password path, closing the timing channel that otherwise leaks
     * which emails are registered.
     */
    private final String dummyHash;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.dummyHash = passwordEncoder.encode("dummy-password-for-timing-attack-prevention");
    }

    public User register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ConflictException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = User.builder()
                .name(request.getName())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        return userRepository.save(user);
    }

    public AuthResponse login(LoginRequest req) {
        String email = normalizeEmail(req.getEmail());
        User user = userRepository.findByEmail(email).orElse(null);

        // Always run a BCrypt comparison — against the real hash if the user
        // exists, against a dummy hash otherwise — so timing does not reveal
        // whether the email is registered.
        String hashToCompare = user != null ? user.getPassword() : dummyHash;
        boolean matches = passwordEncoder.matches(req.getPassword(), hashToCompare);

        if (user == null || !matches) {
            throw new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS);
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return AuthResponse.builder()
                .accessToken(token)
                .build();
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
