package com.taskflow.backend.unit;

import com.taskflow.backend.dto.auth.AuthResponse;
import com.taskflow.backend.dto.auth.LoginRequest;
import com.taskflow.backend.entity.Role;
import com.taskflow.backend.entity.User;
import com.taskflow.backend.repository.RoleRepository;
import com.taskflow.backend.repository.UserRepository;
import com.taskflow.backend.security.JwtService;
import com.taskflow.backend.security.JwtServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.taskflow.backend.service.AuthService;


@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtServiceImpl jwtService;

    @InjectMocks
    private AuthService authService;

    private User user;
    private Role userRole;


    @BeforeEach
    void setup() {

        userRole = Role.builder()
                .id(1L)
                .name("USER")
                .build();

        user = User.builder()
                .id(10L)
                .email("test@taskflow.com")
                .password("encoded-password")
                .roles(Set.of(userRole))
                .createdAt(LocalDateTime.now())
                .build();
    }


    @Test
    void shouldLoginSuccessfully() {

        LoginRequest request =
                new LoginRequest("test@taskflow.com", "123456");

        when(userRepository.findByEmail("test@taskflow.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("123456", "encoded-password"))
                .thenReturn(true);

        when(jwtService.generateToken(user))
                .thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("jwt-token");

        verify(jwtService).generateToken(user);
    }


    @Test
    void shouldFailLoginWhenEmailNotFound() {

        LoginRequest request =
                new LoginRequest("wrong@taskflow.com", "123456");

        when(userRepository.findByEmail("wrong@taskflow.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid credentials");
    }


    @Test
    void shouldFailLoginWhenPasswordInvalid() {

        LoginRequest request =
                new LoginRequest("test@taskflow.com", "wrong");

        when(userRepository.findByEmail("test@taskflow.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("wrong", "encoded-password"))
                .thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid credentials");
    }


    @Test
    void shouldRegisterSuccessfully() {

        when(userRepository.existsByEmail("new@taskflow.com"))
                .thenReturn(false);

        when(roleRepository.findByName("USER"))
                .thenReturn(Optional.of(userRole));

        when(passwordEncoder.encode("123456"))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        when(jwtService.generateToken(any(User.class)))
                .thenReturn("jwt-token");

        AuthResponse response =
                authService.register("New@Taskflow.com", "123456");

        assertThat(response.accessToken()).isEqualTo("jwt-token");

        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
    }


    @Test
    void shouldFailRegisterWhenEmailExists() {

        when(userRepository.existsByEmail("test@taskflow.com"))
                .thenReturn(true);

        assertThatThrownBy(() ->
                authService.register("test@taskflow.com", "123456")
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email already exists");
    }


    @Test
    void shouldFailRegisterWhenRoleMissing() {

        when(userRepository.existsByEmail("new@taskflow.com"))
                .thenReturn(false);

        when(roleRepository.findByName("USER"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                authService.register("new@taskflow.com", "123456")
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessage("USER role missing");
    }
}
