package com.example.rag.service;

import com.example.rag.api.dto.AskRequest;
import com.example.rag.api.dto.AskResponse;
import com.example.rag.config.RagProperties;
import com.example.rag.llm.ChatClient;
import com.example.rag.model.SearchResult;
import com.example.rag.vector.VectorStore;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class QuestionAnsweringService {

    private static final String UNKNOWN_ANSWER = "I don't know based on the provided documents.";

    private final VectorStore vectorStore;
    private final PromptBuilder promptBuilder;
    private final ChatClient chatClient;
    private final RagProperties properties;

    public QuestionAnsweringService(
            VectorStore vectorStore,
            PromptBuilder promptBuilder,
            ChatClient chatClient,
            RagProperties properties
    ) {
        this.vectorStore = vectorStore;
        this.promptBuilder = promptBuilder;
        this.chatClient = chatClient;
        this.properties = properties;
    }

    public AskResponse answer(AskRequest request) {
        int topK = request.topK() == null ? properties.defaultTopK() : request.topK();
        List<SearchResult> results = vectorStore.search(request.question(), topK).stream()
                .filter(result -> result.score() >= properties.minRelevanceScore())
                .toList();

        if (results.isEmpty()) {
            return new AskResponse(UNKNOWN_ANSWER, List.of());
        }

        String prompt = promptBuilder.build(request.question(), results);
        String answer = chatClient.complete(prompt);

        List<AskResponse.SourceChunk> sources = results.stream()
                .map(result -> new AskResponse.SourceChunk(
                        result.documentName(),
                        result.chunkIndex(),
                        result.score(),
                        result.text()
                ))
                .toList();

        return new AskResponse(answer, sources);
    }
}
