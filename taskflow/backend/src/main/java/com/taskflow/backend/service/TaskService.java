package com.taskflow.backend.service;

import com.taskflow.backend.common.NotFoundException;
import com.taskflow.backend.dto.task.TaskDto;
import com.taskflow.backend.entity.*;
import com.taskflow.backend.repository.*;
import com.taskflow.backend.security.AccessDeniedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

        private final TaskRepository taskRepository;
        private final ProjectRepository projectRepository;
        private final TagRepository tagRepository;
        private final UserRepository userRepository;
        private final MemberRepository memberRepository;

        private final UserService userService;
        private final MemberService memberService;
        private final ActivityLogService activityLogService;

        // =========================
        // CREATE TASK
        // =========================
        @Transactional
        public TaskDto create(
                        Long userId,
                        Long projectId,
                        String title,
                        String description,
                        Set<String> tagNames) {
                Project project = projectRepository.findById(projectId)
                                .orElseThrow(() -> new NotFoundException("Project not found id=" + projectId));

                Long workspaceId = project.getWorkspace().getId();

                // 🔐 Workspace member mı?
                memberService.requireMember(userId, workspaceId);

                // ✅ NULL SAFE TAG HANDLING
                Set<Tag> tags = (tagNames == null || tagNames.isEmpty())
                                ? Set.of()
                                : tagNames.stream()
                                                .map(t -> tagRepository.findByNameIgnoreCase(t.trim())
                                                                .orElseGet(() -> tagRepository.save(
                                                                                Tag.builder()
                                                                                                .name(t.trim())
                                                                                                .build())))
                                                .collect(Collectors.toSet());

                Task task = taskRepository.save(
                                Task.builder()
                                                .title(title)
                                                .description(description)
                                                .status("TODO")
                                                .project(project)
                                                .tags(tags)
                                                .createdAt(LocalDateTime.now())
                                                .build());

                activityLogService.logTask(
                                userService.getById(userId),
                                project.getWorkspace(),
                                task.getId(),
                                "TASK_CREATED",
                                "Task oluşturuldu: " + title);

                return toDto(task);
        }

        // =========================
        // UPDATE STATUS
        // =========================
        @Transactional
        public void updateStatus(Long userId, Long taskId, String status) {

                Task task = taskRepository.findById(taskId)
                                .orElseThrow(() -> new NotFoundException("Task not found id=" + taskId));

                Long workspaceId = task.getProject().getWorkspace().getId();
                memberService.requireMember(userId, workspaceId);

                task.setStatus(status);

                activityLogService.logTask(
                                userService.getById(userId),
                                task.getProject().getWorkspace(),
                                task.getId(),
                                "TASK_STATUS_UPDATED",
                                "Task durumu güncellendi: " + status);
        }

        // =========================
        // ASSIGN TASK (OWNER ONLY)
        // =========================
        @Transactional
        public Task assignToTask(Long actorId, Long taskId, Long assigneeId) {

                Task task = taskRepository.findById(taskId)
                                .orElseThrow(() -> new NotFoundException("Task not found id=" + taskId));

                Long workspaceId = task.getProject().getWorkspace().getId();

                // 🔐 SADECE OWNER
                memberService.requireOwner(actorId, workspaceId);

                boolean isMember = memberRepository.existsByWorkspaceIdAndUserId(workspaceId, assigneeId);

                if (!isMember) {
                        throw new AccessDeniedException("Assignee must be a workspace member");
                }

                User assignee = userRepository.findById(assigneeId)
                                .orElseThrow(() -> new NotFoundException("User not found id=" + assigneeId));

                task.setAssignee(assignee);

                activityLogService.logTask(
                                userService.getById(actorId),
                                task.getProject().getWorkspace(),
                                task.getId(),
                                "TASK_ASSIGNED",
                                "Task atandı: " + assignee.getEmail());

                return task;
        }

        // =========================
        // TAG ATTACH
        // =========================
        @Transactional
        public Task attachTagToTask(Long actorId, Long taskId, Long tagId) {

                Task task = taskRepository.findById(taskId)
                                .orElseThrow(() -> new NotFoundException("Task not found id=" + taskId));

                Long workspaceId = task.getProject().getWorkspace().getId();
                memberService.requireMember(actorId, workspaceId);

                Tag tag = tagRepository.findById(tagId)
                                .orElseThrow(() -> new NotFoundException("Tag not found id=" + tagId));

                task.getTags().add(tag);

                activityLogService.logTask(
                                userService.getById(actorId),
                                task.getProject().getWorkspace(),
                                task.getId(),
                                "TAG_ATTACHED",
                                "Tag eklendi: " + tag.getName());

                return task;
        }

        // =========================
        // TAG DETACH
        // =========================
        @Transactional
        public Task detachTagFromTask(Long actorId, Long taskId, Long tagId) {

                Task task = taskRepository.findById(taskId)
                                .orElseThrow(() -> new NotFoundException("Task not found id=" + taskId));

                Long workspaceId = task.getProject().getWorkspace().getId();
                memberService.requireMember(actorId, workspaceId);

                task.getTags().removeIf(t -> t.getId().equals(tagId));

                activityLogService.logTask(
                                userService.getById(actorId),
                                task.getProject().getWorkspace(),
                                task.getId(),
                                "TAG_DETACHED",
                                "Tag çıkarıldı");

                return task;
        }


        @Transactional(readOnly = true)
        public List<TaskDto> listByProject(Long userId, Long projectId) {

                Project project = projectRepository.findById(projectId)
                                .orElseThrow(() -> new NotFoundException("Project not found"));

                // 🔐 Workspace member kontrolü
                memberService.requireMember(
                                userId,
                                project.getWorkspace().getId());

                return taskRepository.findByProjectId(projectId)
                                .stream()
                                .map(this::toDto)
                                .toList();
        }

        // =========================
        // DTO MAPPER
        // =========================
        public TaskDto toDto(Task task) {
            return new TaskDto(
                    task.getId(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getStatus(),
                    task.getAssignee() != null ? task.getAssignee().getId() : null,
                    task.getTags()
                            .stream()
                            .map(Tag::getName)
                            .collect(Collectors.toSet())
            );
        }
}
