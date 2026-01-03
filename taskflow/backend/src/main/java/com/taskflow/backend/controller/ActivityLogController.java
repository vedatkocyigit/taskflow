package com.taskflow.backend.controller;

import com.taskflow.backend.dto.activity.ActivityLogDto;
import com.taskflow.backend.security.AppUserPrincipal;
import com.taskflow.backend.service.ActivityLogService;
import com.taskflow.backend.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/activities")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;
    private final MemberService memberService;

    @GetMapping
    public List<ActivityLogDto> list(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @PathVariable Long workspaceId
    ) {
        memberService.requireMember(principal.getId(), workspaceId);
        return activityLogService.listByWorkspace(workspaceId);
    }
}
