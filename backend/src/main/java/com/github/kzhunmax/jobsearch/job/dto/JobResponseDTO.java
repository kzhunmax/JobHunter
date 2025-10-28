package com.github.kzhunmax.jobsearch.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;


@Schema(description = "Job posting details response")
public record JobResponseDTO(

        @Schema(description = "Unique job identifier")
        Long id,

        @Schema(description = "Job title position")
        String title,

        @Schema(description = "Detailed job description")
        String description,

        @Schema(description = "Company name")
        String company,

        @Schema(description = "Job location")
        String location,

        @Schema(description = "Annual salary")
        Double salary,

        @Schema(description = "Application deadline for current position")
        LocalDate applicationDeadline,

        @Schema(description = "Whether the job posting is active")
        boolean active,

        @Schema(description = "Email of the user who posted the job")
        String postedBy
) {
}
