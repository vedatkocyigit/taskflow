package com.taskflow.backend.unit;

import com.taskflow.backend.dto.workspace.WorkspaceListDto;
import com.taskflow.backend.entity.Member;
import com.taskflow.backend.entity.User;
import com.taskflow.backend.entity.Workspace;
import com.taskflow.backend.repository.MemberRepository;
import com.taskflow.backend.repository.WorkspaceRepository;
import com.taskflow.backend.service.ActivityLogService;
import com.taskflow.backend.service.UserService;
import com.taskflow.backend.service.WorkspaceService;
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
import static org.mockito.Mockito.*;

import com.taskflow.backend.service.WorkspaceService;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceTest {



    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private UserService userService;

    @Mock
    private ActivityLogService activityLogService;


    @InjectMocks
    private WorkspaceService workspaceService;

    private User user;
    private Workspace workspace;

    @BeforeEach
    void setup() {
        user = User.builder()
                .id(1L)
                .email("owner@taskflow.com")
                .build();

        workspace = Workspace.builder()
                .id(10L)
                .name("My Workspace")
                .owner(user)
                .createdAt(LocalDateTime.now())
                .build();
    }


    @Test
    void shouldCreateWorkspaceSuccessfully() {

        when(userService.getById(1L))
                .thenReturn(user);

        when(workspaceRepository.save(any(Workspace.class)))
                .thenAnswer(inv -> {
                    Workspace ws = inv.getArgument(0);
                    ws.setId(10L);
                    return ws;
                });

        when(memberRepository.save(any(Member.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Workspace result =
                workspaceService.create(1L, "My Workspace");

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("My Workspace");
        assertThat(result.getOwner()).isEqualTo(user);

        verify(workspaceRepository).save(any(Workspace.class));

        verify(memberRepository).save(argThat(m ->
                m.getUser().equals(user) &&
                        m.getRole().equals("OWNER")
        ));

        verify(activityLogService).log(
                user,
                result,
                "WORKSPACE_CREATED",
                "Workspace oluşturuldu"
        );
    }


    @Test
    void shouldListMyWorkspaces() {

        Member member = Member.builder()
                .id(100L)
                .user(user)
                .workspace(workspace)
                .role("OWNER")
                .build();

        when(memberRepository.findByUserId(1L))
                .thenReturn(List.of(member));

        List<WorkspaceListDto> result =
                workspaceService.listMyWorkspaces(1L);

        assertThat(result).hasSize(1);

        WorkspaceListDto dto = result.get(0);
        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.name()).isEqualTo("My Workspace");
        assertThat(dto.role()).isEqualTo("OWNER");
    }


    @Test
    void shouldGetWorkspaceById() {

        when(workspaceRepository.findById(10L))
                .thenReturn(Optional.of(workspace));

        Workspace result =
                workspaceService.getById(10L);

        assertThat(result.getName()).isEqualTo("My Workspaces");
    }

    @Test
    void shouldThrowWhenWorkspaceNotFound() {

        when(workspaceRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                workspaceService.getById(99L)
        ).isInstanceOf(RuntimeException.class)
                .hasMessage("Workspace not found");
    }
}
