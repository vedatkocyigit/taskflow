package com.taskflow.backend.unit;

import com.taskflow.backend.entity.User;
import com.taskflow.backend.repository.UserRepository;
import com.taskflow.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {



    @Mock
    private UserRepository userRepository;


    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setup() {
        user = User.builder()
                .id(1L)
                .email("test@taskflow.com")
                .build();
    }


    @Test
    void shouldGetUserByIdSuccessfully() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        User result = userService.getById(1L);

        assertThat(result.getEmail()).isEqualTo("test@taskflow.com");
        verify(userRepository).findById(1L);
    }

    @Test
    void shouldThrowWhenUserNotFoundById() {

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    @Test
    void shouldGetUserByEmailSuccessfully() {

        when(userRepository.findByEmail("test@taskflow.com"))
                .thenReturn(Optional.of(user));

        User result = userService.getByEmail("test@taskflow.com");

        assertThat(result.getId()).isEqualTo(1L);
        verify(userRepository).findByEmail("test@taskflow.com");
    }


    @Test
    void shouldThrowWhenUserNotFoundByEmail() {

        when(userRepository.findByEmail("missing@taskflow.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userService.getByEmail("missing@taskflow.com")
        ).isInstanceOf(RuntimeException.class)
                .hasMessage("User not found email=missing@taskflow.com");
    }
}
