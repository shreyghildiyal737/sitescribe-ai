package com.sitescribe.report;

import com.sitescribe.project.Project;
import com.sitescribe.upload.Upload;
import com.sitescribe.user.AppUser;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    private UUID id = UUID.randomUUID();

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by")
    private AppUser createdBy;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private AppUser approvedBy;

    @ManyToOne
    @JoinColumn(name = "source_upload_id")
    private Upload sourceUpload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.DRAFT;

    @Column(columnDefinition = "TEXT")
    private String rawTranscript;

    @Column(columnDefinition = "TEXT")
    private String structuredJson;

    @Column(columnDefinition = "TEXT")
    private String finalReport;

    private String inputLanguage;

    @Column(nullable = false)
    private String outputLanguage = "en";

    @Column(nullable = false)
    private boolean lowConfidence = false;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant approvedAt;

    protected Report() {}

    public Report(Project project, AppUser createdBy, Upload sourceUpload,
                  String rawTranscript, String structuredJson, String finalReport,
                  String inputLanguage, String outputLanguage, boolean lowConfidence) {
        this.project = project;
        this.createdBy = createdBy;
        this.sourceUpload = sourceUpload;
        this.rawTranscript = rawTranscript;
        this.structuredJson = structuredJson;
        this.finalReport = finalReport;
        this.inputLanguage = inputLanguage;
        this.outputLanguage = outputLanguage;
        this.lowConfidence = lowConfidence;
        this.status = lowConfidence ? ReportStatus.NEEDS_REVIEW : ReportStatus.DRAFT;
    }

    public UUID getId() { return id; }
    public Project getProject() { return project; }
    public AppUser getCreatedBy() { return createdBy; }
    public ReportStatus getStatus() { return status; }
    public String getRawTranscript() { return rawTranscript; }
    public String getStructuredJson() { return structuredJson; }
    public String getFinalReport() { return finalReport; }
    public boolean isLowConfidence() { return lowConfidence; }
    public String getOutputLanguage() { return outputLanguage; }
    public Instant getCreatedAt() { return createdAt; }

    public void updateFinalReport(String text) {
        this.finalReport = text;
        this.status = ReportStatus.NEEDS_REVIEW;
    }

    public void approve(AppUser approver) {
        this.approvedBy = approver;
        this.approvedAt = Instant.now();
        this.status = ReportStatus.APPROVED;
    }
}
