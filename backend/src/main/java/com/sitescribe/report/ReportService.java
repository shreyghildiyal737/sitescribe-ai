package com.sitescribe.report;

import com.sitescribe.ai.ReportExtractionService;
import com.sitescribe.ai.TranscriptionService;
import com.sitescribe.common.NotFoundException;
import com.sitescribe.notification.EmailService;
import com.sitescribe.project.ProjectRepository;
import com.sitescribe.upload.UploadService;
import com.sitescribe.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ReportService {

    private final ReportRepository reports;
    private final UploadService uploads;
    private final UserRepository users;
    private final ProjectRepository projects;
    private final TranscriptionService transcription;
    private final ReportExtractionService extraction;
    private final EmailService email;

    public ReportService(ReportRepository reports, UploadService uploads, UserRepository users,
                         ProjectRepository projects, TranscriptionService transcription,
                         ReportExtractionService extraction, EmailService email) {
        this.reports = reports;
        this.uploads = uploads;
        this.users = users;
        this.projects = projects;
        this.transcription = transcription;
        this.extraction = extraction;
        this.email = email;
    }

    @Transactional
    public Report fromUpload(CreateReportFromUploadRequest req, UUID userId, UUID companyId) {
        var upload = uploads.get(req.uploadId());
        if (!upload.getProject().getCompany().getId().equals(companyId))
            throw new NotFoundException("Upload not found");

        var user = users.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));

        var tx = transcription.transcribe(upload, req.manualTranscript());

        String requested = req.outputLanguage();
        String outputLanguage = (requested == null || requested.isBlank() || "auto".equalsIgnoreCase(requested))
            ? tx.language()
            : requested;

        var extracted = extraction.extract(tx.transcript(), outputLanguage);

        Report report = reports.save(new Report(
            upload.getProject(), user, upload,
            tx.transcript(), extracted.structuredJson(), extracted.finalReport(),
            tx.language(), outputLanguage,
            tx.lowConfidence() || extracted.lowConfidence()
        ));

        uploads.deleteAudioFile(upload);
        email.reportSubmitted(report);

        return report;
    }

    public List<Report> byProject(UUID projectId, UUID companyId) {
        requireProjectInCompany(projectId, companyId);
        return reports.findByProjectIdOrderByCreatedAtDesc(projectId);
    }

    public Report get(UUID id, UUID companyId) {
        var report = reports.findById(id)
            .orElseThrow(() -> new NotFoundException("Report not found"));
        if (!report.getProject().getCompany().getId().equals(companyId))
            throw new NotFoundException("Report not found");
        return report;
    }

    @Transactional
    public Report update(UUID id, UpdateReportRequest req, UUID companyId) {
        var report = get(id, companyId);
        report.updateFinalReport(req.finalReport());
        return report;
    }

    @Transactional
    public Report approve(UUID id, UUID approverId, UUID companyId) {
        var report = get(id, companyId);
        var approver = users.findById(approverId)
            .orElseThrow(() -> new NotFoundException("User not found"));
        report.approve(approver);
        return report;
    }

    private void requireProjectInCompany(UUID projectId, UUID companyId) {
        var project = projects.findById(projectId)
            .orElseThrow(() -> new NotFoundException("Project not found"));
        if (!project.getCompany().getId().equals(companyId))
            throw new NotFoundException("Project not found");
    }
}
