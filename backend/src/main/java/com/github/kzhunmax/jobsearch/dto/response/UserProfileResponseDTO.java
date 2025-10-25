package com.github.kzhunmax.jobsearch.dto.response;

import com.github.kzhunmax.jobsearch.model.Country;
import com.github.kzhunmax.jobsearch.model.ExperienceLevel;
import com.github.kzhunmax.jobsearch.model.WorkFormat;
import com.github.kzhunmax.jobsearch.model.WorkMode;

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
