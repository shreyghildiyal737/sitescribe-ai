package com.sitescribe.export;

import com.sitescribe.auth.UserPrincipal;
import com.sitescribe.common.ConflictException;
import com.sitescribe.report.Report;
import com.sitescribe.report.ReportService;
import com.sitescribe.report.ReportStatus;
import com.sitescribe.user.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ExportControllerTest {

    private final ReportService reports = mock(ReportService.class);
    private final PdfExportService export = mock(PdfExportService.class);
    private final ExportController controller = new ExportController(reports, export);

    private final UUID reportId = UUID.randomUUID();
    private final UserPrincipal principal =
        new UserPrincipal(UUID.randomUUID(), UUID.randomUUID(), UserRole.MANAGER, "manager@example.com");

    @ParameterizedTest
    @EnumSource(value = ReportStatus.class, names = {"DRAFT", "NEEDS_REVIEW"})
    void refusesToExportAReportNobodyHasApproved(ReportStatus status) {
        givenReportIn(status);

        assertThatThrownBy(() -> controller.downloadText(reportId, principal))
            .isInstanceOf(ConflictException.class);
        verify(export, never()).simpleTextExport(any());
    }

    @Test
    void exportsAnApprovedReport() {
        givenReportIn(ReportStatus.APPROVED);
        when(export.simpleTextExport(any())).thenReturn("report".getBytes());

        var response = controller.downloadText(reportId, principal);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("report".getBytes());
    }

    private void givenReportIn(ReportStatus status) {
        Report report = mock(Report.class);
        when(report.getStatus()).thenReturn(status);
        when(reports.get(reportId, principal.companyId())).thenReturn(report);
    }
}
