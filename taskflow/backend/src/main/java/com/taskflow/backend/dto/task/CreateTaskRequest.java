package com.taskflow.backend.dto.task;

import java.util.Set;

public record CreateTaskRequest(
        String title,
        String description,
        Set<String> tags
) {}
