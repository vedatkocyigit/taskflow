package com.taskflow.backend.unit;

import com.taskflow.backend.dto.task.TaskDto;
import com.taskflow.backend.entity.*;
import com.taskflow.backend.repository.*;
import com.taskflow.backend.security.AccessDeniedException;
import com.taskflow.backend.service.ActivityLogService;
import com.taskflow.backend.service.MemberService;
import com.taskflow.backend.service.TaskService;
import com.taskflow.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private TagRepository tagRepository;
    @Mock private UserRepository userRepository;
    @Mock private MemberRepository memberRepository;

    @Mock private UserService userService;
    @Mock private MemberService memberService;
    @Mock private ActivityLogService activityLogService;

    @InjectMocks
    private TaskService taskService;

    private Workspace workspace;
    private Project project;
    private User user;
    private Task task;
    private Tag tag;

    @BeforeEach
    void setup() {

        workspace = Workspace.builder()
                .id(1L)
                .name("Workspace")
                .build();

        project = Project.builder()
                .id(2L)
                .name("Project")
                .workspace(workspace)
                .build();

        user = User.builder()
                .id(10L)
                .email("user@test.com")
                .build();

        tag = Tag.builder()
                .id(50L)
                .name("backend")
                .build();

        task = Task.builder()
                .id(100L)
                .title("Test Task")
                .description("desc")
                .status("TODO")
                .project(project)
                // ⚠️ MUTABLE SET
                .tags(new HashSet<>(Set.of(tag)))
                .createdAt(LocalDateTime.now())
                .build();
    }


    @Test
    void shouldCreateTaskSuccessfully() {

        when(projectRepository.findById(2L))
                .thenReturn(Optional.of(project));

        when(memberService.requireMember(any(), any()))
                .thenReturn(Member.builder().role("MEMBER").build());

        when(tagRepository.findByNameIgnoreCase("backend"))
                .thenReturn(Optional.of(tag));

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(inv -> {
                    Task t = inv.getArgument(0);
                    t.setId(100L);
                    return t;
                });

        when(userService.getById(10L))
                .thenReturn(user);

        TaskDto dto = taskService.create(
                10L, 2L, "Test Task", "desc", Set.of("backend"));

        assertThat(dto.id()).isEqualTo(100L);
    }

    @Test
    void shouldUpdateTaskStatus() {

        when(taskRepository.findById(100L))
                .thenReturn(Optional.of(task));

        when(memberService.requireMember(any(), any()))
                .thenReturn(Member.builder().build());

        when(userService.getById(10L))
                .thenReturn(user);

        taskService.updateStatus(10L, 100L, "DONE");

        assertThat(task.getStatus()).isEqualTo("DONE");
    }

    @Test
    void shouldAssignTaskToUser() {

        when(taskRepository.findById(100L))
                .thenReturn(Optional.of(task));

        doNothing().when(memberService)
                .requireOwner(any(), any());

        when(memberRepository.existsByWorkspaceIdAndUserId(1L, 20L))
                .thenReturn(true);

        User assignee = User.builder()
                .id(20L)
                .email("assignee@test.com")
                .build();

        when(userRepository.findById(20L))
                .thenReturn(Optional.of(assignee));

        when(userService.getById(10L))
                .thenReturn(user);

        Task result =
                taskService.assignToTask(10L, 100L, 20L);

        assertThat(result.getAssignee().getId()).isEqualTo(20L);
    }


    @Test
    void shouldAttachTagToTask() {

        when(taskRepository.findById(100L))
                .thenReturn(Optional.of(task));

        when(memberService.requireMember(any(), any()))
                .thenReturn(Member.builder().build());

        when(tagRepository.findById(50L))
                .thenReturn(Optional.of(tag));

        when(userService.getById(10L))
                .thenReturn(user);

        Task result =
                taskService.attachTagToTask(10L, 100L, 50L);

        assertThat(result.getTags()).hasSize(1);
    }


    @Test
    void shouldDetachTagFromTask() {

        when(taskRepository.findById(100L))
                .thenReturn(Optional.of(task));

        when(memberService.requireMember(any(), any()))
                .thenReturn(Member.builder().build());

        when(userService.getById(10L))
                .thenReturn(user);

        Task result =
                taskService.detachTagFromTask(10L, 100L, 50L);

        assertThat(result.getTags()).isEmpty();
    }

    @Test
    void shouldFailAssignIfUserNotWorkspaceMember() {

        when(taskRepository.findById(100L))
                .thenReturn(Optional.of(task));

        doNothing().when(memberService)
                .requireOwner(any(), any());

        when(memberRepository.existsByWorkspaceIdAndUserId(1L, 20L))
                .thenReturn(false);

        assertThatThrownBy(() ->
                taskService.assignToTask(10L, 100L, 20L)
        ).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void shouldListTasksByProject() {

        when(projectRepository.findById(2L))
                .thenReturn(Optional.of(project));

        when(memberService.requireMember(any(), any()))
                .thenReturn(Member.builder().build());

        when(taskRepository.findByProjectId(2L))
                .thenReturn(List.of(task));

        List<TaskDto> list =
                taskService.listByProject(10L, 2L);

        assertThat(list).hasSize(1);
    }
}
