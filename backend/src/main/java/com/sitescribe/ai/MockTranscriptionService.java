package com.sitescribe.ai;

import com.sitescribe.upload.Upload;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "sitescribe.ai.mode", havingValue = "mock", matchIfMissing = true)
public class MockTranscriptionService implements TranscriptionService {

    @Override
    public TranscriptResult transcribe(Upload upload, String manualTranscript) {
        if (manualTranscript != null && !manualTranscript.isBlank())
            return new TranscriptResult(manualTranscript, detectLanguage(manualTranscript), false);
        return new TranscriptResult(
            "Manual transcript required for this mock build. Replace with real AI (set AI_MODE=live).",
            "en", true);
    }

    private String detectLanguage(String text) {
        String lower = text.toLowerCase();
        if (lower.matches(".*(bhí|inniu|obair|moill|timpiste|suíomh|críochnaíodh).*"))
            return lower.matches(".*(today|site|delay|worker).*") ? "mixed" : "ga";
        return "en";
    }
}
