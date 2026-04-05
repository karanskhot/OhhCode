package com.kdc.ohhcode.controllers.analyzer;

import com.kdc.ohhcode.dtos.analyzer.AnalyzerResponseDto;
import com.kdc.ohhcode.services.analyzer.SnippetAnalyzerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
public class AnalyzerController {

  private final SnippetAnalyzerService snippetAnalyzerService;

  @PostMapping("/snippets/{snippetId}/analyzers")
  public ResponseEntity<AnalyzerResponseDto> createAnalysis(@PathVariable @Valid UUID snippetId) {

    return ResponseEntity.status(HttpStatus.CREATED)
        .contentType(MediaType.APPLICATION_JSON)
        .body(snippetAnalyzerService.generateSnippetAnalysis(snippetId));
  }

  @PostMapping("/snippets/{snippetId}/async-analyzers")
  public ResponseEntity<AnalyzerResponseDto> createAnalysisAsync(
      @PathVariable @Valid UUID snippetId) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .contentType(MediaType.APPLICATION_JSON)
        .body(snippetAnalyzerService.createSnippetAnalysisAsync(snippetId));
  }

  @PostMapping("/snippets/{snippetId}/regenerate")
  public ResponseEntity<AnalyzerResponseDto> regenerateAnalysis(
      @PathVariable @Valid UUID snippetId) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .contentType(MediaType.APPLICATION_JSON)
        .body(snippetAnalyzerService.regenerateSnippetAnalysis(snippetId));
  }

  @GetMapping("/analyzers")
  public ResponseEntity<List<AnalyzerResponseDto>> findAllAnalyses() {
    return ResponseEntity.status(HttpStatus.OK)
        .body(snippetAnalyzerService.getAllSnippetAnalysis());
  }

  @GetMapping("/analyzers/{analysisId}")
  public ResponseEntity<AnalyzerResponseDto> getAnalysis(@PathVariable UUID analysisId) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(snippetAnalyzerService.getSnippetAnalysis(analysisId));
  }

  @DeleteMapping("/analyzers/{analysisId}")
  public ResponseEntity<Void> deleteAnalysis(@PathVariable UUID analysisId) {
    snippetAnalyzerService.deleteAnalysis(analysisId);
    return ResponseEntity.noContent().build();
  }
}
