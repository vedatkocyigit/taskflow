package com.taskflow.backend.dto.activity;

import java.time.LocalDateTime;

public record ActivityLogDto(
        String actorEmail,
        String action,
        String description,
        LocalDateTime createdAt
) {}
