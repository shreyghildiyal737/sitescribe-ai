package com.sitescribe.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "sitescribe.ai")
public record AiProperties(
    @DefaultValue("mock") String mode,
    @DefaultValue("mock") String provider,
    @DefaultValue("") String apiKey,
    @DefaultValue("") String geminiApiKey
) {}
