package com.taskflow.backend.controller;

import com.taskflow.backend.dto.project.CreateProjectRequest;
import com.taskflow.backend.security.AppUserPrincipal;
import com.taskflow.backend.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.taskflow.backend.entity.Project;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    //  CREATE PROJECT (OWNER)
    @PostMapping
    public Project create(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable Long workspaceId,
            @RequestBody CreateProjectRequest request) {
        return projectService.create(principal.getId(), workspaceId, request.name());
    }

    //  LIST PROJECTS (OWNER + MEMBER)
    @GetMapping
    public List<Project> list(
            @PathVariable Long workspaceId) {
        return projectService.list(workspaceId);
    }
}
