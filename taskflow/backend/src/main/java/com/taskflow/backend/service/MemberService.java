package com.taskflow.backend.service;

import com.taskflow.backend.common.NotFoundException;
import com.taskflow.backend.dto.workspace.WorkspaceMemberDto;
import com.taskflow.backend.entity.User;
import com.taskflow.backend.repository.MemberRepository;
import com.taskflow.backend.repository.UserRepository;
import com.taskflow.backend.security.AccessDeniedException;
import com.taskflow.backend.security.AuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskflow.backend.entity.Member;
import com.taskflow.backend.entity.Workspace;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final AuthorizationService authz;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    public Member requireMember(Long userId, Long workspaceId) {
        return memberRepository.findByUserIdAndWorkspaceId(userId, workspaceId)
                .orElseThrow(() -> new RuntimeException("Not a workspace member"));
    }

    public void requireOwner(Long userId, Long workspaceId) {
        Member m = requireMember(userId, workspaceId);
        if (!"OWNER".equals(m.getRole())) {
            throw new RuntimeException("Owner permission required");
        }
    }

    // =========================
    // ADD MEMBER
    // =========================
    @Transactional
    public Member addMember(Long workspaceId, String email, String role) {

        authz.requireWorkspaceOwner(workspaceId);

        Workspace ws = authz.requireWorkspace(workspaceId);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found email=" + email));

        if (memberRepository.existsByWorkspaceIdAndUserId(workspaceId, user.getId())) {
            throw new IllegalArgumentException("User already member of workspace");
        }

        String finalRole = (role == null || role.isBlank()) ? "MEMBER" : role;

        Member member = memberRepository.save(
                Member.builder()
                        .workspace(ws)
                        .user(user)
                        .role(finalRole)
                        .build());

        activityLogService.log(
                authz.currentUser(),
                ws,
                "MEMBER_ADDED",
                user.getEmail() + " workspace'e eklendi");

        return member;
    }

    // =========================
    // LIST MEMBERS (GÜNCEL)
    // =========================
    @Transactional(readOnly = true)
    public List<WorkspaceMemberDto> listMembers(Long workspaceId) {

        authz.requireWorkspaceMember(workspaceId);

        return memberRepository.findAllByWorkspaceId(workspaceId)
                .stream()
                .map(m -> new WorkspaceMemberDto(
                        m.getId(), // ✅ memberId
                        m.getUser().getId(), // userId
                        m.getUser().getEmail(),
                        m.getRole()))
                .toList();
    }

    // =========================
    // REMOVE MEMBER
    // =========================
    @Transactional
    public void removeMember(Long workspaceId, Long memberId) {

        authz.requireWorkspaceOwner(workspaceId);

        Member m = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member not found id=" + memberId));

        if (!m.getWorkspace().getId().equals(workspaceId)) {
            throw new AccessDeniedException("Member does not belong to this workspace");
        }

        memberRepository.delete(m);

        activityLogService.log(
                authz.currentUser(),
                m.getWorkspace(),
                "MEMBER_REMOVED",
                m.getUser().getEmail() + " workspace'den çıkarıldı");
    }
}
