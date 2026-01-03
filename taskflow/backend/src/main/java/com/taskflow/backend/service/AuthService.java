package com.taskflow.backend.service;

import com.taskflow.backend.dto.auth.AuthResponse;
import com.taskflow.backend.dto.auth.LoginRequest;
import com.taskflow.backend.entity.User;
import com.taskflow.backend.repository.RoleRepository;
import com.taskflow.backend.repository.UserRepository;
import com.taskflow.backend.security.JwtServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskflow.backend.entity.Role;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtServiceImpl jwtService;

    public AuthResponse login(LoginRequest request) {

        String email = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return new AuthResponse(jwtService.generateToken(user));
    }

    @Transactional
    public AuthResponse register(String email, String password) {

        String normalizedEmail = email.trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new RuntimeException("Email already exists");
        }

        Role role = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("USER role missing"));

        User user = userRepository.save(
                User.builder()
                        .email(normalizedEmail)
                        .password(passwordEncoder.encode(password))
                        .roles(Set.of(role))
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }

}

