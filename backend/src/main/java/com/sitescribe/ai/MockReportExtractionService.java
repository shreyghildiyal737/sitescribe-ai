package com.sitescribe.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@ConditionalOnProperty(name = "sitescribe.ai.mode", havingValue = "mock", matchIfMissing = true)
public class MockReportExtractionService implements ReportExtractionService {

    @Override
    public StructuredReportResult extract(String transcript, String outputLanguage) {
        String t = transcript == null ? "" : transcript.trim();
        boolean low = t.length() < 25 || t.toLowerCase().contains("unclear");
        List<String> progress = containsAny(t, "completed", "finished", "done", "críochnaíodh") ? List.of(sentence(t)) : List.of();
        List<String> delays = containsAny(t, "delay", "delayed", "waiting", "moill") ? List.of(sentence(t)) : List.of();
        List<String> incidents = containsAny(t, "incident", "accident", "injury", "timpiste")
            && !t.toLowerCase().contains("no incident") && !t.toLowerCase().contains("no accidents")
            ? List.of(sentence(t)) : List.of("No incidents reported");
        String json = "{\"labour\":[\"Extracted from transcript where stated\"],\"progress\":" + arr(progress)
            + ",\"delays\":" + arr(delays) + ",\"issues\":[],\"incidents\":" + arr(incidents)
            + ",\"materials\":[],\"nextActions\":[\"Manager review required before use\"],\"confidence\":{\"lowConfidence\":" + low + "}}";
        String report = formatReport(t, outputLanguage, low);
        return new StructuredReportResult(json, report, low);
    }

    private boolean containsAny(String text, String... words) {
        String l = text.toLowerCase();
        for (String w : words) if (l.contains(w.toLowerCase())) return true;
        return false;
    }

    private String sentence(String t) { return t.length() > 500 ? t.substring(0, 500) : t; }

    private String arr(List<String> values) {
        return "[" + values.stream().map(v -> "\"" + v.replace("\"", "'") + "\"")
            .reduce((a, b) -> a + "," + b).orElse("") + "]";
    }

    private String formatReport(String t, String lang, boolean low) {
        String title = switch (lang == null ? "en" : lang) {
            case "ga" -> "Tuarascáil Laethúil Suímh";
            case "es" -> "Informe Diario de Obra";
            case "uk" -> "Щоденний звіт про будівельний майданчик";
            default -> "Daily Site Report";
        };
        return title + "\n\nSource Summary:\n" + t
            + "\n\nImportant: AI draft only. Manager review required before sharing or filing."
            + (low ? "\n\nWarning: Low-confidence extraction. Confirm details manually." : "");
    }
}
