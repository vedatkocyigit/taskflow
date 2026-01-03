package com.taskflow.backend.controller;

import com.taskflow.backend.dto.workspace.CreateWorkspaceRequest;
import com.taskflow.backend.dto.workspace.WorkspaceListDto;
import com.taskflow.backend.entity.Workspace;
import com.taskflow.backend.security.AppUserPrincipal;
import com.taskflow.backend.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    public WorkspaceListDto create(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @RequestBody CreateWorkspaceRequest request
    ) {
        Workspace ws = workspaceService.create(principal.getId(), request.name());

        return new WorkspaceListDto(
                ws.getId(),
                ws.getName(),
                "OWNER"
        );
    }

    @GetMapping
    public List<WorkspaceListDto> myWorkspaces(
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return workspaceService.listMyWorkspaces(principal.getId());
    }
}
