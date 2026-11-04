package com.sitescribe.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sitescribe.config.AiProperties;
import com.sitescribe.storage.StorageService;
import com.sitescribe.upload.Upload;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "sitescribe.ai.mode", havingValue = "live")
public class GroqTranscriptionService implements TranscriptionService {

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/audio/transcriptions";

    private final AiProperties props;
    private final StorageService storage;
    private final HttpClient http;
    private final ObjectMapper mapper;

    public GroqTranscriptionService(AiProperties props, StorageService storage) {
        this.props = props;
        this.storage = storage;
        this.http = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    @Override
    public TranscriptResult transcribe(Upload upload, String manualTranscript) {
        if (manualTranscript != null && !manualTranscript.isBlank())
            return new TranscriptResult(manualTranscript, detectLanguage(manualTranscript), false);

        try {
            byte[] audioBytes = storage.retrieve(upload.getFilePath());
            String boundary = UUID.randomUUID().toString().replace("-", "");
            String contentType = upload.getContentType() != null ? upload.getContentType() : "audio/mpeg";

            MultipartBodyBuilder body = new MultipartBodyBuilder(boundary);
            body.addFile("file", upload.getFileName(), contentType, audioBytes);
            body.addField("model", "whisper-large-v3-turbo");
            body.addField("response_format", "verbose_json");
            byte[] bodyBytes = body.finish();

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GROQ_URL))
                .header("Authorization", "Bearer " + props.apiKey())
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(bodyBytes))
                .build();

            // Retry transient overloads (503) / rate limits (429) before failing.
            int maxAttempts = 3;
            for (int attempt = 1; ; attempt++) {
                HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
                int status = response.statusCode();

                if ((status == 503 || status == 429) && attempt < maxAttempts) {
                    sleepQuietly(800L * attempt);
                    continue;
                }
                if (status == 429)
                    throw new IllegalStateException("Transcription service is busy, please retry in a moment.");
                if (status != 200)
                    throw new IllegalStateException("Transcription failed (HTTP " + status + ").");

                JsonNode json = mapper.readTree(response.body());
                String text = json.path("text").asText();
                String language = json.path("language").asText("en");
                return new TranscriptResult(text, language, text.isBlank());
            }

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Transcription request failed: " + e.getMessage(), e);
        }
    }

    private void sleepQuietly(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private String detectLanguage(String text) {
        String lower = text.toLowerCase();
        if (lower.matches(".*(bhí|inniu|obair|moill|timpiste|suíomh|críochnaíodh).*"))
            return lower.matches(".*(today|site|delay|worker).*") ? "mixed" : "ga";
        return "en";
    }
}
