package com.github.kzhunmax.jobsearch.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


@Schema(description = "Request to apply for a job position")
public record JobApplicationRequestDTO(

        @Schema(description = "The ID of the resume to attach to this application", example = "1")
        @NotNull(message = "Resume ID is required")
        Long resumeId,

        @Schema(description = "Cover letter explaining interest and qualifications", maxLength = 2000)
        @Size(max = 2000)
        String coverLetter
) {
}
