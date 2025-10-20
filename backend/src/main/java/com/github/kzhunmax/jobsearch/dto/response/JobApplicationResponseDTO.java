package com.github.kzhunmax.jobsearch.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Job application details response",
        example = """
        {
            "id": 123,
            "jobId": 123,
            "jobTitle": "Java Developer",
            "company": "BigTech",
            "candidateEmail": "user@example.com",
            "status": "APPLIED",
            "appliedAt": "2025-09-15T10:30:00Z",
            "coverLetter": "I am very interested in this position because...",
        }
        """
)
public record JobApplicationResponseDTO(

        @Schema(description = "Unique application identifier", example = "123")
        Long id,

        @Schema(description = "ID of the applied job", example = "123")
        Long jobId,

        @Schema(description = "Title of the job position", example = "Java Developer")
        String jobTitle,

        @Schema(description = "Company offering the job", example = "BigTech")
        String company,

        @Schema(description = "Email of the applicant", example = "user@example.com")
        String candidateEmail,

        @Schema(
                description = "Current application status",
                example = "APPLIED",
                allowableValues = {"APPLIED", "UNDER_REVIEW", "INTERVIEWED", "REJECTED", "OFFERED", "ACCEPTED"}
        )
        String status,

        @Schema(description = "When the application was submitted", example = "2025-09-15T10:30:00Z")
        String appliedAt,

        @Schema(description = "Cover letter text", example = "I am very interested in this position because...")
        String coverLetter,

        @Schema(description = "An url to candidate resume")
        String cvUrl
) {
}
