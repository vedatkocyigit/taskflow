package com.taskflow.backend.security;

import com.taskflow.backend.common.NotFoundException;
import com.taskflow.backend.entity.*;
import com.taskflow.backend.repository.MemberRepository;
import com.taskflow.backend.repository.ProjectRepository;
import com.taskflow.backend.repository.TaskRepository;
import com.taskflow.backend.repository.UserRepository;
import com.taskflow.backend.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    /** JWT -> Authentication name = email varsayımı (senin filter bu şekilde set ediyorsa çalışır). */
    public User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new AccessDeniedException("Unauthenticated");
        }
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found for email=" + email));
    }

    public Workspace requireWorkspace(Long workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new NotFoundException("Workspace not found id=" + workspaceId));
    }

    public void requireWorkspaceMember(Long workspaceId) {
        User me = currentUser();
        boolean ok = memberRepository.existsByWorkspaceIdAndUserId(workspaceId, me.getId());
        if (!ok) throw new AccessDeniedException("You are not a member of this workspace");
    }

    public Member requireWorkspaceMemberRow(Long workspaceId) {
        User me = currentUser();
        return memberRepository.findByWorkspaceIdAndUserId(workspaceId, me.getId())
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this workspace"));
    }

    public void requireWorkspaceOwner(Long workspaceId) {
        Member m = requireWorkspaceMemberRow(workspaceId);
        if (!"OWNER".equalsIgnoreCase(m.getRole())) {
            throw new AccessDeniedException("Only OWNER can perform this action");
        }
    }

    public Project requireProjectInWorkspace(Long workspaceId, Long projectId) {
        requireWorkspaceMember(workspaceId);
        Project p = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found id=" + projectId));
        if (!p.getWorkspace().getId().equals(workspaceId)) {
            throw new AccessDeniedException("Project does not belong to this workspace");
        }
        return p;
    }

    public Task requireTaskInWorkspace(Long workspaceId, Long taskId) {
        requireWorkspaceMember(workspaceId);
        Task t = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found id=" + taskId));
        Long taskWsId = t.getProject().getWorkspace().getId();
        if (!taskWsId.equals(workspaceId)) {
            throw new AccessDeniedException("Task does not belong to this workspace");
        }
        return t;
    }
}
