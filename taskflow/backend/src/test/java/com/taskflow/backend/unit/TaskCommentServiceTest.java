package com.taskflow.backend.unit;

import com.taskflow.backend.dto.comment.TaskCommentDto;
import com.taskflow.backend.entity.*;
import com.taskflow.backend.repository.TaskCommentRepository;
import com.taskflow.backend.repository.TaskRepository;
import com.taskflow.backend.security.AuthorizationService;
import com.taskflow.backend.service.ActivityLogService;
import com.taskflow.backend.service.TaskCommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskCommentServiceTest {


    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskCommentRepository commentRepository;

    @Mock
    private AuthorizationService authz;

    @Mock
    private ActivityLogService activityLogService;



    @InjectMocks
    private TaskCommentService taskCommentService;



    private Workspace workspace;
    private Project project;
    private Task task;
    private User user;
    private TaskComment comment;

    @BeforeEach
    void setup() {

        workspace = Workspace.builder()
                .id(1L)
                .name("Test Workspace")
                .build();

        project = Project.builder()
                .id(2L)
                .name("Test Project")
                .workspace(workspace)
                .build();

        task = Task.builder()
                .id(3L)
                .title("Test Task")
                .project(project)
                .build();

        user = User.builder()
                .id(10L)
                .email("user@test.com")
                .build();

        comment = TaskComment.builder()
                .id(100L)
                .task(task)
                .user(user)
                .content("Bu bir test yorumudur")
                .createdAt(LocalDateTime.now())
                .build();
    }


    @Test
    void shouldAddCommentSuccessfully() {

        when(authz.requireTaskInWorkspace(1L, 3L))
                .thenReturn(task);

        when(authz.currentUser())
                .thenReturn(user);

        when(commentRepository.save(any(TaskComment.class)))
                .thenAnswer(inv -> {
                    TaskComment c = inv.getArgument(0);
                    c.setId(100L);
                    return c;
                });

        TaskCommentDto dto =
                taskCommentService.add(1L, 3L, "Bu bir test yorumudur");

        assertThat(dto.id()).isEqualTo(100L);
        assertThat(dto.userId()).isEqualTo(10L);
        assertThat(dto.content()).isEqualTo("Bu bir test yorumudur");
        assertThat(dto.createdAt()).isNotNull();

        verify(activityLogService).logTask(
                user,
                workspace,
                task.getId(),
                "COMMENT_ADDED",
                "Task'a yorum eklendi"
        );
    }



    @Test
    void shouldListCommentsOrderedByCreatedAt() {

        when(authz.requireTaskInWorkspace(1L, 3L))
                .thenReturn(task);

        when(commentRepository.findByTaskIdOrderByCreatedAtAsc(3L))
                .thenReturn(List.of(comment));

        List<TaskCommentDto> result =
                taskCommentService.list(1L, 3L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).content())
                .isEqualTo("Bu bir test yorumudur");
        assertThat(result.get(0).userId())
                .isEqualTo(10L);
    }
}
