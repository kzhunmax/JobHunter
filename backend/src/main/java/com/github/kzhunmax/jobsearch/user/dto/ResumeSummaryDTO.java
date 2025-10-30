package com.github.kzhunmax.jobsearch.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Summary DTO for a user's resume")
public record ResumeSummaryDTO(
        @Schema(description = "Unique ID of the resume", example = "1")
        Long id,

        @Schema(description = "Title of the resume (usually the filename)", example = "My_Resume.pdf")
        String title,

        @Schema(description = "Public URL to access the resume file", example = "https://.../My_Resume.pdf")
        String fileUrl
) {
}
