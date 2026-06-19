package com.example.rag.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "openrouter")
public record OpenRouterProperties(
        @NotBlank String baseUrl,
        String apiKey,
        @NotBlank String model,
        double temperature,
        @Positive int timeoutSeconds
) {
}
