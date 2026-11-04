package com.sitescribe.upload;

import com.sitescribe.project.Project;
import com.sitescribe.user.AppUser;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "uploads")
public class Upload {

    @Id
    private UUID id = UUID.randomUUID();

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(optional = false)
    @JoinColumn(name = "uploaded_by")
    private AppUser uploadedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "upload_type", nullable = false)
    private UploadType type;

    @Column(nullable = false)
    private String fileName;

    // Nulled after the audio file is deleted post-transcription
    private String filePath;

    private String contentType;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal accuracyMeters;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Upload() {}

    public Upload(Project project, AppUser uploadedBy, UploadType type,
                  String fileName, String filePath, String contentType,
                  BigDecimal latitude, BigDecimal longitude, BigDecimal accuracyMeters) {
        this.project = project;
        this.uploadedBy = uploadedBy;
        this.type = type;
        this.fileName = fileName;
        this.filePath = filePath;
        this.contentType = contentType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracyMeters = accuracyMeters;
    }

    public UUID getId() { return id; }
    public Project getProject() { return project; }
    public AppUser getUploadedBy() { return uploadedBy; }
    public UploadType getType() { return type; }
    public String getFileName() { return fileName; }
    public String getFilePath() { return filePath; }
    public String getContentType() { return contentType; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public BigDecimal getAccuracyMeters() { return accuracyMeters; }
    public Instant getCreatedAt() { return createdAt; }

    public void clearFilePath() { this.filePath = null; }
}
