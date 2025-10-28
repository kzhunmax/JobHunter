package com.github.kzhunmax.jobsearch.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO for login", requiredProperties = {"email", "password"})
public record UserLoginDTO(

        @Schema(description = "Email address for login", minLength = 3, maxLength = 255)
        @NotBlank(message = "Email is required")
        String email,

        @Schema(description = "User password", minLength = 8, maxLength = 100, format = "password")
        @NotBlank(message = "Password is required")
        String password
) {
}
