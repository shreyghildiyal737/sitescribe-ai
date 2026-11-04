package com.sitescribe.export;

import com.sitescribe.report.Report;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class PdfExportService {

    private static final DateTimeFormatter DATE_FMT =
        DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm").withZone(ZoneId.of("Europe/Dublin"));

    public byte[] simpleTextExport(Report report) {
        String header = "SiteScribe AI — Daily Site Report\n"
            + "Project: " + report.getProject().getName() + "\n"
            + "Date: " + DATE_FMT.format(report.getCreatedAt()) + "\n"
            + "Status: " + report.getStatus().name() + "\n"
            + "Language: " + report.getOutputLanguage() + "\n"
            + "─".repeat(60) + "\n\n";

        return (header + report.getFinalReport()).getBytes(StandardCharsets.UTF_8);
    }
}
