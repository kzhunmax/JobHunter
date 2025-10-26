package com.github.kzhunmax.jobsearch.user.dto;

import com.github.kzhunmax.jobsearch.shared.enums.Language;
import com.github.kzhunmax.jobsearch.shared.enums.LanguageLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LanguageSkillRequestDTO(

        @NotBlank(message = "Language name is required")
        Language language,

        @NotNull(message = "Language level is required")
        LanguageLevel level
) {
}
