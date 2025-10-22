package com.github.kzhunmax.jobsearch.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;


@Schema(
        description = "Request to apply for a job position",
        example = """
                {
                    "coverLetter": "I am very interested in this position because..."
                }
                """
)
public record JobApplicationRequestDTO(

        @Schema(
                description = "Cover letter explaining interest and qualifications",
                example = "I am very interested in this position because...",
                maxLength = 2000
        )
        @Size(max = 2000)
        String coverLetter
) {
}
