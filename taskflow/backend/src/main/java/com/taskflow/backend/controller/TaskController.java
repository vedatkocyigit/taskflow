package com.taskflow.backend.controller;

import com.taskflow.backend.dto.task.CreateTaskRequest;
import com.taskflow.backend.dto.task.UpdateTaskStatusRequest;
import com.taskflow.backend.security.AppUserPrincipal;
import com.taskflow.backend.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import com.taskflow.backend.entity.Task;
import com.taskflow.backend.dto.task.TaskDto;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@RequiredArgsConstructor
public class TaskController {

        private final TaskService taskService;

        @PostMapping
        public TaskDto create(
                        @AuthenticationPrincipal AppUserPrincipal principal,
                        @PathVariable Long projectId,
                        @RequestBody CreateTaskRequest request) {
                return taskService.create(
                                principal.getId(),
                                projectId,
                                request.title(),
                                request.description(),
                                request.tags());
        }

    @PatchMapping("/{taskId}/tags/{tagId}/attach")
    public TaskDto attachTag(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable Long taskId,
            @PathVariable Long tagId
    ) {
        Task task = taskService.attachTagToTask(
                principal.getId(),
                taskId,
                tagId
        );

        return taskService.toDto(task);
    }

    // =========================
// TAG DETACH
// =========================
    @PatchMapping("/{taskId}/tags/{tagId}/detach")
    public TaskDto detachTag(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable Long taskId,
            @PathVariable Long tagId
    ) {
        Task task = taskService.detachTagFromTask(
                principal.getId(),
                taskId,
                tagId
        );

        return taskService.toDto(task);
    }
        @GetMapping
        public List<TaskDto> list(
                        @AuthenticationPrincipal AppUserPrincipal principal,
                        @PathVariable Long projectId) {
                return taskService.listByProject(
                                principal.getId(),
                                projectId);
        }

        @PatchMapping("/{taskId}/status")
        public void updateStatus(
                        @AuthenticationPrincipal AppUserPrincipal principal,
                        @PathVariable Long taskId,
                        @RequestBody UpdateTaskStatusRequest request) {
                taskService.updateStatus(
                                principal.getId(),
                                taskId,
                                request.status());
        }

        @PatchMapping("/{taskId}/assign/{assigneeId}")
        public Task assign(
                        @AuthenticationPrincipal AppUserPrincipal principal,
                        @PathVariable Long taskId,
                        @PathVariable Long assigneeId) {
                return taskService.assignToTask(
                                principal.getId(),
                                taskId,
                                assigneeId);
        }
}
