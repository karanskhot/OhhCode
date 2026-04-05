package com.kdc.ohhcode.controllers.snippet;

import com.kdc.ohhcode.dtos.snippet.SnippetRequestDto;
import com.kdc.ohhcode.dtos.snippet.SnippetResponseDto;
import com.kdc.ohhcode.services.snippet.CodeSnippetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/snippets")
public class SnippetController {

  private final CodeSnippetService codeSnippetService;

  @PostMapping
  public ResponseEntity<SnippetResponseDto> createSnippet(
      @ModelAttribute @Valid SnippetRequestDto snippetRequestDto) {

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(codeSnippetService.createSnippet(snippetRequestDto));
  }

  @GetMapping
  public ResponseEntity<List<SnippetResponseDto>> getAllSnippets() {
    return ResponseEntity.status(HttpStatus.OK).body(codeSnippetService.getAllCodeSnippets());
  }

  @GetMapping("/{id}")
  public ResponseEntity<SnippetResponseDto> getSnippet(@PathVariable UUID id) {
    return ResponseEntity.status(HttpStatus.OK).body(codeSnippetService.getCodeSnippetById(id));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteSnippet(@PathVariable UUID id) {
    codeSnippetService.deleteCodeSnippet(id);
    return ResponseEntity.noContent().build();
  }
}
