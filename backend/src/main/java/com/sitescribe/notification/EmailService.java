package com.sitescribe.notification;

import com.sitescribe.report.Report;

public interface EmailService {
    void reportSubmitted(Report report);
}
