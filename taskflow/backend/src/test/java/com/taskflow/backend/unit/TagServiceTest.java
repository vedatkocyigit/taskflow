package com.taskflow.backend.unit;

import com.taskflow.backend.common.NotFoundException;
import com.taskflow.backend.dto.tag.TagDto;
import com.taskflow.backend.entity.Tag;
import com.taskflow.backend.entity.User;
import com.taskflow.backend.entity.Workspace;
import com.taskflow.backend.repository.TagRepository;
import com.taskflow.backend.security.AuthorizationService;
import com.taskflow.backend.service.ActivityLogService;
import com.taskflow.backend.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TagServiceTest {



    @Mock
    private TagRepository tagRepository;

    @Mock
    private AuthorizationService authz;

    @Mock
    private ActivityLogService activityLogService;


    @InjectMocks
    private TagService tagService;



    private Workspace workspace;
    private User user;
    private Tag tag;

    @BeforeEach
    void setup() {

        workspace = Workspace.builder()
                .id(1L)
                .name("Test Workspace")
                .createdAt(LocalDateTime.now())
                .build();

        user = User.builder()
                .id(10L)
                .email("user@test.com")
                .build();

        tag = Tag.builder()
                .id(100L)
                .name("backend")
                .color("#ff0000")
                .workspace(workspace)
                .build();
    }



    @Test
    void shouldReturnExistingTagWhenAlreadyExists() {


        doNothing().when(authz).requireWorkspaceMember(1L);
        when(authz.requireWorkspace(1L)).thenReturn(workspace);


        when(tagRepository.findByWorkspaceIdAndNameIgnoreCase(1L, "backend"))
                .thenReturn(Optional.of(tag));


        Set<Tag> result =
                tagService.getOrCreateTags(1L, Set.of("backend"));


        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().getName()).isEqualTo("backend");


        verify(tagRepository, never()).save(any());
        verify(activityLogService, never()).log(any(), any(), any(), any());
    }

    @Test
    void shouldCreateTagIfNotExists() {

        doNothing().when(authz).requireWorkspaceMember(1L);
        when(authz.requireWorkspace(1L)).thenReturn(workspace);
        when(authz.currentUser()).thenReturn(user);

        when(tagRepository.findByWorkspaceIdAndNameIgnoreCase(1L, "frontend"))
                .thenReturn(Optional.empty());

        when(tagRepository.save(any(Tag.class)))
                .thenAnswer(inv -> {
                    Tag t = inv.getArgument(0);
                    t.setId(200L);
                    return t;
                });

        Set<Tag> result =
                tagService.getOrCreateTags(1L, Set.of("frontend"));

        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().getName()).isEqualTo("frontend");

        verify(activityLogService).log(
                user,
                workspace,
                "TAG_CREATED",
                "Tag oluşturuldu: frontend"
        );
    }

    @Test
    void shouldReturnEmptySetWhenTagNamesEmpty() {

        doNothing().when(authz).requireWorkspaceMember(1L);

        Set<Tag> result = tagService.getOrCreateTags(1L, Set.of());

        assertThat(result).isEmpty();
    }


    @Test
    void shouldCreateTagManually() {

        doNothing().when(authz).requireWorkspaceMember(1L);
        when(authz.requireWorkspace(1L)).thenReturn(workspace);
        when(authz.currentUser()).thenReturn(user);

        when(tagRepository.existsByWorkspaceIdAndNameIgnoreCase(1L, "devops"))
                .thenReturn(false);

        when(tagRepository.save(any(Tag.class)))
                .thenAnswer(inv -> {
                    Tag t = inv.getArgument(0);
                    t.setId(300L);
                    return t;
                });

        TagDto dto = tagService.create(1L, "devops", "#00ff00");

        assertThat(dto.id()).isEqualTo(300L);
        assertThat(dto.name()).isEqualTo("devops");
        assertThat(dto.color()).isEqualTo("#00ff00");

        verify(activityLogService).log(
                user,
                workspace,
                "TAG_CREATED",
                "Tag oluşturuldu: devops"
        );
    }

    @Test
    void shouldFailIfTagAlreadyExistsOnCreate() {

        doNothing().when(authz).requireWorkspaceMember(1L);

        when(tagRepository.existsByWorkspaceIdAndNameIgnoreCase(1L, "backend"))
                .thenReturn(true);

        assertThatThrownBy(() ->
                tagService.create(1L, "backend", null)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tag already exists");
    }


    @Test
    void shouldListAllTagsSorted() {

        doNothing().when(authz).requireWorkspaceMember(1L);

        Tag t1 = Tag.builder().id(1L).name("api").workspace(workspace).build();
        Tag t2 = Tag.builder().id(2L).name("backend").workspace(workspace).build();

        when(tagRepository.findByWorkspaceId(1L))
                .thenReturn(List.of(t2, t1));

        List<TagDto> result = tagService.listAll(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("api");
        assertThat(result.get(1).name()).isEqualTo("backend");
    }


    @Test
    void shouldReturnTagById() {

        when(tagRepository.findById(100L))
                .thenReturn(Optional.of(tag));

        Tag result = tagService.getById(100L);

        assertThat(result.getName()).isEqualTo("backend");
    }

    @Test
    void shouldThrowIfTagNotFound() {

        when(tagRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                tagService.getById(999L)
        ).isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Tag not found");
    }
}
