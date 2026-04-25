package com.app.taskmanager.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.app.taskmanager.entity.User;
import com.app.taskmanager.repository.UserRepository;
import com.app.taskmanager.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain filterChain) throws ServletException,IOException {
        String authHeader = req.getHeader("Authorization");
        if(authHeader!=null && authHeader.startsWith("Bearer ")){
            String token = authHeader.substring(7);
            if(jwtUtil.isValid(token)){
                String email = jwtUtil.extractEmail(token);
                User user = userRepository.findByEmail(email).orElse(null);
                if(user!=null){
                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_"+user.getRole().name()));
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user.getEmail(),null,authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
                // System.out.println("Authenticated user: " + email);
            }
        }
        filterChain.doFilter(req, res);
    }
}
