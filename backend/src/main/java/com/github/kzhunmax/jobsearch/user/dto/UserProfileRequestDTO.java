package com.github.kzhunmax.jobsearch.user.dto;

import com.github.kzhunmax.jobsearch.shared.enums.Country;
import com.github.kzhunmax.jobsearch.shared.enums.ExperienceLevel;
import com.github.kzhunmax.jobsearch.shared.enums.WorkFormat;
import com.github.kzhunmax.jobsearch.shared.enums.WorkMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UserProfileRequestDTO(

        @NotBlank
        String fullName,

        String phoneNumber,
        String photoUrl,
        String about,

        @NotNull(message = "Country is required")
        Country country,

        String city,

        @NotBlank(message = "Position is required")
        String position,

        @NotNull(message = "Experience is required")
        ExperienceLevel experience,

        @NotNull(message = "Work mode is required")
        WorkMode workMode,

        @NotNull(message = "Work format is required")
        WorkFormat format,

        String portfolioUrl,

        @Valid
        List<LanguageSkillRequestDTO> languages

) {
}
