package com.github.kzhunmax.jobsearch.company.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for displaying company details")
public record CompanyResponseDTO(

        @Schema(description = "Unique ID of the company", example = "1")
        Long id,

        @Schema(description = "Company's official name", example = "Tech Solutions Inc.")
        String name,

        @Schema(description = "A brief description of the company", example = "A leading provider of cloud solutions.")
        String description,

        @Schema(description = "Company's primary location", example = "New York, NY")
        String location,

        @Schema(description = "Company's official website", example = "https://techsolutions.com")
        String website,

        @Schema(description = "URL to the company's logo", example = "https://cdn.techsolutions.com/logo.png")
        String logoUrl
) {
}
