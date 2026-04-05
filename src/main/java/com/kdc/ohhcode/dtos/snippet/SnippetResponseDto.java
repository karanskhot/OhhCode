package com.kdc.ohhcode.dtos.snippet;

import com.kdc.ohhcode.entities.enums.Difficulty;
import com.kdc.ohhcode.entities.enums.Language;
import com.kdc.ohhcode.entities.enums.SnippetStatus;

import java.util.UUID;

public record SnippetResponseDto(
        UUID id,
        String title,
        String url,
        String hash,
        UUID userId,
        String memoryNotes,
        Difficulty difficulty,
        boolean important,
        SnippetStatus snippet,
        Language language) {
}
