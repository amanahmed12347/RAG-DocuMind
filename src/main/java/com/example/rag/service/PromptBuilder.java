package com.example.rag.service;

import com.example.rag.config.RagProperties;
import com.example.rag.model.SearchResult;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PromptBuilder {

    private final RagProperties properties;

    public PromptBuilder(RagProperties properties) {
        this.properties = properties;
    }

    public String build(String question, List<SearchResult> results) {
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < results.size(); i++) {
            SearchResult result = results.get(i);
            String block = """
                    Source %d | document=%s | chunk=%d
                    %s

                    """.formatted(i + 1, result.documentName(), result.chunkIndex(), result.text());

            if (context.length() + block.length() > properties.maxContextChars()) {
                break;
            }
            context.append(block);
        }

        return """
                You are a Document Q&A assistant.

                Rules:
                - Answer using only the provided document context.
                - If the context is insufficient, respond exactly: I don't know based on the provided documents.
                - Keep the answer concise, factual, and grounded.
                - Do not use prior knowledge.

                Document context:
                %s

                User question:
                %s
                """.formatted(context, question);
    }
}
