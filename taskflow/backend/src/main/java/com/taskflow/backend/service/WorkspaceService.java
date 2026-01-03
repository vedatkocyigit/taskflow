package com.taskflow.backend.service;

import com.taskflow.backend.dto.workspace.WorkspaceListDto;
import com.taskflow.backend.entity.Member;
import com.taskflow.backend.entity.User;
import com.taskflow.backend.entity.Workspace;
import com.taskflow.backend.repository.MemberRepository;
import com.taskflow.backend.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final MemberRepository memberRepository;
    private final UserService userService;
    private final ActivityLogService activityLogService;

    @Transactional
    public Workspace create(Long userId, String name) {

        User owner = userService.getById(userId);

        Workspace workspace = workspaceRepository.save(
                Workspace.builder()
                        .name(name)
                        .owner(owner)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        memberRepository.save(
                Member.builder()
                        .user(owner)
                        .workspace(workspace)
                        .role("OWNER")
                        .build()
        );

        activityLogService.log(
                owner,
                workspace,
                "WORKSPACE_CREATED",
                "Workspace oluşturuldu"
        );

        return workspace;
    }

    @Transactional(readOnly = true)
    public List<WorkspaceListDto> listMyWorkspaces(Long userId) {
        return memberRepository.findByUserId(userId).stream()
                .map(m -> new WorkspaceListDto(
                        m.getWorkspace().getId(),
                        m.getWorkspace().getName(),
                        m.getRole()
                ))
                .toList();
    }

    public Workspace getById(Long id) {
        return workspaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));
    }

}
