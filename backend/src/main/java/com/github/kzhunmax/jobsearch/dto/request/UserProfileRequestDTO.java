package com.github.kzhunmax.jobsearch.dto.request;

import com.github.kzhunmax.jobsearch.model.Country;
import com.github.kzhunmax.jobsearch.model.ExperienceLevel;
import com.github.kzhunmax.jobsearch.model.WorkFormat;
import com.github.kzhunmax.jobsearch.model.WorkMode;
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
