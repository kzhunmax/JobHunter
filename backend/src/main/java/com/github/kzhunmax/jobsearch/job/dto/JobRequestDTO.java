package com.github.kzhunmax.jobsearch.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;


@Schema(
        description = "Request to create or update a job posting",
        example = """
                {
                    "title": "Java Developer",
                    "description": "Looking for experienced Java developer...",
                    "company": "BigTech Inc.",
                    "location": "Remote",
                    "salary": 120000.0
                }
                """
)
public record JobRequestDTO(

        @Schema(
                description = "Job title position",
                example = "Java Developer",
                minLength = 3,
                maxLength = 100
        )
        @NotBlank(message = "Title is required")
        @Size(min = 3, max = 100)
        String title,

        @Schema(
                description = "Detailed job description and requirements",
                example = "Looking for experienced Java developer..."
        )
        String description,


        @Schema(
                description = "Company name offering the position",
                example = "BigTech",
                maxLength = 255
        )
        @NotBlank(message = "Company is required")
        @Size(max = 255)
        String company,

        @Schema(
                description = "Job location (city, state, remote, etc.)",
                example = "Remote",
                maxLength = 100
        )
        @NotBlank(message = "Location is required")
        @Size(max = 100)
        String location,

        @Schema(
                description = "Annual salary",
                example = "120000.0",
                minimum = "0"
        )
        @NotNull(message = "Salary is required")
        @Positive(message = "Salary must be positive")
        Double salary,

        @Schema(
                description = "Application dealine for current position"
        )
        @NotNull(message = "Deadline for applying is required")
        LocalDate applicationDeadline

) {
}
