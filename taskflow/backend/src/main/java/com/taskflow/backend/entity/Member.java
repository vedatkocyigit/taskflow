package com.taskflow.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "members")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Workspace workspace;

    @Column(nullable = false)
    private String role; // OWNER, MEMBER
}
