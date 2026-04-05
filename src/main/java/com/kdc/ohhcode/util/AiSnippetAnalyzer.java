package com.kdc.ohhcode.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdc.ohhcode.entities.SnippetEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSnippetAnalyzer {

    private final ChatClient chatClient;
    ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Value("classpath:prompt/code-snippet-analyzer-2.st")
    private Resource promptResource;


    public String aiSnippetAnalyzer(SnippetEntity snippetEntity) {
        String language = snippetEntity.getLanguage().name();
        String difficulty = snippetEntity.getDifficulty().name();
        String url = snippetEntity.getUrl();
        String prompt = createPrompt(language, difficulty);

        Media snippet = new Media(MimeTypeUtils.IMAGE_PNG, URI.create(url));
        return chatClient.prompt().system(prompt).user(u -> u.text("""
                STRICT:
                - Return ONLY valid JSON
                - No markdown / no explanation
                
                If NO DSA problem or code found:
                { "meta": { "title": "Invalid Input" } }
                """).media(snippet)).call().content();
    }

    private String createPrompt(String language, String difficulty) {

        log.info("Creating prompt... language={}, difficulty={}", language, difficulty);

        try {
            String template = new String(promptResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            String safeLanguage = language != null ? language : "ENGLISH";
            String safeDifficulty = difficulty != null ? difficulty : "MEDIUM";
            return template.replace("{{language}}", safeLanguage).replace("{{difficulty}}", safeDifficulty);
        } catch (IOException e) {
            throw new RuntimeException("Failed to build prompt", e);
        }
    }

    public boolean extractIsValid(String rawResponse) {
        try {
            JsonNode rootNode = mapper.readTree(rawResponse);
            JsonNode isValidNode = rootNode.get("isValid");
            return isValidNode != null && isValidNode.asBoolean();
        } catch (Exception e) {
            return false;
        }
    }
}
