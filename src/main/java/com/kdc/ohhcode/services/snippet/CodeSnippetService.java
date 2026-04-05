package com.kdc.ohhcode.services.snippet;

import com.kdc.ohhcode.dtos.snippet.SnippetRequestDto;
import com.kdc.ohhcode.dtos.snippet.SnippetResponseDto;
import com.kdc.ohhcode.entities.SnippetEntity;
import com.kdc.ohhcode.entities.UserEntity;
import com.kdc.ohhcode.entities.enums.Difficulty;
import com.kdc.ohhcode.entities.enums.Language;
import com.kdc.ohhcode.entities.enums.SnippetStatus;
import com.kdc.ohhcode.mappers.SnippetMapper;
import com.kdc.ohhcode.repositories.SnippetRepository;
import com.kdc.ohhcode.security.config.AppConfig;
import com.kdc.ohhcode.util.AuthUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodeSnippetService {

  private final SnippetRepository snippetRepository;
  private final AppConfig appConfig;
  private final AuthUtil authUtil;
  private final SnippetMapper snippetMapper;

  @Transactional
  public SnippetResponseDto createSnippet(SnippetRequestDto snippetRequestDto) {

    UserEntity user = authUtil.getCurrentUser();

    if (snippetRequestDto.snippetImage().isEmpty()) {
      throw new IllegalArgumentException("Code snippet image is required");
    }

    String hash = appConfig.generateHash(snippetRequestDto.snippetImage());

    Optional<SnippetEntity> snippet = snippetRepository.findByUserIdAndHashCode(user.getId(), hash);

    if (snippet.isPresent()) {
      return snippetMapper.toDto(snippet.get());
    }

    Optional<SnippetEntity> globalSnippet = snippetRepository.findFirstByHashCode(hash);
    String url;
    if (globalSnippet.isPresent()) {
      url = globalSnippet.get().getUrl();
    } else {
      url = appConfig.uploadImage(snippetRequestDto.snippetImage());
    }

    Language language = Objects.requireNonNullElse(snippetRequestDto.language(), Language.ENGLISH);
    Difficulty difficulty =
        Objects.requireNonNullElse(snippetRequestDto.difficulty(), Difficulty.MEDIUM);

    SnippetEntity snippetEntity =
        SnippetEntity.builder()
            .title(snippetRequestDto.title())
            .url(url)
            .hashCode(hash)
            .status(SnippetStatus.UPLOADED)
            .language(language)
            .difficulty(difficulty)
            .important(Boolean.TRUE.equals(snippetRequestDto.important()))
            .tags(Collections.emptySet())
            .user(user)
            .memoryNotes(snippetRequestDto.memoryNotes())
            .build();

    snippetRepository.save(snippetEntity);
    return snippetMapper.toDto(snippetEntity);
  }

  public List<SnippetResponseDto> getAllCodeSnippets() {
    UserEntity user = authUtil.getCurrentUser();
    List<SnippetEntity> snippetEntityList = snippetRepository.findAllByUserId(user.getId());
    return snippetEntityList.stream().map(snippetMapper::toDto).toList();
  }

  public SnippetResponseDto getCodeSnippetById(UUID id) {
    UserEntity user = authUtil.getCurrentUser();
    Optional<SnippetEntity> snippetEntity = snippetRepository.findByIdAndUserId(id, user.getId());
    if (snippetEntity.isPresent()) {
      return snippetMapper.toDto(snippetEntity.get());
    } else {
      throw new EntityNotFoundException("Code snippet id " + id + " not found");
    }
  }

  @Transactional
  public void deleteCodeSnippet(UUID id) {
    UserEntity user = authUtil.getCurrentUser();
    SnippetEntity snippet =
        snippetRepository
            .findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new AccessDeniedException("Not allowed to perform this operation"));
    snippetRepository.delete(snippet);
  }
}
