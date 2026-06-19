package com.example.rag.service;

public interface EmbeddingClient {

    float[] embed(String text);

    int dimension();
}
