package com.example.rag.api.dto;

import java.util.List;

public record AskResponse(
        String answer,
        List<SourceChunk> sources
) {
    public record SourceChunk(
            String documentName,
            int chunkIndex,
            double score,
            String text
    ) {
    }
}
