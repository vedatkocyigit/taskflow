package com.taskflow.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taskflow.backend.entity.TaskComment;

import java.util.List;

public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {

    List<TaskComment> findByTaskIdOrderByCreatedAtAsc(Long taskId);
}
