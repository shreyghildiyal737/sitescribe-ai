package com.sitescribe.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sitescribe.project.ProjectAssignmentRepository;
import com.sitescribe.report.Report;
import com.sitescribe.user.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "sitescribe.email.api-key")
public class ResendEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailService.class);

    private final String apiKey;
    private final String from;
    private final String appUrl;
    private final ProjectAssignmentRepository assignments;
    private final HttpClient http;
    private final ObjectMapper mapper;

    public ResendEmailService(
        @Value("${sitescribe.email.api-key}") String apiKey,
        @Value("${sitescribe.email.from:noreply@sitescribe.ai}") String from,
        @Value("${sitescribe.email.app-url:http://localhost:8091}") String appUrl,
        ProjectAssignmentRepository assignments
    ) {
        this.apiKey = apiKey;
        this.from = from;
        this.appUrl = appUrl.replaceAll("/$", "");
        this.assignments = assignments;
        this.http = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    @Override
    public void reportSubmitted(Report report) {
        var projectId = report.getProject().getId();
        var projectName = report.getProject().getName();
        var reportUrl = appUrl + "/reports/" + report.getId();

        var supervisors = assignments.findByProjectIdAndActiveTrue(projectId).stream()
            .filter(a -> {
                var role = a.getUser().getRole();
                return role == UserRole.OWNER || role == UserRole.MANAGER;
            })
            .map(a -> a.getUser().getEmail())
            .filter(email -> !email.equals(report.getCreatedBy().getEmail()))
            .toList();

        if (supervisors.isEmpty()) return;

        String subject = "New report submitted — " + projectName;
        String html = """
            <p>A new site report has been submitted for <strong>%s</strong>.</p>
            <p>Submitted by: %s</p>
            <p><a href="%s">Review the report</a></p>
            <p style="color:#888;font-size:12px">SiteScribe AI — Daily Site Reporting</p>
            """.formatted(projectName, report.getCreatedBy().getName(), reportUrl);

        supervisors.forEach(to -> sendEmail(to, subject, html));
    }

    private void sendEmail(String to, String subject, String html) {
        try {
            String body = mapper.writeValueAsString(Map.of(
                "from", from,
                "to", List.of(to),
                "subject", subject,
                "html", html
            ));

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.resend.com/emails"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200 && response.statusCode() != 201)
                log.warn("Resend email to {} failed: HTTP {}", to, response.statusCode());

        } catch (Exception e) {
            log.warn("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
