package com.taskflow.backend.dto.auth;

public record RegisterRequest(
        String email,
        String password
) {}
