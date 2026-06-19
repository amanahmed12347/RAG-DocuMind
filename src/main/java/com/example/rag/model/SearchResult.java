package com.example.rag.model;

public record SearchResult(
        String documentName,
        int chunkIndex,
        String text,
        double score
) {
}
