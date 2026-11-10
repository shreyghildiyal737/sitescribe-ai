package com.sitescribe.export;

import com.sitescribe.auth.UserPrincipal;
import com.sitescribe.common.ConflictException;
import com.sitescribe.report.ReportService;
import com.sitescribe.report.ReportStatus;
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
        // The product rule is that AI only drafts: nothing leaves the system until a manager has approved it.
        if (report.getStatus() != ReportStatus.APPROVED && report.getStatus() != ReportStatus.EXPORTED) {
            throw new ConflictException("Approve the report before exporting it.");
        }
        byte[] data = export.simpleTextExport(report);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=site-report-" + id + ".txt")
            .contentType(MediaType.TEXT_PLAIN)
            .body(data);
    }
}
