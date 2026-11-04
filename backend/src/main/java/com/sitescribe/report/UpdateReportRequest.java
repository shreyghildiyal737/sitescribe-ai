package com.sitescribe.report;
import jakarta.validation.constraints.NotBlank;
public record UpdateReportRequest(@NotBlank String finalReport){}
