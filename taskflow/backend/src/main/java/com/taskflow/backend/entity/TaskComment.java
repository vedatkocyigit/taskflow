package com.taskflow.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_comments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TaskComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Task task;

    @ManyToOne(optional = false)
    private User user;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
