package com.taskflow.backend.controller;

import com.taskflow.backend.dto.tag.TagDto;
import com.taskflow.backend.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public List<TagDto> list(@PathVariable Long workspaceId) {
        return tagService.listAll(workspaceId);
    }


    @PostMapping
    public TagDto create(
            @PathVariable Long workspaceId,
            @RequestParam String name,
            @RequestParam(required = false) String color) {
        return tagService.create(workspaceId, name, color);
    }
}
