package com.kdc.ohhcode.dtos.analyzer;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record AnalyzerRequestDto(

        @NotBlank(message = "snippet Id is missing for analysis")
        UUID snippetId

) {
}
