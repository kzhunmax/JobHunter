package com.github.kzhunmax.jobsearch.user.dto;

import com.github.kzhunmax.jobsearch.shared.enums.Language;
import com.github.kzhunmax.jobsearch.shared.enums.LanguageLevel;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for displaying a language skill")
public record LanguageSkillResponseDTO(
        @Schema(description = "Unique ID of the language skill entry", example = "1")
        Long id,

        @Schema(description = "Name of the language", example = "ENGLISH")
        Language language,

        @Schema(description = "Proficiency level", example = "B2")
        LanguageLevel level
) {
}
