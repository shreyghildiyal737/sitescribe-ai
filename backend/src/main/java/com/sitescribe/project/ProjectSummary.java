package com.sitescribe.project;

import java.util.UUID;

public record ProjectSummary(
    UUID id,
    String name,
    String address,
    String roleOnProject,
    long activeWorkers,
    long pendingReports
) {}
