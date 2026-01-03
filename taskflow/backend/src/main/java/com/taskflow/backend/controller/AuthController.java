package com.taskflow.backend.controller;

import com.taskflow.backend.dto.auth.AuthResponse;
import com.taskflow.backend.dto.auth.LoginRequest;
import com.taskflow.backend.dto.auth.RegisterRequest;
import com.taskflow.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return authService.register(
                request.email(),
                request.password()
        );
    }
}
