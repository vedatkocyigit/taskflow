package com.taskflow.backend.unit;

import com.taskflow.backend.dto.activity.ActivityLogDto;
import com.taskflow.backend.entity.ActivityLog;
import com.taskflow.backend.entity.User;
import com.taskflow.backend.entity.Workspace;
import com.taskflow.backend.repository.ActivityLogRepository;
import com.taskflow.backend.service.ActivityLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityLogServiceTest {

    @Mock
    private ActivityLogRepository activityLogRepository;


    @InjectMocks
    private ActivityLogService activityLogService;

    private User actor;
    private Workspace workspace;


    @BeforeEach
    void setup() {
        actor = User.builder()
                .id(1L)
                .email("test@taskflow.com")
                .build();

        workspace = Workspace.builder()
                .id(10L)
                .name("Test Workspace")
                .build();
    }

    @Test
    void shouldLogBasicActivity() {

        activityLogService.log(
                actor,
                workspace,
                "WORKSPACE_CREATED",
                "Workspace oluşturuldu"
        );

        ArgumentCaptor<ActivityLog> captor =
                ArgumentCaptor.forClass(ActivityLog.class);

        verify(activityLogRepository).save(captor.capture());

        ActivityLog saved = captor.getValue();

        assertThat(saved.getActor()).isEqualTo(actor);
        assertThat(saved.getWorkspace()).isEqualTo(workspace);
        assertThat(saved.getAction()).isEqualTo("WORKSPACE_CREATED");
        assertThat(saved.getDescription()).isEqualTo("Workspace oluşturuldu");
        assertThat(saved.getCreatedAt()).isNotNull();

        assertThat(saved.getTaskId()).isNull();
        assertThat(saved.getProjectId()).isNull();
    }

    @Test
    void shouldLogTaskAndProjectActivity() {

        activityLogService.log(
                actor,
                workspace,
                "TASK_UPDATED",
                100L,
                200L,
                "Task güncellendi"
        );

        ArgumentCaptor<ActivityLog> captor =
                ArgumentCaptor.forClass(ActivityLog.class);

        verify(activityLogRepository).save(captor.capture());

        ActivityLog saved = captor.getValue();

        assertThat(saved.getTaskId()).isEqualTo(100L);
        assertThat(saved.getProjectId()).isEqualTo(200L);
        assertThat(saved.getAction()).isEqualTo("TASK_UPDATED");
    }


    @Test
    void shouldLogTaskShortcut() {

        activityLogService.logTask(
                actor,
                workspace,
                55L,
                "COMMENT_ADDED",
                "Task'a yorum eklendi"
        );

        ArgumentCaptor<ActivityLog> captor =
                ArgumentCaptor.forClass(ActivityLog.class);

        verify(activityLogRepository).save(captor.capture());

        ActivityLog saved = captor.getValue();

        assertThat(saved.getTaskId()).isEqualTo(55L);
        assertThat(saved.getProjectId()).isNull();
    }





    @Test
    void shouldListActivityLogsByWorkspace() {

        ActivityLog log1 = ActivityLog.builder()
                .actor(actor)
                .workspace(workspace)
                .action("A1")
                .description("Desc1")
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .build();

        ActivityLog log2 = ActivityLog.builder()
                .actor(actor)
                .workspace(workspace)
                .action("A2")
                .description("Desc2")
                .createdAt(LocalDateTime.now())
                .build();

        when(activityLogRepository
                .findByWorkspaceIdOrderByCreatedAtDesc(10L))
                .thenReturn(List.of(log2, log1));

        List<ActivityLogDto> result =
                activityLogService.listByWorkspace(10L);

        assertThat(result).hasSize(2);

        ActivityLogDto first = result.get(0);
        assertThat(first.actorEmail()).isEqualTo("test@taskflow.com");
        assertThat(first.action()).isEqualTo("A3");
        assertThat(first.description()).isEqualTo("Desc2");
    }
}
