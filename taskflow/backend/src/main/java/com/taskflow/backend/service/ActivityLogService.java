package com.taskflow.backend.service;

import com.taskflow.backend.dto.activity.ActivityLogDto;
import com.taskflow.backend.entity.ActivityLog;
import com.taskflow.backend.entity.User;
import com.taskflow.backend.entity.Workspace;
import com.taskflow.backend.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    // =========================
    // CORE LOG (GENEL)
    // =========================
    @Transactional
    public void log(
            User actor,
            Workspace workspace,
            String action,
            String description
    ) {
        activityLogRepository.save(
                ActivityLog.builder()
                        .actor(actor)
                        .workspace(workspace)
                        .action(action)
                        .description(description)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    // =========================
    // CORE LOG (TASK / PROJECT DESTEKLİ)
    // =========================
    @Transactional
    public void log(
            User actor,
            Workspace workspace,
            String action,
            Long taskId,
            Long projectId,
            String description
    ) {
        activityLogRepository.save(
                ActivityLog.builder()
                        .actor(actor)
                        .workspace(workspace)
                        .action(action)
                        .taskId(taskId)
                        .projectId(projectId)
                        .description(description)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    // =========================
    // SHORTCUT HELPERS
    // =========================
    @Transactional
    public void logTask(
            User actor,
            Workspace workspace,
            Long taskId,
            String action,
            String description
    ) {
        log(actor, workspace, action, taskId, null, description);
    }

    @Transactional
    public void logProject(
            User actor,
            Workspace workspace,
            Long projectId,
            String action,
            String description
    ) {
        log(actor, workspace, action, null, projectId, description);
    }

    // =========================
    // WORKSPACE TIMELINE
    // =========================
    @Transactional(readOnly = true)
    public List<ActivityLogDto> listByWorkspace(Long workspaceId) {
        return activityLogRepository
                .findByWorkspaceIdOrderByCreatedAtDesc(workspaceId)
                .stream()
                .map(l -> new ActivityLogDto(
                        l.getActor().getEmail(),
                        l.getAction(),
                        l.getDescription(),
                        l.getCreatedAt()
                ))
                .toList();
    }
}
