package com.github.kzhunmax.jobsearch.dto.request;

public record JobRequestDTO(
        String title,
        String description,
        String company,
        String location,
        Double salary
) {
}
