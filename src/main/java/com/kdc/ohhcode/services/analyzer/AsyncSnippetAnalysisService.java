package com.kdc.ohhcode.services.analyzer;

import com.kdc.ohhcode.entities.AnalyzerEntity;
import com.kdc.ohhcode.entities.SnippetEntity;
import com.kdc.ohhcode.entities.UserEntity;
import com.kdc.ohhcode.entities.enums.SnippetStatus;
import com.kdc.ohhcode.repositories.SnippetRepository;
import com.kdc.ohhcode.repositories.AnalyzerRepository;
import com.kdc.ohhcode.repositories.UserRepository;
import com.kdc.ohhcode.util.AiSnippetAnalyzer;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncSnippetAnalysisService {

    private static final int MAX_RETRIES = 2;
    private final AiSnippetAnalyzer aiSnippetAnalyzer;
    private final AnalyzerRepository analyzerRepository;
    private final SnippetRepository snippetRepository;
    private final UserRepository userRepository;

    @Async
    @Transactional
    public void processAnalysisAsync(UUID snippetId, UUID userId) {

        log.info("AI processing started for snippet {}", snippetId);
        UserEntity user = userRepository.findById(userId).orElseThrow();
        SnippetEntity snippetEntity = snippetRepository.findById(snippetId)
                                                       .orElseThrow();
        try {

            String rawResponse = callWithRetry(snippetEntity);
            boolean isValid = aiSnippetAnalyzer.extractIsValid(rawResponse);
            AnalyzerEntity snippetAnalyzer = AnalyzerEntity.builder()
                                                           .snippet(snippetEntity)
                                                           .user(user)
                                                           .jsonResponse(rawResponse)
                                                           .build();
            analyzerRepository.save(snippetAnalyzer);
            snippetEntity.setStatus(isValid ? SnippetStatus.ANALYZED : SnippetStatus.FAILED);
            log.info("AI processing completed for snippet {}", snippetEntity.getId());

        } catch (Exception e) {
            log.error("AI processing failed for snippet {}", snippetId, e);
            snippetEntity.setStatus(SnippetStatus.FAILED);
            AnalyzerEntity snippetAnalyzer = AnalyzerEntity.builder()
                                                                         .snippet(snippetEntity)
                                                                         .user(user)
                                                                         .jsonResponse(fallbackResponse())
                                                                         .build();
            analyzerRepository.save(snippetAnalyzer);
        }
    }


    private String callWithRetry(SnippetEntity snippetEntity) {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                return callWithTimeout(snippetEntity);
            } catch (Exception e) {
                attempt++;
                log.info("Attempt - {} :  AI processing failed for snippet {}", attempt, snippetEntity.getId());
            }
        }
        throw new RuntimeException("AI processing failed for snippet " + snippetEntity.getId());
    }


    private String callWithTimeout(SnippetEntity snippetEntity) {
        return CompletableFuture.supplyAsync(() -> aiSnippetAnalyzer.aiSnippetAnalyzer(snippetEntity))
                                .orTimeout(20, TimeUnit.SECONDS)
                                .join();
    }


    private String fallbackResponse() {
        return """
                {
                  "isValid": false,
                  "meta": {
                    "title": "Analysis Failed",
                    "tags": [],
                    "difficulty": "Unknown",
                    "codeLanguage": "Unknown"
                  },
                  "message": "AI processing failed"
                }
                """;
    }
}

