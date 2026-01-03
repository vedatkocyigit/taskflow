package com.taskflow.backend.repository;

import com.taskflow.backend.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByUserIdAndWorkspaceId(Long userId, Long workspaceId);

    List<Member> findByWorkspaceId(Long workspaceId);

    boolean existsByUserIdAndWorkspaceId(Long userId, Long workspaceId);

    List<Member> findByUserId(Long userId);

    boolean existsByWorkspaceIdAndUserId(Long workspaceId, Long userId);

    Optional<Member> findByWorkspaceIdAndUserId(Long workspaceId, Long userId);

    List<Member> findAllByWorkspaceId(Long workspaceId);

}
