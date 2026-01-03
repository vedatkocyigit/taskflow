package com.taskflow.backend.service;

import com.taskflow.backend.repository.ProjectRepository;
import com.taskflow.backend.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.taskflow.backend.security.AuthorizationService;

import java.time.LocalDateTime;
import java.util.List;

import com.taskflow.backend.entity.Project;
import com.taskflow.backend.entity.Workspace;

@Service
@RequiredArgsConstructor
public class ProjectService {

        private final ProjectRepository projectRepository;
        private final WorkspaceRepository workspaceRepository;
        private final MemberService memberService;
        private final UserService userService;
        private final ActivityLogService activityLogService;
        private final AuthorizationService authz; // ✅ EKLE

        // CREATE aynen kalıyor
        @Transactional
        public Project create(Long userId, Long workspaceId, String name) {

                memberService.requireOwner(userId, workspaceId);

                Workspace ws = workspaceRepository.findById(workspaceId)
                                .orElseThrow(() -> new RuntimeException("Workspace not found"));

                Project project = projectRepository.save(
                                Project.builder()
                                                .name(name)
                                                .workspace(ws)
                                                .createdAt(LocalDateTime.now())
                                                .build());

                activityLogService.logProject(
                                userService.getById(userId),
                                ws,
                                project.getId(),
                                "PROJECT_CREATED",
                                "Project oluşturuldu: " + name);

                return project;
        }

        // ✅ LIST PROJECTS (OWNER + MEMBER)
        @Transactional(readOnly = true)
        public List<Project> list(Long workspaceId) {

                // 🔐 JWT'deki kullanıcı workspace member mı?
                authz.requireWorkspaceMember(workspaceId);

                return projectRepository.findByWorkspaceId(workspaceId);
        }
}