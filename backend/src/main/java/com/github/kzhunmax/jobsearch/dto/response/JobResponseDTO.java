package com.github.kzhunmax.jobsearch.dto.response;

public record JobResponseDTO(
        Long id,
        String title,
        String description,
        String company,
        String location,
        Double salary,
        boolean active,
        String postedBy
) {
}
