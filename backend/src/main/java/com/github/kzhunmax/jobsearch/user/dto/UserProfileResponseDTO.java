package com.github.kzhunmax.jobsearch.user.dto;

import com.github.kzhunmax.jobsearch.shared.enums.Country;
import com.github.kzhunmax.jobsearch.shared.enums.ExperienceLevel;
import com.github.kzhunmax.jobsearch.shared.enums.WorkFormat;
import com.github.kzhunmax.jobsearch.shared.enums.WorkMode;

import java.util.List;

public record UserProfileResponseDTO(
        Long id,

        String fullName,

        String phoneNumber,

        String photoUrl,

        String about,

        Country country,

        String city,

        String position,

        ExperienceLevel experience,

        WorkMode workMode,

        WorkFormat format,

        String portfolioUrl,

        List<LanguageSkillResponseDTO> languages,

        List<ResumeSummaryDTO> resumes
) {
}
