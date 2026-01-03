package com.taskflow.backend.it;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class TagControllerIT extends BaseIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    WorkspaceRepository workspaceRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    ActivityLogRepository activityLogRepository;

    User user;
    Workspace workspace;

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
                        .role("OWNER")
                        .build()
        );
    }


    @Test
    void shouldListTags() throws Exception {

        tagRepository.save(
                Tag.builder()
                        .name("backend")
                        .color("#ff0000")
                        .workspace(workspace)
                        .build()
        );

        setPrincipal(user);

        mockMvc.perform(
                        get("/api/workspaces/{id}/tags", workspace.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("backend"));
    }

    @Test
    void shouldCreateTag() throws Exception {

        setPrincipal(user);

        mockMvc.perform(
                        post("/api/workspaces/{id}/tags", workspace.getId())
                                .param("name", "frontend")
                                .param("color", "#00ff00")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("frontend"))
                .andExpect(jsonPath("$.color").value("#00ff00"));
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
