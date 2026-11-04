package com.sitescribe.notification;

import com.sitescribe.report.Report;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnMissingBean(EmailService.class)
public class NoOpEmailService implements EmailService {

    @Override
    public void reportSubmitted(Report report) {
        // No email configured — silently skip
    }
}
