package com.kdc.ohhcode.services.analyzer;

import com.kdc.ohhcode.constants.AppConstants;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new AccessDeniedException("Unauthorized"));
    SnippetEntity snippetEntity =
        snippetRepository
            .findById(snippetId)
            .orElseThrow(() -> new AccessDeniedException("Unauthorized"));
    try {

      String rawResponse = callWithRetry(snippetEntity);
      boolean isValid = aiSnippetAnalyzer.extractIsValid(rawResponse);
      AnalyzerEntity snippetAnalyzer =
          AnalyzerEntity.builder()
              .snippet(snippetEntity)
              .user(user)
              .jsonResponse(rawResponse)
              .build();
      analyzerRepository.save(snippetAnalyzer);

      Set<String> aiTags =
          aiSnippetAnalyzer.extractTags(rawResponse).stream()
              .filter(AppConstants.ALLOWED_TAGS::contains)
              .collect(Collectors.toSet());

      if (snippetEntity.getTags().isEmpty()) {
        snippetEntity.setTags(aiTags);
      }

      snippetEntity.setStatus(isValid ? SnippetStatus.ANALYZED : SnippetStatus.FAILED);
      log.info("AI processing completed for snippet {}", snippetEntity.getId());

    } catch (Exception e) {
      log.error("AI processing failed for snippet {}", snippetId, e);
      snippetEntity.setStatus(SnippetStatus.FAILED);
      AnalyzerEntity snippetAnalyzer =
          AnalyzerEntity.builder()
              .snippet(snippetEntity)
              .user(user)
              .jsonResponse(AppConstants.FALLBACK_RESPONSE)
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
        log.info(
            "Attempt - {} :  AI processing failed for snippet {}", attempt, snippetEntity.getId());
      }
    }
    throw new RuntimeException("AI processing failed for snippet " + snippetEntity.getId());
  }

  private String callWithTimeout(SnippetEntity snippetEntity) {
    return CompletableFuture.supplyAsync(() -> aiSnippetAnalyzer.aiSnippetAnalyzer(snippetEntity))
        .orTimeout(60, TimeUnit.SECONDS)
        .completeOnTimeout(AppConstants.FALLBACK_RESPONSE, 60, TimeUnit.SECONDS)
        .exceptionally(
            ex -> {
              log.error("failed generating ai response for snippet {}", snippetEntity.getId(), ex);
              return AppConstants.FALLBACK_RESPONSE;
            })
        .join();
  }
}

/*private String callWithTimeout(SnippetEntity snippetEntity) {
  return CompletableFuture.supplyAsync(() -> aiSnippetAnalyzer.aiSnippetAnalyzer(snippetEntity))
          .orTimeout(60, TimeUnit.SECONDS)
      .join();
}*/
