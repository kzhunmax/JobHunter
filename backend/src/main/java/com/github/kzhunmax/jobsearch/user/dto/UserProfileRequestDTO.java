package com.github.kzhunmax.jobsearch.user.dto;

import com.github.kzhunmax.jobsearch.shared.enums.Country;
import com.github.kzhunmax.jobsearch.shared.enums.ExperienceLevel;
import com.github.kzhunmax.jobsearch.shared.enums.WorkFormat;
import com.github.kzhunmax.jobsearch.shared.enums.WorkMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "DTO for creating or updating a user profile")
public record UserProfileRequestDTO(

        @Schema(description = "User's full name", example = "John Doe")
        @NotBlank
        String fullName,

        @Schema(description = "User's phone number", example = "+1234567890")
        String phoneNumber,

        @Schema(description = "Public URL to the user's profile photo")
        String photoUrl,

        @Schema(description = "A short bio about the user", example = "Senior Java Developer with 5 years of experience...")
        String about,

        @Schema(description = "User's country of residence", example = "USA")
        @NotNull(message = "Country is required")
        Country country,

        @Schema(description = "User's city of residence", example = "New York")
        String city,

        @Schema(description = "User's desired job title or current position", example = "Senior Java Developer")
        @NotBlank(message = "Position is required")
        String position,


        @Schema(description = "User's level of experience", example = "FIVE_YEARS")
        @NotNull(message = "Experience is required")
        ExperienceLevel experience,


        @Schema(description = "Desired work mode", example = "FULL_TIME")
        @NotNull(message = "Work mode is required")
        WorkMode workMode,

        @Schema(description = "Desired work format", example = "REMOTE")
        @NotNull(message = "Work format is required")
        WorkFormat format,

        @Schema(description = "URL to the user's portfolio or personal website", example = "https://myportfolio.com")
        String portfolioUrl,

        @Schema(description = "List of language skills")
        @Valid
        List<LanguageSkillRequestDTO> languages

) {
}
