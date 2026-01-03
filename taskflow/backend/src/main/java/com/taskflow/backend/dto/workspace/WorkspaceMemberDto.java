package com.taskflow.backend.dto.workspace;

public record WorkspaceMemberDto(
                Long memberId,
                Long userId,
                String email,
                String role) {
}
