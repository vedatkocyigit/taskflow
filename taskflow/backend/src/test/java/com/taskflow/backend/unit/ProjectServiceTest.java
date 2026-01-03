package com.taskflow.backend.unit;

import com.taskflow.backend.entity.Project;
import com.taskflow.backend.entity.User;
import com.taskflow.backend.entity.Workspace;
import com.taskflow.backend.repository.ProjectRepository;
import com.taskflow.backend.repository.WorkspaceRepository;
import com.taskflow.backend.security.AuthorizationService;
import com.taskflow.backend.service.ActivityLogService;
import com.taskflow.backend.service.MemberService;
import com.taskflow.backend.service.ProjectService;
import com.taskflow.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {


    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private MemberService memberService;

    @Mock
    private UserService userService;

    @Mock
    private ActivityLogService activityLogService;

    @Mock
    private AuthorizationService authz;



    @InjectMocks
    private ProjectService projectService;



    private Workspace workspace;
    private User owner;
    private Project project;

    @BeforeEach
    void setup() {

        workspace = Workspace.builder()
                .id(1L)
                .name("Test Workspace")
                .createdAt(LocalDateTime.now())
                .build();

        owner = User.builder()
                .id(10L)
                .email("owner@test.com")
                .build();

        project = Project.builder()
                .id(100L)
                .name("Test Project")
                .workspace(workspace)
                .createdAt(LocalDateTime.now())
                .build();
    }



    @Test
    void shouldCreateProjectSuccessfully() {

        doNothing().when(memberService).requireOwner(10L, 1L);

        when(workspaceRepository.findById(1L))
                .thenReturn(Optional.of(workspace));

        when(userService.getById(10L))
                .thenReturn(owner);

        when(projectRepository.save(any(Project.class)))
                .thenAnswer(inv -> {
                    Project p = inv.getArgument(0);
                    p.setId(100L);
                    return p;
                });

        Project result =
                projectService.create(10L, 1L, "New Project");

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getName()).isEqualTo("New Project");
        assertThat(result.getWorkspace().getId()).isEqualTo(1L);

        verify(activityLogService).logProject(
                owner,
                workspace,
                100L,
                "PROJECT_CREATED",
                "Project oluşturuldu: New Project"
        );
    }

    @Test
    void shouldThrowIfWorkspaceNotFound() {

        doNothing().when(memberService).requireOwner(10L, 1L);

        when(workspaceRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                projectService.create(10L, 1L, "X")
        ).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Workspace not found");
    }



    @Test
    void shouldListProjectsForWorkspaceMember() {

        doNothing().when(authz).requireWorkspaceMember(1L);

        when(projectRepository.findByWorkspaceId(1L))
                .thenReturn(List.of(project));

        List<Project> result = projectService.list(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Project");
    }

    @Test
    void shouldFailIfNotWorkspaceMember() {

        doThrow(new RuntimeException("Not member"))
                .when(authz).requireWorkspaceMember(1L);

        assertThatThrownBy(() ->
                projectService.list(1L)
        ).isInstanceOf(RuntimeException.class);
    }
}
