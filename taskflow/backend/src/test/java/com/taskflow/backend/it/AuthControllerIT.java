package com.taskflow.backend.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.backend.dto.auth.LoginRequest;
import com.taskflow.backend.dto.auth.RegisterRequest;
import com.taskflow.backend.entity.Role;
import com.taskflow.backend.repository.RoleRepository;
import com.taskflow.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class AuthControllerIT extends BaseIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @BeforeEach
    void setup() {
        roleRepository.save(
                Role.builder()
                        .name("USER")
                        .build()
        );
    }

    @Test
    void shouldRegisterSuccessfully() throws Exception {

        String email =
                "user_" + UUID.randomUUID() + "@taskflow.com";

        RegisterRequest request = new RegisterRequest(
                email,
                "123456"
        );

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {

        String email =
                "login_" + UUID.randomUUID() + "@taskflow.com";

        RegisterRequest registerRequest =
                new RegisterRequest(email, "123456");

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest))
                )
                .andExpect(status().isOk());

        LoginRequest loginRequest =
                new LoginRequest(email, "123456");

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }
}
