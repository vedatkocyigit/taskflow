package com.taskflow.backend.controller;

import com.taskflow.backend.dto.user.UserDto;
import com.taskflow.backend.entity.User;
import com.taskflow.backend.security.AppUserPrincipal;
import com.taskflow.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 🔹 KENDİ PROFİLİM
    @GetMapping("/me")
    public UserDto me(@AuthenticationPrincipal AppUserPrincipal principal) {

        User user = userService.getById(principal.getId());

        Set<String> roles = user.getRoles()
                .stream()
                .map(r -> r.getName())
                .collect(java.util.stream.Collectors.toSet());

        return new UserDto(
                user.getId(),
                user.getEmail(),
                roles
        );
    }

    @GetMapping("/search")
    public UserDto findByEmail(
            @AuthenticationPrincipal AppUserPrincipal principal,
            @RequestParam String email,
            @RequestParam Long workspaceId
    ) {
        // OWNER kontrolü (var olan yapıdan)
        // memberService.requireOwner(principal.getId(), workspaceId);

        User user = userService.getByEmail(email);

        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getRoles().stream()
                        .map(r -> r.getName())
                        .collect(java.util.stream.Collectors.toSet())
        );
    }

}
