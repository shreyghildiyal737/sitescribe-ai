package com.sitescribe.project;

import com.sitescribe.auth.UserPrincipal;
import com.sitescribe.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @PostMapping
    public ApiResponse<Project> create(@Valid @RequestBody CreateProjectRequest req,
                                       @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(service.create(req, principal));
    }

    @GetMapping("/my")
    public ApiResponse<List<ProjectSummary>> myProjects(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(service.myProjects(principal.userId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<Project> get(@PathVariable UUID id,
                                    @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(service.get(id, principal));
    }

    @GetMapping("/{id}/members")
    public ApiResponse<List<ProjectMember>> members(@PathVariable UUID id,
                                                    @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(service.getMembers(id, principal));
    }

    @PostMapping("/{id}/assignments")
    public ApiResponse<ProjectAssignment> invite(@PathVariable UUID id,
                                                 @Valid @RequestBody InviteUserRequest req,
                                                 @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(service.invite(id, req, principal));
    }
}
