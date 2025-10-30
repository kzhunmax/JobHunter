package com.github.kzhunmax.jobsearch.user.dto;

import com.github.kzhunmax.jobsearch.shared.enums.Country;
import com.github.kzhunmax.jobsearch.shared.enums.ExperienceLevel;
import com.github.kzhunmax.jobsearch.shared.enums.WorkFormat;
import com.github.kzhunmax.jobsearch.shared.enums.WorkMode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "DTO for displaying a user profile")
public record UserProfileResponseDTO(

        @Schema(description = "Unique ID of the user profile", example = "1")
        Long id,

        @Schema(description = "User's full name", example = "John Doe")
        String fullName,

        @Schema(description = "User's phone number", example = "+1234567890")
        String phoneNumber,

        @Schema(description = "Public URL to the user's profile photo")
        String photoUrl,

        @Schema(description = "A short bio about the user", example = "Senior Java Developer...")
        String about,

        @Schema(description = "User's country of residence", example = "USA")
        Country country,

        @Schema(description = "User's city of residence", example = "New York")
        String city,

        @Schema(description = "User's desired job title or current position", example = "Senior Java Developer")
        String position,

        @Schema(description = "User's level of experience", example = "FIVE_YEARS")
        ExperienceLevel experience,

        @Schema(description = "Desired work mode", example = "FULL_TIME")
        WorkMode workMode,

        @Schema(description = "Desired work format", example = "REMOTE")
        WorkFormat format,

        @Schema(description = "URL to the user's portfolio or personal website", example = "https://myportfolio.com")
        String portfolioUrl,

        @Schema(description = "List of language skills")
        List<LanguageSkillResponseDTO> languages,

        @Schema(description = "List of user's resumes")
        List<ResumeSummaryDTO> resumes
) {
}
