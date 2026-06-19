package com.example.rag.service;

import com.example.rag.config.EmbeddingProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class HashingEmbeddingClient implements EmbeddingClient {

    private final EmbeddingProperties properties;

    public HashingEmbeddingClient(EmbeddingProperties properties) {
        this.properties = properties;
    }

    @Override
    public float[] embed(String text) {
        float[] vector = new float[dimension()];
        String[] tokens = text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+");

        for (String token : tokens) {
            if (token.length() < 2) {
                continue;
            }
            byte[] digest = sha256(token);
            int bucket = Math.floorMod(toInt(digest, 0), vector.length);
            int sign = (digest[4] & 1) == 0 ? 1 : -1;
            vector[bucket] += sign;
        }

        normalize(vector);
        return vector;
    }

    @Override
    public int dimension() {
        return properties.dimension();
    }

    private byte[] sha256(String token) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required by the JVM", exception);
        }
    }

    private int toInt(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xff) << 24)
                | ((bytes[offset + 1] & 0xff) << 16)
                | ((bytes[offset + 2] & 0xff) << 8)
                | (bytes[offset + 3] & 0xff);
    }

    private void normalize(float[] vector) {
        double sum = 0;
        for (float value : vector) {
            sum += value * value;
        }
        double norm = Math.sqrt(sum);
        if (norm == 0) {
            return;
        }
        for (int i = 0; i < vector.length; i++) {
            vector[i] = (float) (vector[i] / norm);
        }
    }
}
