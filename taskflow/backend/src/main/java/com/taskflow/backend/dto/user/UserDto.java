package com.taskflow.backend.dto.user;

import java.util.Set;

public record UserDto(
        Long id,
        String email,
        Set<String> roles
) {}
