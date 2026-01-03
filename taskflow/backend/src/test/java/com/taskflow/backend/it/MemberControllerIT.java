package com.taskflow.backend.it;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class MemberControllerIT extends BaseIntegrationTest {

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

        AppUserPrincipal principal =
                new AppUserPrincipal(owner);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of()
                )
        );
    }


    @Test
    void shouldListMembers() throws Exception {

        mockMvc.perform(
                        get("/api/workspaces/{id}/members", workspace.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].email").exists())
                .andExpect(jsonPath("$[0].role").exists());
    }


    @Test
    void shouldAddMember() throws Exception {

        User newUser = userRepository.save(
                User.builder()
                        .email("new@taskflow.com")
                        .password("pwd")
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        mockMvc.perform(
                        post("/api/workspaces/{id}/members", workspace.getId())
                                .param("email", newUser.getEmail())
                                .param("role", "MEMBER")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MEMBER"));
    }

    @Test
    void shouldRemoveMember() throws Exception {

        Member m = memberRepository.findAll()
                .stream()
                .filter(mm -> "MEMBER".equals(mm.getRole()))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(
                        delete("/api/workspaces/{wid}/members/{mid}",
                                workspace.getId(),
                                m.getId())
                )
                .andExpect(status().isOk());
    }
}
