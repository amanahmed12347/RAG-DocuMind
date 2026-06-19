package com.example.rag.api.dto;

public record UploadResponse(
        String documentId,
        String fileName,
        int chunksStored
) {
}
