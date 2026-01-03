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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@Transactional
class ActivityLogControllerIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    private User user;
    private Workspace workspace;

    @BeforeEach
    void setup() {


        user = userRepository.save(
                User.builder()
                        .email("test@taskflow.com")
                        .password("x")
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


        activityLogRepository.save(
                ActivityLog.builder()
                        .actor(user)
                        .workspace(workspace)
                        .action("TASK_CREATED")
                        .description("Task oluşturuldu")
                        .createdAt(LocalDateTime.now().minusMinutes(1))
                        .build()
        );

        activityLogRepository.save(
                ActivityLog.builder()
                        .actor(user)
                        .workspace(workspace)
                        .action("TASK_UPDATED")
                        .description("Task güncellendi")
                        .createdAt(LocalDateTime.now())
                        .build()
        );


        AppUserPrincipal principal = new AppUserPrincipal(user);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                )
        );
    }

    @Test
    void shouldReturnWorkspaceActivityTimeline() throws Exception {

        mockMvc.perform(
                        get("/api/workspaces/{workspaceId}/activities", workspace.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())

                // 2 adet activity
                .andExpect(jsonPath("$.length()").value(2))

                // DESC order → en yeni üstte
                .andExpect(jsonPath("$[0].action").value("TASK_UPDATED"))
                .andExpect(jsonPath("$[0].actorEmail").value("test@taskflow.com"))

                .andExpect(jsonPath("$[1].action").value("TASK_CREATED"));
    }
}
