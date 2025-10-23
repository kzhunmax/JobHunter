package com.github.kzhunmax.jobsearch.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;


@Schema(
        description = "Job posting details response",
        example = """
                {
                    "id": 1,
                    "title": "Java Developer",
                    "description": "Looking for experienced Java developer...",
                    "company": "BigTech",
                    "location": "Remote",
                    "salary": 120000.0,
                    "active": true,
                    "postedBy": "recruiter@example.com",
                    "createdAt": "2025-09-15T10:30:00",
                    "updatedAt": "2025-09-15T10:30:00"
                }
                """
)
public record JobResponseDTO(

        @Schema(description = "Unique job identifier", example = "1")
        Long id,

        @Schema(description = "Job title position", example = "Java Developer")
        String title,

        @Schema(description = "Detailed job description", example = "Looking for experienced Java developer...")
        String description,

        @Schema(description = "Company name", example = "BigTech")
        String company,

        @Schema(description = "Job location", example = "Remote")
        String location,

        @Schema(description = "Annual salary", example = "120000.0")
        Double salary,

        @Schema(description = "Application deadline for current position")
        LocalDate applicationDeadline,

        @Schema(description = "Whether the job posting is active", example = "true")
        boolean active,

        @Schema(description = "Email of the user who posted the job", example = "recruiter@example.com")
        String postedBy
) {
}
