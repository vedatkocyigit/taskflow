package com.taskflow.backend.dto.workspace;

public record WorkspaceListDto(
        Long id,
        String name,
        String role // OWNER / MEMBER
) {}
