package com.github.kzhunmax.jobsearch.user.dto;

import com.github.kzhunmax.jobsearch.shared.enums.Language;
import com.github.kzhunmax.jobsearch.shared.enums.LanguageLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "DTO for adding or updating a language skill")
public record LanguageSkillRequestDTO(

        @Schema(description = "Name of the language", example = "ENGLISH")
        @NotNull(message = "Language name is required")
        Language language,

        @Schema(description = "Proficiency level", example = "B2")
        @NotNull(message = "Language level is required")
        LanguageLevel level
) {
}
