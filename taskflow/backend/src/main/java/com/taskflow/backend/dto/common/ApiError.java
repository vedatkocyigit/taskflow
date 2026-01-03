package com.taskflow.backend.dto.common;

import java.time.LocalDateTime;

public record ApiError(
        String message,
        LocalDateTime timestamp
) {}
