package com.sitescribe.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sitescribe.config.AiProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
@ConditionalOnProperty(name = "sitescribe.ai.mode", havingValue = "live")
public class GeminiExtractionService implements ReportExtractionService {

    // gemini-2.0-flash now has a zero free-tier quota; gemini-flash-latest is the
    // current free-tier-available alias. Override with sitescribe.ai (future) if needed.
    private static final String GEMINI_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=";

    private final AiProperties props;
    private final HttpClient http;
    private final ObjectMapper mapper;

    public GeminiExtractionService(AiProperties props) {
        this.props = props;
        this.http = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    @Override
    public StructuredReportResult extract(String transcript, String outputLanguage) {
        String lang = outputLanguage == null ? "en" : outputLanguage;
        String prompt = buildPrompt(transcript, lang);
        String raw = callGemini(prompt);

        try {
            JsonNode node = mapper.readTree(raw);
            boolean low = node.path("lowConfidence").asBoolean(false);
            String finalReport = node.path("finalReport").asText("");

            // rebuild structured JSON from the extracted fields
            JsonNode structNode = mapper.createObjectNode()
                .put("labour", node.path("labour").toString())
                .put("progress", node.path("progress").toString())
                .put("delays", node.path("delays").toString())
                .put("issues", node.path("issues").toString())
                .put("incidents", node.path("incidents").toString())
                .put("materials", node.path("materials").toString())
                .put("nextActions", node.path("nextActions").toString());
            String structuredJson = mapper.writeValueAsString(node);

            if (finalReport.isBlank()) {
                finalReport = "Daily Site Report\n\n" + transcript
                    + "\n\nImportant: AI draft only. Manager review required before filing.";
                low = true;
            }

            return new StructuredReportResult(structuredJson, finalReport, low);

        } catch (Exception e) {
            // fallback — return raw text as report if JSON parsing fails
            return new StructuredReportResult("{}", raw, true);
        }
    }

    String callGemini(String prompt) {
        String requestBody = """
            {
              "contents": [{"parts": [{"text": %s}]}],
              "generationConfig": {"responseMimeType": "application/json"}
            }
            """.formatted(toJsonString(prompt));

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(GEMINI_URL + props.geminiApiKey()))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
            .build();

        // Gemini's free tier occasionally returns transient 503 (overloaded) or 429.
        // Retry a few times with backoff before surfacing an error to the user.
        int maxAttempts = 3;
        for (int attempt = 1; ; attempt++) {
            try {
                HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
                int status = response.statusCode();

                if ((status == 503 || status == 429) && attempt < maxAttempts) {
                    sleepQuietly(800L * attempt);
                    continue;
                }
                if (status == 429)
                    throw new IllegalStateException("AI extraction service is busy, please retry in a moment.");
                if (status != 200)
                    throw new IllegalStateException("AI extraction failed (HTTP " + status + ").");

                JsonNode root = mapper.readTree(response.body());
                return root.path("candidates").path(0)
                    .path("content").path("parts").path(0)
                    .path("text").asText("");

            } catch (IOException e) {
                if (attempt < maxAttempts) { sleepQuietly(800L * attempt); continue; }
                throw new IllegalStateException("AI extraction request failed: " + e.getMessage(), e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("AI extraction request failed: " + e.getMessage(), e);
            }
        }
    }

    private void sleepQuietly(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private String buildPrompt(String transcript, String outputLanguage) {
        String langInstruction = switch (outputLanguage) {
            case "ga" -> "Gaeilge (Irish) only";
            case "es" -> "Spanish (Español) only";
            case "uk" -> "Ukrainian (Українська) only";
            case "bilingual" -> "bilingual — write the finalReport with an English section first, then a Gaeilge (Irish) section";
            default -> "English only";
        };

        return """
            You are a construction site report assistant for Irish SMEs.

            Site update transcript:
            \"\"\"
            %s
            \"\"\"

            Output language: %s

            Return a JSON object with exactly this structure (no extra fields):
            {
              "labour": ["list of workers/trades mentioned"],
              "progress": ["work completed today"],
              "delays": ["any delays or blockers"],
              "issues": ["safety or quality issues"],
              "incidents": ["incidents — use \\"No incidents reported\\" if none"],
              "materials": ["materials used or needed"],
              "nextActions": ["planned next steps"],
              "finalReport": "A formal Daily Site Report suitable for Irish construction records",
              "lowConfidence": false
            }

            Rules:
            - If the transcript is unclear, very short (under 25 words), or missing key details, set lowConfidence to true
            - The finalReport must be a complete professional report, not just a summary
            - Always include at least one entry in incidents (use "No incidents reported" if none mentioned)
            - If outputLanguage is bilingual, the finalReport must have an English section followed by a Gaeilge section separated by a blank line
            """.formatted(transcript, langInstruction);
    }

    private String toJsonString(String text) {
        // safely escape text for embedding as a JSON string value
        try {
            return mapper.writeValueAsString(text);
        } catch (Exception e) {
            return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
        }
    }
}
