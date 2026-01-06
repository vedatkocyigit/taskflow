package com.taskflow.backend.it;

import com.taskflow.backend.entity.Role;
import com.taskflow.backend.entity.User;
import com.taskflow.backend.repository.RoleRepository;
import com.taskflow.backend.repository.UserRepository;
import com.taskflow.backend.security.AppUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class UserControllerIT extends BaseIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    User user;

    @BeforeEach
    void setup() {

        Role userRole = roleRepository.save(
                Role.builder()
                        .name("USER")
                        .build()
        );

        user = userRepository.save(
                User.builder()
                        .email("test@taskflow.com")
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
    void shouldReturnMyProfile() throws Exception {

        mockMvc.perform(
                        get("/api/users/me")
                                .with(authentication(auth(user)))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.email").value("test@taskflow.com"))
                .andExpect(jsonPath("$.roles[0]").value("USER"));
    }

    @Test
    void shouldFindUserByEmail() throws Exception {

        mockMvc.perform(
                        get("/api/users/search")
                                .with(authentication(auth(user)))
                                .param("email", "test@taskflow.com")
                                .param("workspaceId", "1") 
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@taskflow.com"))
                .andExpect(jsonPath("$.roles[0]").value("USER"));
    }
}
