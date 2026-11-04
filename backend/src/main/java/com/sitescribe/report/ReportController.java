package com.sitescribe.report;

import com.sitescribe.auth.UserPrincipal;
import com.sitescribe.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @PostMapping("/from-upload")
    public ApiResponse<Report> fromUpload(@Valid @RequestBody CreateReportFromUploadRequest req,
                                          @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(service.fromUpload(req, principal.userId(), principal.companyId()));
    }

    @GetMapping("/project/{projectId}")
    public ApiResponse<List<Report>> byProject(@PathVariable UUID projectId,
                                               @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(service.byProject(projectId, principal.companyId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<Report> get(@PathVariable UUID id,
                                   @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(service.get(id, principal.companyId()));
    }

    @PatchMapping("/{id}")
    public ApiResponse<Report> update(@PathVariable UUID id,
                                      @Valid @RequestBody UpdateReportRequest req,
                                      @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(service.update(id, req, principal.companyId()));
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<Report> approve(@PathVariable UUID id,
                                       @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(service.approve(id, principal.userId(), principal.companyId()));
    }
}
