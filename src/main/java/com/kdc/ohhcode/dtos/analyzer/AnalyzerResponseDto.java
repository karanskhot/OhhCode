package com.kdc.ohhcode.dtos.analyzer;


import com.fasterxml.jackson.annotation.JsonRawValue;
import com.kdc.ohhcode.entities.enums.SnippetStatus;

import java.util.UUID;

public record AnalyzerResponseDto(
        UUID analysisId,
        @JsonRawValue
        String analysis,
        SnippetStatus status,
        UUID snippetId
) {
}