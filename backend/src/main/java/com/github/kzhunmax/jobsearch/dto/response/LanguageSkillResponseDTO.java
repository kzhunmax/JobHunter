package com.github.kzhunmax.jobsearch.dto.response;

import com.github.kzhunmax.jobsearch.model.Language;
import com.github.kzhunmax.jobsearch.model.LanguageLevel;

public record LanguageSkillResponseDTO(
        Long id,
        Language language,
        LanguageLevel level
) {
}
