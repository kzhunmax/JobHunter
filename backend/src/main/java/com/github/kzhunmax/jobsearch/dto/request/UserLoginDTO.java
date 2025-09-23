package com.github.kzhunmax.jobsearch.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(
        description = "DTO for login",
        requiredProperties = {"usernameOrEmail", "password"}
)
public record UserLoginDTO(

        @Schema(description = "Username or email address for login", example = "user", minLength = 3, maxLength = 255)
        @NotBlank(message = "Username or email is required")
        String usernameOrEmail,

        @Schema(description = "User password", example = "Password123", minLength = 8, maxLength = 100, format = "password")
        @NotBlank(message = "Password is required")
        String password
) {
}
