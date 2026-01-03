package com.taskflow.backend.controller;

import com.taskflow.backend.dto.workspace.WorkspaceMemberDto;
import com.taskflow.backend.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import  com.taskflow.backend.entity.Member;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public List<WorkspaceMemberDto> list(@PathVariable Long workspaceId) {
        return memberService.listMembers(workspaceId);
    }

    @PostMapping
    public Member add(
            @PathVariable Long workspaceId,
            @RequestParam String email,
            @RequestParam(required = false) String role) {
        return memberService.addMember(workspaceId, email, role);
    }

    @DeleteMapping("/{memberId}")
    public void remove(
            @PathVariable Long workspaceId,
            @PathVariable Long memberId) {
        memberService.removeMember(workspaceId, memberId);
    }
}
