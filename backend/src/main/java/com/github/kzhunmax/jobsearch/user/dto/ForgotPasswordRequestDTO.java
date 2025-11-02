package com.github.kzhunmax.jobsearch.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO for requesting a password reset")
public record ForgotPasswordRequestDTO(
        @Schema(description = "The email address of the account to reset", example = "user@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email
) {

    public ForgotPasswordRequestDTO(String email) {
        this.email = (email != null) ? email.trim().toLowerCase() : null;
    }
}
