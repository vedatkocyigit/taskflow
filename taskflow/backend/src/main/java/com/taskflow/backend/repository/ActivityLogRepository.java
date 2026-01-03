package com.taskflow.backend.repository;

import com.taskflow.backend.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findByWorkspaceIdOrderByCreatedAtDesc(Long workspaceId);

    List<ActivityLog> findByTaskIdOrderByCreatedAtDesc(Long taskId);
}
