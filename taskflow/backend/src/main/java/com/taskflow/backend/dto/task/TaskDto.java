package com.taskflow.backend.dto.task;

import java.util.Set;

public record TaskDto(
        Long id,
        String title,
        String description,
        String status,
        Long assigneeId,
        Set<String> tags
) {}
