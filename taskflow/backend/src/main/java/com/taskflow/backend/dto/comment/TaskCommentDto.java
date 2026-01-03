package com.taskflow.backend.dto.comment;

import java.time.LocalDateTime;

public record TaskCommentDto(
        Long id,
        Long userId,
        String content,
        LocalDateTime createdAt
) {}
