package com.github.kzhunmax.jobsearch.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(
        description = "DTO for login",
        requiredProperties = {"email", "password"},
        example = """
                {
                    "email": "user@example.com",
                    "password": "Password123"
                }
                """
)
public record UserLoginDTO(

        @Schema(description = "Email address for login", example = "user@example.com", minLength = 3, maxLength = 255)
        @NotBlank(message = "Email is required")
        String email,

        @Schema(description = "User password", example = "Password123", minLength = 8, maxLength = 100, format = "password")
        @NotBlank(message = "Password is required")
        String password
) {
}
