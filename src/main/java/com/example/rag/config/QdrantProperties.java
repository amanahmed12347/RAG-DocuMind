package com.example.rag.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "qdrant")
public record QdrantProperties(
        @NotBlank String baseUrl,
        @NotBlank String collectionName,
        String apiKey
) {
}
