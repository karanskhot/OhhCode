package com.kdc.ohhcode.services.analyzer;

import com.kdc.ohhcode.dtos.analyzer.AnalyzerResponseDto;
import com.kdc.ohhcode.entities.AnalyzerEntity;
import com.kdc.ohhcode.entities.SnippetEntity;

import com.kdc.ohhcode.entities.UserEntity;
import com.kdc.ohhcode.entities.enums.SnippetStatus;
import com.kdc.ohhcode.repositories.SnippetRepository;
import com.kdc.ohhcode.repositories.AnalyzerRepository;
import com.kdc.ohhcode.util.AiSnippetAnalyzer;
import com.kdc.ohhcode.util.AuthUtil;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnippetAnalyzerService {

  private final AuthUtil authUtil;
  private final AiSnippetAnalyzer aiSnippetAnalyzer;
  private final AsyncSnippetAnalysisService asyncSnippetAnalysisService;
  private final AnalyzerRepository analyzerRepository;

  private final SnippetRepository snippetRepository;

  @Transactional
  public AnalyzerResponseDto generateSnippetAnalysis(UUID id) {
    UserEntity user = authUtil.getCurrentUser();

    // 1: fetch snippet by snippetId and userId -- throw authentication error if not exist
    SnippetEntity snippet =
        snippetRepository
            .findByIdAndUserId(id, user.getId())
            .orElseThrow(
                () ->
                    new AccessDeniedException(
                        "User not authorized to generate analysis for snippet."));

    // 1: Check if already exists.
    Optional<AnalyzerEntity> existingAnalysis =
        analyzerRepository.findBySnippetIdAndUserId(id, user.getId());
    if (existingAnalysis.isPresent()) {
      return new AnalyzerResponseDto(
          existingAnalysis.get().getId(), existingAnalysis.get().getJsonResponse(),
          snippet.getStatus(), existingAnalysis.get().getSnippet().getId());
    }

    snippet.setStatus(SnippetStatus.ANALYZING);
    String rawResponse = aiSnippetAnalyzer.aiSnippetAnalyzer(snippet);
    boolean isValid = aiSnippetAnalyzer.extractIsValid(rawResponse);

    AnalyzerEntity snippetAnalyzer =
        AnalyzerEntity.builder().snippet(snippet).user(user).jsonResponse(rawResponse).build();

    AnalyzerEntity saved = analyzerRepository.save(snippetAnalyzer);
    snippet.setStatus(isValid ? SnippetStatus.ANALYZED : SnippetStatus.FAILED);

    return new AnalyzerResponseDto(
        saved.getId(), saved.getJsonResponse(), snippet.getStatus(), saved.getSnippet().getId());
  }

  public AnalyzerResponseDto getSnippetAnalysis(UUID analysisId) {
    UserEntity user = authUtil.getCurrentUser();
    AnalyzerEntity snippetAnalyzer =
        analyzerRepository
            .findByIdAndUserId(analysisId, user.getId())
            .orElseThrow(
                () ->
                    new AccessDeniedException(
                        "User not authorized to delete analysis for snippet."));

    return new AnalyzerResponseDto(
        snippetAnalyzer.getId(), snippetAnalyzer.getJsonResponse(),
        snippetAnalyzer.getSnippet().getStatus(), snippetAnalyzer.getSnippet().getId());
  }

  public List<AnalyzerResponseDto> getAllSnippetAnalysis() {
    UserEntity user = authUtil.getCurrentUser();
    List<AnalyzerEntity> snippetAnalyzersList = analyzerRepository.findAllByUserId(user.getId());
    return snippetAnalyzersList.stream()
        .map(
            s ->
                new AnalyzerResponseDto(
                    s.getId(),
                    s.getJsonResponse(),
                    s.getSnippet().getStatus(),
                    s.getSnippet().getId()))
        .toList();
  }

  @Transactional
  public void deleteAnalysis(UUID analysisId) {

    UserEntity user = authUtil.getCurrentUser();
    AnalyzerEntity snippetAnalyzer =
        analyzerRepository
            .findByIdAndUserId(analysisId, user.getId())
            .orElseThrow(
                () ->
                    new AccessDeniedException(
                        "User not authorized to delete analysis for snippet."));

    SnippetEntity snippet = snippetAnalyzer.getSnippet();
    snippet.setAnalysis(null);
    snippet.setStatus(SnippetStatus.UPLOADED);

    analyzerRepository.delete(snippetAnalyzer);
  }

  @Transactional
  public AnalyzerResponseDto regenerateSnippetAnalysis(UUID snippetId) {
    UserEntity user = authUtil.getCurrentUser();

    SnippetEntity snippet =
        snippetRepository
            .findByIdAndUserId(snippetId, user.getId())
            .orElseThrow(
                () ->
                    new AccessDeniedException(
                        "User Unauthorized to regenerate analysis for snippet."));

    if (snippet.getStatus() == SnippetStatus.ANALYZING) {
      throw new IllegalStateException("Snippet analysis is already being analyzed.");
    }

    analyzerRepository
        .findBySnippetIdAndUserId(snippetId, user.getId())
        .ifPresent(snippetAnalyzer -> deleteAnalysis(snippetAnalyzer.getId()));

    return generateSnippetAnalysis(snippetId);
  }

  @Transactional
  public AnalyzerResponseDto createSnippetAnalysisAsync(@Valid UUID snippetId) {
    UserEntity user = authUtil.getCurrentUser();

    // 1: fetch snippet by snippetId and userId -- throw authentication error if not exist
    SnippetEntity snippet =
        snippetRepository
            .findByIdAndUserId(snippetId, user.getId())
            .orElseThrow(
                () ->
                    new AccessDeniedException(
                        "User not authorized to generate analysis for snippet."));

    // 1: Check if already exists.
    Optional<AnalyzerEntity> existingAnalysis =
        analyzerRepository.findBySnippetIdAndUserId(snippetId, user.getId());
    if (existingAnalysis.isPresent()) {
      return new AnalyzerResponseDto(
          existingAnalysis.get().getId(), existingAnalysis.get().getJsonResponse(),
          snippet.getStatus(), existingAnalysis.get().getSnippet().getId());
    }

    if (snippet.getStatus() == SnippetStatus.ANALYZING) {
      return new AnalyzerResponseDto(null, null, SnippetStatus.ANALYZING, snippetId);
    }
    snippet.setStatus(SnippetStatus.ANALYZING);
    asyncSnippetAnalysisService.processAnalysisAsync(snippetId, user.getId());

    return new AnalyzerResponseDto(null, null, snippet.getStatus(), snippetId);
  }
}
