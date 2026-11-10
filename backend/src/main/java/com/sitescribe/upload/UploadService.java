package com.sitescribe.upload;

import com.sitescribe.common.NotFoundException;
import com.sitescribe.project.ProjectRepository;
import com.sitescribe.storage.StorageService;
import com.sitescribe.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

@Service
public class UploadService {

    private static final Logger log = LoggerFactory.getLogger(UploadService.class);

    private final UploadRepository uploads;
    private final ProjectRepository projects;
    private final UserRepository users;
    private final StorageService storage;

    public UploadService(UploadRepository uploads, ProjectRepository projects,
                         UserRepository users, StorageService storage) {
        this.uploads = uploads;
        this.projects = projects;
        this.users = users;
        this.storage = storage;
    }

    public Upload store(UUID projectId, UUID companyId, UUID userId, UploadType type, MultipartFile file,
                        BigDecimal latitude, BigDecimal longitude, BigDecimal accuracyMeters) {
        if (file.isEmpty()) throw new IllegalArgumentException("File is empty");

        var project = projects.findById(projectId)
            .orElseThrow(() -> new NotFoundException("Project not found"));
        // Prevent cross-tenant writes: the project must belong to the caller's company.
        if (!project.getCompany().getId().equals(companyId))
            throw new NotFoundException("Project not found");
        var user = users.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));

        try {
            String reference = storage.store(
                file.getOriginalFilename(), file.getContentType(), file.getBytes());
            return uploads.save(new Upload(project, user, type, file.getOriginalFilename(),
                reference, file.getContentType(), latitude, longitude, accuracyMeters));
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not store upload");
        }
    }

    public Upload get(UUID id) {
        return uploads.findById(id)
            .orElseThrow(() -> new NotFoundException("Upload not found"));
    }

    public void deleteAudioFile(Upload upload) {
        if (upload.getFilePath() == null) return;
        try {
            storage.delete(upload.getFilePath());
            upload.clearFilePath();
        } catch (IOException | RuntimeException e) {
            // Keep the path so the leftover file can still be found and removed; clearing it
            // would orphan audio the privacy page says is gone. Not rethrown, because the
            // report has already been saved and the user should not see it fail.
            log.warn("Could not delete audio for upload {}", upload.getId(), e);
        }
    }
}
