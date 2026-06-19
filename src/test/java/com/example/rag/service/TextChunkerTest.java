package com.example.rag.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.rag.config.RagProperties;
import org.junit.jupiter.api.Test;

class TextChunkerTest {

    @Test
    void splitsLongTextIntoMultipleChunks() {
        TextChunker chunker = new TextChunker(new RagProperties(40, 10, 5, 7000, 0.15));

        assertThat(chunker.split("First paragraph has useful text.\n\nSecond paragraph has more useful text."))
                .hasSize(2);
    }
}
