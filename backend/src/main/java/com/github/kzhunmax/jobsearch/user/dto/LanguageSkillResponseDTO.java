package com.github.kzhunmax.jobsearch.user.dto;

import com.github.kzhunmax.jobsearch.shared.enums.Language;
import com.github.kzhunmax.jobsearch.shared.enums.LanguageLevel;

public record LanguageSkillResponseDTO(
        Long id,
        Language language,
        LanguageLevel level
) {
}
