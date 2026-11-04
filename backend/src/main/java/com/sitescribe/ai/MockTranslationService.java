package com.sitescribe.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "sitescribe.ai.mode", havingValue = "mock", matchIfMissing = true)
public class MockTranslationService implements TranslationService {

    @Override
    public String translate(String text, String targetLanguage) {
        return text + "\n\n[Translation to " + targetLanguage + " pending — set AI_MODE=live]";
    }
}
