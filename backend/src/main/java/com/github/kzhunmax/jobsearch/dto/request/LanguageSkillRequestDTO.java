package com.github.kzhunmax.jobsearch.dto.request;

import com.github.kzhunmax.jobsearch.model.Language;
import com.github.kzhunmax.jobsearch.model.LanguageLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LanguageSkillRequestDTO(

        @NotBlank(message = "Language name is required")
        Language language,

        @NotNull(message = "Language level is required")
        LanguageLevel level
) {
}
