package com.github.kzhunmax.jobsearch.dto.request;

import jakarta.validation.constraints.NotNull;

public record JobApplicationRequestDTO(
    @NotNull
    Long jobId,

    String coverLetter
) {
}
