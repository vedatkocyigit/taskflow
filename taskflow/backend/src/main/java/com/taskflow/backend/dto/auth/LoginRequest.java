package com.taskflow.backend.dto.auth;

public record LoginRequest(
        String email,
        String password
) {}
