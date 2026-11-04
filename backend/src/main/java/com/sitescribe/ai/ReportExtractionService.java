package com.sitescribe.ai;

public interface ReportExtractionService {
    StructuredReportResult extract(String transcript, String outputLanguage);
}
