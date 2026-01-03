package com.taskflow.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Kim yaptı
    @ManyToOne(optional = false)
    private User actor;

    // Hangi workspace
    @ManyToOne(optional = false)
    private Workspace workspace;

    // Ne yaptı
    @Column(nullable = false)
    private String action;
    // TASK_CREATED, TASK_UPDATED, COMMENT_ADDED, MEMBER_ADDED ...

    // Opsiyonel hedefler
    private Long taskId;
    private Long projectId;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
