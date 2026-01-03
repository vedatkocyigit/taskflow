package com.taskflow.backend.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.backend.dto.workspace.CreateWorkspaceRequest;
import com.taskflow.backend.entity.Role;
import com.taskflow.backend.entity.User;
import com.taskflow.backend.repository.RoleRepository;
import com.taskflow.backend.repository.UserRepository;
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
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
class WorkspaceControllerIT extends BaseIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    User user;


    @BeforeEach
    void setup() {

        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() ->
                        roleRepository.save(
                                Role.builder().name("USER").build()
                        )
                );

        user = userRepository.save(
                User.builder()
                        .email("owner@taskflow.com")
                        .password("pwd")
                        .roles(Set.of(userRole))
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
    void shouldCreateWorkspace() throws Exception {

        CreateWorkspaceRequest request =
                new CreateWorkspaceRequest("My Workspace");

        mockMvc.perform(
                        post("/api/workspaces")
                                .with(authentication(auth(user)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("My Workspace"))
                .andExpect(jsonPath("$.role").value("OWNER"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void shouldListMyWorkspaces() throws Exception {

        CreateWorkspaceRequest request =
                new CreateWorkspaceRequest("Team Space");

        mockMvc.perform(
                        post("/api/workspaces")
                                .with(authentication(auth(user)))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/api/workspaces")
                                .with(authentication(auth(user)))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Team Space"))
                .andExpect(jsonPath("$[0].role").value("OWNER"));
    }
}
