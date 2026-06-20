package com.example.rag.api;

import com.example.rag.model.Conversation;
import com.example.rag.service.ConversationRepository;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationRepository conversationRepository;

    public ConversationController(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    @GetMapping
    public List<Conversation> list(Authentication auth) {
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(auth.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Conversation create(@RequestBody Map<String, String> body, Authentication auth) {
        String title = body.getOrDefault("title", "New Chat");
        Conversation conversation = new Conversation(auth.getName(), title);
        return conversationRepository.save(conversation);
    }

    @PatchMapping("/{id}")
    public Conversation rename(@PathVariable String id, @RequestBody Map<String, String> body, Authentication auth) {
        Conversation conversation = conversationRepository.findByUserIdAndId(auth.getName(), id)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        conversation.setTitle(body.getOrDefault("title", conversation.getTitle()));
        conversation.setUpdatedAt(java.time.Instant.now());
        return conversationRepository.save(conversation);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id, Authentication auth) {
        Conversation conversation = conversationRepository.findByUserIdAndId(auth.getName(), id)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        conversationRepository.delete(conversation);
    }
}
