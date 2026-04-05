package com.kdc.ohhcode.dtos.snippet;

import com.kdc.ohhcode.entities.enums.Difficulty;
import com.kdc.ohhcode.entities.enums.Language;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;


public record SnippetRequestDto(

        @Size(max = 160)
        String title,

        @NotNull(message = "Snippet Image is required.")
        MultipartFile snippetImage,

        String memoryNotes,

        Language language,

        Difficulty difficulty,

        Boolean important
) {
}
