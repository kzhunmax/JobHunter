package com.github.kzhunmax.jobsearch.dto.response;

public record JobApplicationResponseDTO(
        Long id,
        Long jobId,
        String jobTitle,
        String company,
        String candidateUsername,
        String status,
        String appliedAt,
        String coverLetter
) {
}
