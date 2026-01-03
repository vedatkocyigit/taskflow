package com.taskflow.backend.service;

import com.taskflow.backend.common.NotFoundException;
import com.taskflow.backend.dto.comment.TaskCommentDto;
import com.taskflow.backend.entity.Task;
import com.taskflow.backend.entity.TaskComment;
import com.taskflow.backend.entity.User;
import com.taskflow.backend.repository.TaskCommentRepository;
import com.taskflow.backend.repository.TaskRepository;
import com.taskflow.backend.security.AccessDeniedException;
import com.taskflow.backend.security.AuthorizationService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskCommentService {

    private final TaskRepository taskRepository;
    private final TaskCommentRepository commentRepository;
    private final AuthorizationService authz;
    private final ActivityLogService activityLogService;

    // =========================
    // ADD COMMENT
    // =========================
    @Transactional
    public TaskCommentDto add(Long workspaceId, Long taskId, String content) {

        // 🔐 Task + Workspace + Member kontrolü
        Task task = authz.requireTaskInWorkspace(workspaceId, taskId);

        // 🔐 JWT'den gelen user
        User me = authz.currentUser();

        TaskComment comment = commentRepository.save(
                TaskComment.builder()
                        .task(task)
                        .user(me)
                        .content(content)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        activityLogService.logTask(
                me,
                task.getProject().getWorkspace(),
                task.getId(),
                "COMMENT_ADDED",
                "Task'a yorum eklendi"
        );


        return new TaskCommentDto(
                comment.getId(),
                me.getId(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<TaskCommentDto> list(Long workspaceId, Long taskId) {

        authz.requireTaskInWorkspace(workspaceId, taskId);

        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId)
                .stream()
                .map(c -> new TaskCommentDto(
                        c.getId(),
                        c.getUser().getId(),
                        c.getContent(),
                        c.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public void delete(Long workspaceId, Long taskId, Long commentId, Long userId) {

        TaskComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Cannot delete others' comments");
        }

        commentRepository.delete(comment);

        activityLogService.logTask(
                comment.getUser(),
                comment.getTask().getProject().getWorkspace(),
                taskId,
                "COMMENT_DELETED",
                "Yorum silindi"
        );
    }

}
