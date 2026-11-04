package com.sitescribe.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "sitescribe.ai.mode", havingValue = "live")
public class GeminiTranslationService implements TranslationService {

    private final GeminiExtractionService gemini;

    public GeminiTranslationService(GeminiExtractionService gemini) {
        this.gemini = gemini;
    }

    @Override
    public String translate(String text, String targetLanguage) {
        String langName = switch (targetLanguage) {
            case "ga" -> "Irish (Gaeilge)";
            case "es" -> "Spanish (Español)";
            case "uk" -> "Ukrainian (Українська)";
            default -> "English";
        };
        String prompt = "Translate the following construction site report to " + langName
            + ". Preserve the formatting and professional tone. Return only the translated text, no explanation.\n\n" + text;
        return gemini.callGemini(prompt);
    }
}
