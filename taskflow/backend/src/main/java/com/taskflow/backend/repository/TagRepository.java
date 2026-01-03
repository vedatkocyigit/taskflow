package com.taskflow.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.taskflow.backend.entity.Tag;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);
    boolean existsByNameIgnoreCase(String name);
    Optional<Tag> findByNameIgnoreCase(String name);
    List<Tag> findByWorkspaceId(Long workspaceId);
    Optional<Tag> findByWorkspaceIdAndNameIgnoreCase(Long workspaceId, String name);
    boolean existsByWorkspaceIdAndNameIgnoreCase(Long workspaceId, String name);

}
