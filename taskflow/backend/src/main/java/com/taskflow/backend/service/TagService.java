package com.taskflow.backend.service;

import com.taskflow.backend.common.NotFoundException;
import com.taskflow.backend.dto.tag.TagDto;
import com.taskflow.backend.entity.Tag;
import com.taskflow.backend.entity.Workspace;
import com.taskflow.backend.repository.TagRepository;
import com.taskflow.backend.security.AuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final AuthorizationService authz;
    private final ActivityLogService activityLogService;

    // =========================
    // GET OR CREATE (TaskService için)
    // =========================
    @Transactional
    public Set<Tag> getOrCreateTags(Long workspaceId, Set<String> tagNames) {

        authz.requireWorkspaceMember(workspaceId);
        Workspace ws = authz.requireWorkspace(workspaceId);

        if (tagNames == null || tagNames.isEmpty()) {
            return Set.of();
        }

        return tagNames.stream()
                .map(name -> getOrCreate(ws, name))
                .collect(Collectors.toSet());
    }

    private Tag getOrCreate(Workspace ws, String name) {

        String normalized = name.trim();

        return tagRepository
                .findByWorkspaceIdAndNameIgnoreCase(ws.getId(), normalized)
                .orElseGet(() -> {
                    Tag tag = tagRepository.save(
                            Tag.builder()
                                    .name(normalized)
                                    .workspace(ws)
                                    .build()
                    );

                    activityLogService.log(
                            authz.currentUser(),
                            ws,
                            "TAG_CREATED",
                            "Tag oluşturuldu: " + tag.getName()
                    );

                    return tag;
                });
    }

    // =========================
    // MANUAL CREATE
    // =========================
    @Transactional
    public TagDto create(Long workspaceId, String name, String color) {

        authz.requireWorkspaceMember(workspaceId);
        Workspace ws = authz.requireWorkspace(workspaceId);

        String normalized = name.trim();

        if (tagRepository.existsByWorkspaceIdAndNameIgnoreCase(workspaceId, normalized)) {
            throw new IllegalArgumentException("Tag already exists: " + normalized);
        }

        Tag tag = tagRepository.save(
                Tag.builder()
                        .name(normalized)
                        .color(color)
                        .workspace(ws)
                        .build()
        );

        activityLogService.log(
                authz.currentUser(),
                ws,
                "TAG_CREATED",
                "Tag oluşturuldu: " + tag.getName()
        );

        return toDto(tag);
    }

    // =========================
    // LIST / AUTOCOMPLETE
    // =========================
    @Transactional(readOnly = true)
    public List<TagDto> listAll(Long workspaceId) {

        authz.requireWorkspaceMember(workspaceId);

        return tagRepository.findByWorkspaceId(workspaceId)
                .stream()
                .map(this::toDto)
                .sorted((a, b) -> a.name().compareToIgnoreCase(b.name()))
                .toList();
    }

    // =========================
    // GET BY ID
    // =========================
    @Transactional(readOnly = true)
    public Tag getById(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tag not found id=" + id));
    }

    private TagDto toDto(Tag tag) {
        return new TagDto(
                tag.getId(),
                tag.getName(),
                tag.getColor()
        );
    }
}
