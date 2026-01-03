package com.taskflow.backend.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.backend.entity.*;
import com.taskflow.backend.repository.*;
import com.taskflow.backend.security.AppUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
class TaskControllerIT extends BaseIntegrationTest {

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
    TaskRepository taskRepository;

    User owner;
    User member;
    Workspace workspace;
    Project project;


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
                        .name("Test WS")
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

        project = projectRepository.save(
                Project.builder()
                        .name("Backend Project")
                        .workspace(workspace)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    private UsernamePasswordAuthenticationToken auth(User user) {

        AppUserPrincipal principal =
                new AppUserPrincipal(user);

        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of()
        );
    }


    @Test
    void shouldCreateTask() throws Exception {

        mockMvc.perform(
                        post("/api/projects/{projectId}/tasks", project.getId())
                                .with(authentication(auth(owner)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "title": "Test Task",
                                      "description": "Desc",
                                      "tags": []
                                    }
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void shouldListTasks() throws Exception {

        taskRepository.save(
                Task.builder()
                        .title("Existing Task")
                        .status("TODO")
                        .project(project)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        mockMvc.perform(
                        get("/api/projects/{projectId}/tasks", project.getId())
                                .with(authentication(auth(member)))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Existing Task"));
    }


    @Test
    void shouldUpdateTaskStatus() throws Exception {

        Task task = taskRepository.save(
                Task.builder()
                        .title("Task")
                        .status("TODO")
                        .project(project)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        mockMvc.perform(
                        patch("/api/projects/{projectId}/tasks/{taskId}/status",
                                project.getId(), task.getId())
                                .with(authentication(auth(owner)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    { "status": "DONE" }
                                """)
                )
                .andExpect(status().isOk());
    }


    @Test
    void shouldAssignTaskToMember() throws Exception {

        Task task = taskRepository.save(
                Task.builder()
                        .title("Assign Task")
                        .status("TODO")
                        .project(project)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        mockMvc.perform(
                        patch("/api/projects/{projectId}/tasks/{taskId}/assign/{assigneeId}",
                                project.getId(), task.getId(), member.getId())
                                .with(authentication(auth(owner)))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignee.id").value(member.getId()));
    }
}
