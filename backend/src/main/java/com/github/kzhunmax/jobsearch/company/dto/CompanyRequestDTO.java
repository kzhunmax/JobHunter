package com.github.kzhunmax.jobsearch.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

@Schema(description = "DTO for creating or updating a company")
public record CompanyRequestDTO(
        @Schema(description = "Company's official name", example = "Tech Solutions Inc.")
        @NotBlank(message = "Company name is required")
        String name,

        @Schema(description = "A brief description of the company", example = "A leading provider of cloud solutions.")
        String description,

        @Schema(description = "Company's primary location", example = "New York, NY")
        @NotBlank(message = "Location is required")
        String location,

        @Schema(description = "Company's official website", example = "https://company.com")
        @URL(message = "Must be a valid URL")
        String website,

        @Schema(description = "URL to the company's logo", example = "https://cdn.company.com/logo.png")
        @URL(message = "Must be a valid URL")
        String logoUrl
) {
}
