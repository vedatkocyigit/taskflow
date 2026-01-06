package com.taskflow.backend.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.backend.dto.project.CreateProjectRequest;
import com.taskflow.backend.entity.*;
import com.taskflow.backend.repository.*;
import com.taskflow.backend.security.AppUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class ProjectControllerIT extends BaseIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    WorkspaceRepository workspaceRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ActivityLogRepository activityLogRepository;

    User owner;
    User member;
    Workspace workspace;

    @BeforeEach
    void setup() {

        owner = userRepository.save(
                User.builder()
                        .email("owner@taskflow.com")
                        .password("pwd")
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        member = userRepository.save(
                User.builder()
                        .email("member@taskflow.com")
                        .password("pwd")
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        workspace = workspaceRepository.save(
                Workspace.builder()
                        .name("Test Workspace")
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

        memberRepository.save(
                Member.builder()
                        .user(member)
                        .workspace(workspace)
                        .role("MEMBER")
                        .build()
        );
    }


    @Test
    void shouldCreateProject() throws Exception {

        setPrincipal(owner);

        CreateProjectRequest request =
                new CreateProjectRequest("Backend Project");

        mockMvc.perform(
                        post("/api/workspaces/{id}/projects", workspace.getId())
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Backend Project"));
    }


    @Test
    void shouldListProjectsAsMember() throws Exception {

        projectRepository.save(
                Project.builder()
                        .name("Test Project")
                        .workspace(workspace)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        setPrincipal(member);

        mockMvc.perform(
                        get("/api/workspaces/{id}/projects", workspace.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Project"));
    }


    private void setPrincipal(User user) {
        AppUserPrincipal principal =
                new AppUserPrincipal(user);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of()
                )
        );
    }
}
