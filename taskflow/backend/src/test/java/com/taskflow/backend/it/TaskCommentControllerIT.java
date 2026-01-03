package com.taskflow.backend.it;

import com.taskflow.backend.entity.*;
import com.taskflow.backend.repository.*;
import com.taskflow.backend.security.AppUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class TaskCommentControllerIT extends BaseIntegrationTest {

    @Autowired
    MockMvc mockMvc;

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

    @Autowired
    TaskCommentRepository taskCommentRepository;

    @Autowired
    ActivityLogRepository activityLogRepository;

    User user;
    Workspace workspace;
    Project project;
    Task task;

    @BeforeEach
    void setup() {

        user = userRepository.save(
                User.builder()
                        .email("user@taskflow.com")
                        .password("pwd")
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        workspace = workspaceRepository.save(
                Workspace.builder()
                        .name("Test Workspace")
                        .owner(user)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        memberRepository.save(
                Member.builder()
                        .user(user)
                        .workspace(workspace)
                        .role("MEMBER")
                        .build()
        );

        project = projectRepository.save(
                Project.builder()
                        .name("Test Project")
                        .workspace(workspace)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        task = taskRepository.save(
                Task.builder()
                        .title("Test Task")
                        .status("TODO")
                        .project(project)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }


    @Test
    void shouldAddComment() throws Exception {

        setPrincipal(user);

        mockMvc.perform(
                        post("/api/workspaces/{ws}/tasks/{task}/comments",
                                workspace.getId(), task.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "content": "İlk yorum"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.content").value("İlk yorum"))
                .andExpect(jsonPath("$.userId").value(user.getId()));
    }


    @Test
    void shouldListComments() throws Exception {

        taskCommentRepository.save(
                TaskComment.builder()
                        .task(task)
                        .user(user)
                        .content("Yorum 1")
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        setPrincipal(user);

        mockMvc.perform(
                        get("/api/workspaces/{ws}/tasks/{task}/comments",
                                workspace.getId(), task.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].content").value("Yorum 1"))
                .andExpect(jsonPath("$[0].userId").value(user.getId()));
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
