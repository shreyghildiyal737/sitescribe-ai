package com.sitescribe.report;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateReportFromUploadRequest(
    @NotNull UUID uploadId,
    String manualTranscript,
    String outputLanguage
) {}
