package com.sitescribe.upload;

import com.sitescribe.auth.UserPrincipal;
import com.sitescribe.common.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/uploads")
public class UploadController {

    private final UploadService service;

    public UploadController(UploadService service) {
        this.service = service;
    }

    @PostMapping("/audio")
    public ApiResponse<Upload> audio(
        @PathVariable UUID projectId,
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam MultipartFile file,
        @RequestParam(required = false) BigDecimal latitude,
        @RequestParam(required = false) BigDecimal longitude,
        @RequestParam(required = false) BigDecimal accuracyMeters
    ) {
        return ApiResponse.ok(service.store(projectId, principal.companyId(), principal.userId(), UploadType.AUDIO,
            file, latitude, longitude, accuracyMeters));
    }

    @PostMapping("/photos")
    public ApiResponse<Upload> photo(
        @PathVariable UUID projectId,
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam MultipartFile file,
        @RequestParam(required = false) BigDecimal latitude,
        @RequestParam(required = false) BigDecimal longitude,
        @RequestParam(required = false) BigDecimal accuracyMeters
    ) {
        return ApiResponse.ok(service.store(projectId, principal.companyId(), principal.userId(), UploadType.PHOTO,
            file, latitude, longitude, accuracyMeters));
    }
}
