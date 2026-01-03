package com.taskflow.backend.unit;

import com.taskflow.backend.common.NotFoundException;
import com.taskflow.backend.dto.workspace.WorkspaceMemberDto;
import com.taskflow.backend.entity.Member;
import com.taskflow.backend.entity.User;
import com.taskflow.backend.entity.Workspace;
import com.taskflow.backend.repository.MemberRepository;
import com.taskflow.backend.repository.UserRepository;
import com.taskflow.backend.security.AccessDeniedException;
import com.taskflow.backend.security.AuthorizationService;
import com.taskflow.backend.service.ActivityLogService;
import com.taskflow.backend.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;


import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthorizationService authz;

    @Mock
    private ActivityLogService activityLogService;



    @InjectMocks
    private MemberService memberService;



    private Workspace workspace;
    private User owner;
    private User memberUser;
    private Member member;

    @BeforeEach
    void setup() {

        workspace = Workspace.builder()
                .id(1L)
                .name("Test Workspace")
                .build();

        owner = User.builder()
                .id(10L)
                .email("owner@test.com")
                .build();

        memberUser = User.builder()
                .id(20L)
                .email("member@test.com")
                .build();

        member = Member.builder()
                .id(100L)
                .workspace(workspace)
                .user(memberUser)
                .role("MEMBER")
                .build();
    }


    @Test
    void shouldAddMemberSuccessfully() {

        doNothing().when(authz).requireWorkspaceOwner(1L);
        when(authz.requireWorkspace(1L)).thenReturn(workspace);
        when(authz.currentUser()).thenReturn(owner);

        when(userRepository.findByEmail("member@test.com"))
                .thenReturn(Optional.of(memberUser));

        when(memberRepository.existsByWorkspaceIdAndUserId(1L, 20L))
                .thenReturn(false);

        when(memberRepository.save(any(Member.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Member result = memberService.addMember(
                1L,
                "member@test.com",
                null
        );

        assertThat(result.getRole()).isEqualTo("MEMBER");
        assertThat(result.getUser().getEmail()).isEqualTo("member@test.com");

        verify(activityLogService).log(
                owner,
                workspace,
                "MEMBER_ADDED",
                "member@test.com workspace'e eklendi"
        );
    }

    @Test
    void shouldThrowIfUserNotFound() {

        doNothing().when(authz).requireWorkspaceOwner(1L);
        when(authz.requireWorkspace(1L)).thenReturn(workspace);

        when(userRepository.findByEmail("x@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                memberService.addMember(1L, "x@test.com", "MEMBER")
        ).isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldThrowIfAlreadyMember() {

        doNothing().when(authz).requireWorkspaceOwner(1L);
        when(authz.requireWorkspace(1L)).thenReturn(workspace);

        when(userRepository.findByEmail("member@test.com"))
                .thenReturn(Optional.of(memberUser));

        when(memberRepository.existsByWorkspaceIdAndUserId(1L, 20L))
                .thenReturn(true);

        assertThatThrownBy(() ->
                memberService.addMember(1L, "member@test.com", "MEMBER")
        ).isInstanceOf(IllegalArgumentException.class);
    }



    @Test
    void shouldListMembers() {

        doNothing().when(authz).requireWorkspaceMember(1L);

        when(memberRepository.findAllByWorkspaceId(1L))
                .thenReturn(List.of(member));

        List<WorkspaceMemberDto> result =
                memberService.listMembers(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).email()).isEqualTo("member@test.com");
        assertThat(result.get(0).role()).isEqualTo("MEMBER");
    }



    @Test
    void shouldRemoveMemberSuccessfully() {

        doNothing().when(authz).requireWorkspaceOwner(1L);
        when(authz.currentUser()).thenReturn(owner);

        when(memberRepository.findById(100L))
                .thenReturn(Optional.of(member));

        memberService.removeMember(1L, 100L);

        verify(memberRepository).delete(member);
        verify(activityLogService).log(
                owner,
                workspace,
                "MEMBER_REMOVED",
                "member@test.com workspace'den çıkarıldı"
        );
    }

    @Test
    void shouldThrowIfMemberNotInWorkspace() {

        doNothing().when(authz).requireWorkspaceOwner(1L);

        Workspace otherWs = Workspace.builder().id(99L).build();
        member.setWorkspace(otherWs);

        when(memberRepository.findById(100L))
                .thenReturn(Optional.of(member));

        assertThatThrownBy(() ->
                memberService.removeMember(1L, 100L)
        ).isInstanceOf(AccessDeniedException.class);
    }
}
