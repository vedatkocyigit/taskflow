package com.taskflow.backend.repository;

import com.taskflow.backend.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    List<Workspace> findByOwnerId(Long ownerId);

    @Query("""
        select m.workspace
        from Member m
        where m.user.id = :userId
    """)
    List<Workspace> findWorkspacesUserIsMemberOf(Long userId);
}
