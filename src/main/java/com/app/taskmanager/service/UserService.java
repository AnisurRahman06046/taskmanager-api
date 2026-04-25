package com.app.taskmanager.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.taskmanager.dto.AuthResponse;
import com.app.taskmanager.dto.LoginRequest;
import com.app.taskmanager.dto.RegisterRequest;
import com.app.taskmanager.entity.Role;
import com.app.taskmanager.entity.User;
import com.app.taskmanager.repository.UserRepository;
import com.app.taskmanager.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // public UserService(UserRepository userRepository,PasswordEncoder
    // passwordEncoder){
    // this.userRepository=userRepository;
    // this.passwordEncoder=passwordEncoder;
    // }
    public User register(RegisterRequest request) {
        // check if email exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder().name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        return userRepository.save(user);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        String token = jwtUtil.generateToken(user.getEmail());
        return AuthResponse.builder()
                .accessToken(token)
                .build();
    }
}
