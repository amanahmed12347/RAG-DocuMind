package com.example.rag.model;

public record DocumentChunk(
        String id,
        String documentId,
        String documentName,
        int chunkIndex,
        String text
) {
}
