package com.sitescribe.export;

import com.sitescribe.auth.UserPrincipal;
import com.sitescribe.report.ReportService;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/exports")
public class ExportController {

    private final ReportService reports;
    private final PdfExportService export;

    public ExportController(ReportService reports, PdfExportService export) {
        this.reports = reports;
        this.export = export;
    }

    @GetMapping("/reports/{id}/text")
    public ResponseEntity<byte[]> downloadText(@PathVariable UUID id,
                                               @AuthenticationPrincipal UserPrincipal principal) {
        // Company-scoped lookup — a different company gets 404, same as every other read.
        var report = reports.get(id, principal.companyId());
        byte[] data = export.simpleTextExport(report);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=site-report-" + id + ".txt")
            .contentType(MediaType.TEXT_PLAIN)
            .body(data);
    }
}
