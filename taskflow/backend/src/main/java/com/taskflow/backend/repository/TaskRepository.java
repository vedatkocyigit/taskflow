package com.taskflow.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taskflow.backend.entity.Task;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectId(Long projectId);

    List<Task> findByAssigneeId(Long userId);

    List<Task> findByStatus(String status); // TODO, IN_PROGRESS, DONE
}
