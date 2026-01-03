package com.taskflow.backend.controller;

import com.taskflow.backend.dto.comment.CreateCommentRequest;
import com.taskflow.backend.dto.comment.TaskCommentDto;
import com.taskflow.backend.security.AppUserPrincipal;
import com.taskflow.backend.service.TaskCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/tasks/{taskId}/comments")
@RequiredArgsConstructor
public class TaskCommentController {

    private final TaskCommentService commentService;

    // =========================
    // ADD COMMENT
    // =========================
    @PostMapping
    public TaskCommentDto add(
            @PathVariable Long workspaceId,
            @PathVariable Long taskId,
            @RequestBody CreateCommentRequest request
    ) {
        return commentService.add(
                workspaceId,
                taskId,
                request.content()
        );
    }

    // =========================
    // LIST COMMENTS
    // =========================
    @GetMapping
    public List<TaskCommentDto> list(
            @PathVariable Long workspaceId,
            @PathVariable Long taskId
    ) {
        return commentService.list(workspaceId, taskId);
    }

    @DeleteMapping("/{commentId}")
    public void delete(
            @PathVariable Long workspaceId,
            @PathVariable Long taskId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        commentService.delete(
                workspaceId,
                taskId,
                commentId,
                principal.getId()
        );
    }

}
