package com.github.kzhunmax.jobsearch.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record JobRequestDTO(
        @NotBlank(message = "Title is required")
        String title,

        String description,

        @NotBlank(message = "Company is required")
        String company,

        @NotBlank(message = "Location is required")
        String location,

        @NotNull(message = "Salary is required")
        @Positive(message = "Salary must be positive")
        Double salary
) {
}
