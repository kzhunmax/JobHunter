package com.github.kzhunmax.jobsearch.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Job application details response")
public record JobApplicationResponseDTO(

        @Schema(description = "Unique application identifier")
        Long id,

        @Schema(description = "ID of the applied job")
        Long jobId,

        @Schema(description = "Title of the job position")
        String jobTitle,

        @Schema(description = "Company offering the job")
        String company,

        @Schema(description = "Email of the applicant")
        String candidateEmail,

        @Schema(description = "ID of the applicant's user profile")
        Long candidateProfileId,

        @Schema(description = "Current application status", allowableValues = {"APPLIED", "UNDER_REVIEW", "INTERVIEWED", "REJECTED", "OFFERED", "ACCEPTED"})
        String status,

        @Schema(description = "When the application was submitted")
        String appliedAt,

        @Schema(description = "Cover letter text")
        String coverLetter,

        @Schema(description = "An url to candidate resume")
        String resumeUrl
) {
}
